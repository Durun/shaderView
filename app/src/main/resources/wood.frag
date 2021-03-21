
uniform mat4 mat[4];
uniform sampler2D texture0;
uniform sampler2D texture1;
uniform sampler2D texture2;
uniform vec3 lightPos;
uniform vec3 lightcolor;
varying vec3 normal;
varying vec2 texcoord;
varying vec3 viewDir;
varying vec3 lightDir;
varying vec3 pos;

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

vec3 sampleColor(vec2 t) {
	return texture2D(texture0, t).xyz;
}
vec3 sampleBump(vec2 t) {
	vec4 v = texture2D(texture1, t)*2.0-1.0;
	return vec3(-v.x, -v.y, v.z);
}
float sampleHeight(vec2 t) {
	return texture2D(texture2, t).x;
}
float sampleMetallic(vec2 t) {
	return texture2D(texture2, t).y;
}
float sampleRoughness(vec2 t) {
	return texture2D(texture2, t).z;
}

vec2 getUV_LinearSearch(vec2 texCoord, vec3 view) {
	float height = sampleHeight(texcoord);
	int resolution = 128;
	float maxdepth = 0.08;
	vec3 dv = maxdepth * vec3(view.x, -view.y, abs(view.z)) * abs(view.z) / float(resolution);

	vec3 v = vec3(0.0, 0.0, 0.0);
	for (int i = 0; i < resolution; i++) {
	  v = v + dv; // v = dv*(i+1)
	  vec2 t = texCoord + v.xy;
	  float hRay = 1.0 - v.z;
	  float hObj = sampleHeight(t);
	  if (hRay < hObj) break;
	}
	return texCoord + v.xy;
}


void main (void){
  // view vector
  vec3 v = normalize(viewDir);

  // parallax mapping
  vec2 uv = getUV_LinearSearch(texcoord, v);
  vec3 bump = sampleBump(uv);

  // vector parameters
  vec3 l = normalize(lightPos - pos);	// Spot light
  vec3 n = normalize(normal + 8.0*bump);
  vec3 h = (l+v)/2.0;

  // color parameters
  float metalic = sampleMetallic(uv);
  float smoothness = 1.0 - sampleRoughness(uv);
  float roughness = 1.0 - smoothness*(metalic*0.2 + 0.8);
  vec3 metalColor = vec3(1.0, 0.8, 0.1);
  vec3 specularColor = (1.0-metalic)*lightcolor + metalic*metalColor;
  vec3 diffuseColor = sampleColor(uv);

  // reflections
  vec3 diffuse = OrenNayar(n,l,v, roughness) *diffuseColor*lightcolor;
  vec3 specular = F_Schlick(specularColor, l, h) * V_JointGGX(n,l,v, roughness) * D_GGX(n,h, roughness);
  vec3 ambient = diffuse*lightcolor;

  vec3 reflection =     diffuse
    				  + (metalic+0.3)*specular
                      + 0.15*ambient;

  gl_FragColor = vec4(reflection, 1.0);
}
