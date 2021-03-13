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

vec3 sampleBump(sampler2D F, vec2 v) {
	return (texture2D(F, v)*2.0-1.0).xyz;
}
float sampleHeight(vec2 t) {
	return texture2D(texture2, t).x;
}

vec2 getUV_BinarySearch(vec2 texCoord, vec3 view) {
	float height = sampleHeight(texcoord);
	int resolution = 64;
	float maxdepth = 0.03;
	vec3 dv = maxdepth * vec3(view.x, -view.y, abs(view.z)) * abs(view.z);

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
	float d = 0.003;
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
  vec2 uv = getUV_BinarySearch(texcoord, v);
  vec3 bump = sampleBump(texture0, uv).xyz;

  // vector parameters
  vec3 l = normalize(lightpos - pos);	// Spot light
  vec3 n = normalize(normal + 8.0*bump);
  vec3 h = (l+v)/2.0;
  float nh = max(dot(n, h),0.0);

  // color parameters
  vec2 dfdx = abs(dFdx(texture0, uv).xy);
  vec2 dfdy = abs(dFdy(texture0, uv).xy);
  float d = max(max(dfdx.x,dfdx.y),max(dfdy.x,dfdy.y));
  float wet = 1.0-d*0.01;
  float brightness = texture2D(texture1, uv).x * 0.7 + 0.3;

  // specular parameters
  float ks1 = pow(nh,40.1) * brightness;
  float ks2 = pow(nh,150.1) * (1.0-wet);

  // reflections
  vec3 diffuse = color.xyz * brightness;
  vec3 specular = lightcolor;
  vec3 ambient = diffuse*lightcolor * brightness;

  vec3 reflection =     0.95*max(dot(l,n),0.0) *diffuse*lightcolor
    				  + 0.3 *ks1 * specular
    				  + 1.0 *ks2 * specular
                      + 0.15*ambient;

  gl_FragColor = vec4(reflection, 1.0);
}
