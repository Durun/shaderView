//#version 120
//
// simple.frag
//
uniform mat4 mat[4];
uniform sampler2D texture0;
uniform vec3 lightpos;
uniform vec3 lightcolor;
varying vec3 normal;
varying vec4 color;
varying vec2 texcoord;
varying vec3 viewdir;
varying vec3 lightdir;
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

void main (void){
  // view vector
  vec3 v = normalize(viewdir);

  // bump mapping
  vec3 bump = (texture2D(texture0, texcoord)*2.0-1.0).xyz;

  // vector parameters
  vec3 l = normalize(lightpos - pos);	// Spot light
  vec3 n = normalize(normal + 0.2*bump);
  vec3 h = (l+v)/2.0;

  // color parameters
  vec3 diffuseColor = color.xyz;
  float roughness = 0.45;

  // reflections
  vec3 diffuse = OrenNayar(n,l,v, roughness) *diffuseColor*lightcolor;
  vec3 specular = F_Schlick(lightcolor, l, h) * V_JointGGX(n,l,v, roughness) * D_GGX(n,h, roughness);
  vec3 ambient = diffuseColor*lightcolor;

  vec3 reflection =   0.9*diffuse
  					+ 0.02*specular
                    + 0.05*ambient;

  gl_FragColor = vec4(reflection,1.0);
}
