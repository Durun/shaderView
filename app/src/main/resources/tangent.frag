
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
  vec3 bitangent = cross(normal, tangent);

  vec3 view = -vec3(dot(pos, tangent),
                   dot(pos, bitangent),
                   dot(pos, normal));

  gl_FragColor = vec4(viewDir/2.0+0.5 , 1.0);
}
