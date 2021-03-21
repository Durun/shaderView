//#version 120
//
// simple.frag
//
uniform mat4 mat[4];
uniform sampler2D texture0;
uniform sampler2D texture1;
uniform sampler2D texture2;
uniform vec3 lightPos;
uniform vec3 lightcolor;
varying vec3 normal;
varying vec4 color;
varying vec2 texcoord;
varying vec3 viewDir;
varying vec3 lightDir;
varying vec3 pos;
varying vec3 tangent;

void main (void){
	float x = 2.0 * abs(texcoord.x-0.5);
	float y = 2.0 * abs(texcoord.y-0.5);
	float t = max(x, y);	// 0(center)..1(edge)

	float v = pow(10.0, -1000.0*(1.0-t)*(1.0-t));

	vec3 lineColor = vec3(1.0, 1.0, 1.0) - lightcolor;
	vec3 color = v*lineColor + (1.0-v)*lightcolor;
  	gl_FragColor = vec4(color.x, color.y, color.z, 1.0);
}
