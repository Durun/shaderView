
uniform mat4 mat[4];
uniform sampler2D texture0;
uniform sampler2D texture1;
uniform vec3 lightPos;
uniform vec3 lightcolor;
varying vec3 normal;
varying vec4 color;
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

float G1_Shlick(vec3 n, vec3 v, float k) {
  float nv = dot(n,v);
  return nv / (nv*(1.0-k) + k);
}
float G_Smith(vec3 n, vec3 l, vec3 v, float roughness) {
  float k = roughness * PIrt;
  return G1_Shlick(n,l,k) * G1_Shlick(n,v,k);
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
	return texture2D(texture1, t).x;
}

vec2 getUV_LinearSearch(vec2 texCoord, vec3 view) {
	float height = sampleHeight(texcoord);
	int resolution = 64;
	float maxdepth = 0.05;
	vec3 dv = maxdepth * vec3(view.x, -view.y, view.z) / float(resolution);

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
  float texscale = 1.0;
  vec2 uv = getUV_LinearSearch(texcoord/texscale, v);
  vec3 bump = sampleBump(texture0, uv);

  // vector parameters
  vec3 l = normalize(lightPos - pos);	// Spot light
  vec3 n = normalize(normal+0.8*bump);
  vec3 h = (l+v)/2.0;

  // color parameters
  vec3 diffuseColor = color.xyz;
  vec3 specularColor = vec3(0.60, 0.33, 0.27);
  float roughness = 0.5;

  // reflections
  vec3 diffuse = OrenNayar(n,l,v, roughness) *diffuseColor*lightcolor;
  vec3 specular = F_Schlick(specularColor, l, h) * G_Smith(n,l,v, roughness) / (4.0*dot(l,n)*dot(v,n)) * D_GGX(n,h, roughness);
  vec3 ambient = diffuseColor*lightcolor;

  vec3 reflection =   0.2*diffuse
  					+ 6.0*specular
  					+ 0.05*ambient;
  gl_FragColor = vec4(reflection, 1.0);
}
