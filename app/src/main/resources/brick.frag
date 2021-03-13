//#version 120
//
// simple.frag
//
uniform mat4 mat[4];
uniform sampler2D texture0;
uniform sampler2D texture1;
uniform sampler2D texture2;
uniform vec3 lightpos;
uniform vec3 lightcolor;
varying vec3 normal;
varying vec4 color;
varying vec2 texcoord;
varying vec3 viewdir;
varying vec3 lightdir;
varying vec3 pos;
varying vec3 tangent;

const float PI = 3.141592653589793;
const float pi23 = PI/2.0 - 2.0/3.0;
const float PIrt = 0.7978845608; //sqrt(2.0/PI);


float OrenNayar(vec3 n, vec3 l, vec3 v, float roughness) {
	float baseAB = (PI + pi23*roughness);
	float A = 1.0 / baseAB;
	float B = roughness / baseAB;

	float nl = dot(n,l);
	float nv = dot(n,v);
	float s = dot(l,v) - nl*nv;
	float t = (s <= 0.0) ? 1.0 : max(nl, abs(nv));

	return PI * nl * (A + B * s/t);
}

// h = (v+l)/2.0
vec3 F_Schlick(vec3 color, vec3 l, vec3 h) {
  float lh = dot(l,h);
  return color + (vec3(1.0, 1.0, 1.0) - color)*pow(1.0 - lh, 5.0);
}

// V = G / (4.0*dot(l,n)*dot(v,n))
float V_JointGGX(vec3 n, vec3 l, vec3 v, float roughness) {
  float a = roughness*roughness;
  float a2 = a*a;
  float nl = dot(n,l);
  float nv = dot(n,v);
  float tl = nv * sqrt(nl*nl*(1.0-a2)+a2);
  float tv = nl * sqrt(nv*nv*(1.0-a2)+a2);
  return 0.5 / (tl + tv);
}

// h = (v+l)/2.0
float D_GGX(vec3 n, vec3 h, float roughness) {
  float a = roughness*roughness;
  float a2 = a*a;
  float nh = dot(n,h);
  if (nh >= 0.39) {
    float tmp = 1.0 - (1.0-a2) * nh*nh;
    return a2 / (PI*tmp*tmp);
  } else { return 0.0; }
}

vec3 sampleBump(sampler2D F, vec2 v) {
	return (texture2D(F, v)*2.0-1.0).xyz;
}
float sampleHeight(vec2 t) {
	return texture2D(texture2, t).x;
}


vec2 getUV_BinarySearch(vec2 texCoord, vec3 view) {
	float height = sampleHeight(texcoord);
	int resolution = 64;
	float maxdepth = 0.01;
	vec3 dv = maxdepth * vec3(view.x, -view.y, view.z);

	vec3 v = vec3(0.0, 0.0, 0.0);
	vec3 before;
	bool isForward = true;
	for (int i = 0; i < resolution; i++) {
	  before = v;
	  v = v + dv;
	  vec2 t = texCoord + v.xy;
	  float hRay = 1.0 - v.z;
	  float hObj = sampleHeight(t);
	  if (isForward) {
	    if (hRay < hObj) { dv = -dv/2.0; isForward = false; }
	    else { dv = dv/2.0; }
	  } else {
	    if (hObj < hRay) { dv = -dv/2.0; isForward = true; }
	    else { dv = dv/2.0; }
	  }
	}
	return texCoord + before.xy;
}

vec2 getUV_LinearSearch(vec2 texCoord, vec3 view) {
	float height = sampleHeight(texcoord);
	int resolution = 128;
	float maxdepth = 0.1;
	vec3 dv = maxdepth * vec3(view.x, -view.y, view.z) / float(resolution);

	vec3 v = vec3(0.0, 0.0, 0.0);
	for (int i = 0; i < resolution; i++) {
	  v = v + dv; // v = dv*(i+1)
	  vec2 t = texCoord + v.xy;
	  float hRay = 1.0 - v.z;
	  float hObj = sampleHeight(t);
	  if (hRay < hObj) break;
	}

	//test
	//v = 0.5 * vec3(view.x, -view.y, 0.0);
	//v = 0.5 * vec3(view.x, 0.0, 0.0);

	return texCoord + v.xy;
}

vec3 dFdx(sampler2D F, vec2 v) {
	float d = 0.003;
	vec2 dx = vec2(d, 0);
	vec2 dy = vec2(0, d);

	// Ando's filter
	float a = 0.112737;
	float b = 0.274526;
	vec3 deltaV =
		+b * sampleBump(F, v +dx)
		-b * sampleBump(F, v -dx)
		+a * sampleBump(F, v +dx+dy)
		-a * sampleBump(F, v -dx+dy)
		+a * sampleBump(F, v +dx-dy)
		-a * sampleBump(F, v -dx-dy);
	return deltaV / d;
}

vec3 dFdy(sampler2D F, vec2 v) {
	float d = 0.1;
	vec2 dx = vec2(d, 0);
	vec2 dy = vec2(0, d);

	// Ando's filter
	float a = 0.112737;
	float b = 0.274526;
	vec3 deltaV =
		+b * sampleBump(F, v +dy)
		-b * sampleBump(F, v -dy)
		+a * sampleBump(F, v +dx+dy)
		-a * sampleBump(F, v +dx-dy)
		+a * sampleBump(F, v -dx+dy)
		-a * sampleBump(F, v -dx-dy);
	return deltaV / d;
}


void main (void){
  // view vector
  vec3 v = normalize(viewdir);

  // parallax mapping
  vec2 uv = getUV_BinarySearch(texcoord, viewdir);
  vec3 bump = sampleBump(texture0, uv);

  // vector parameters
  vec3 l = normalize(lightpos - pos);	// Spot light
  vec3 n = normalize(normal + 0.3*bump);
  vec3 h = (l+v)/2.0;

  // wet
  vec2 dfdx = abs(dFdx(texture0, uv).xy);
  vec2 dfdy = abs(dFdy(texture0, uv).xy);
  float d = max(max(dfdx.x,dfdx.y),max(dfdy.x,dfdy.y));
  float wet = 1.0-d*0.01;

  // color parameters
  float brightness = texture2D(texture1, uv).x * 0.7 + 0.3;
  float roughness = 1.0 - texture2D(texture2, uv).x*0.2 - wet*0.7;
  vec3 diffuseColor = color.xyz * brightness;
  vec3 specularColor = lightcolor;

  // reflections
  vec3 diffuse = OrenNayar(n,l,v, roughness) *diffuseColor*lightcolor;
  vec3 specular = F_Schlick(specularColor, l, h) * V_JointGGX(n,l,v, roughness) * D_GGX(n,h, roughness);
  vec3 ambient = diffuseColor*lightcolor * texture2D(texture2, uv).x;

  vec3 reflection =     1.7*diffuse
   					  + 0.3*specular
                      + 0.1*ambient;

  gl_FragColor = vec4(reflection, 1.0);
}
