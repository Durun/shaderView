
attribute vec3 inPosition;
attribute vec4 inColor;
attribute vec3 inNormal;
attribute vec2 inTexCoord0;
attribute vec3 inTangent;
varying vec3 normal;
varying vec4 color;
varying vec2 texcoord;
varying vec3 viewDir;
varying vec3 lightDir;
uniform mat4 mat[4];
uniform vec3 lightPos;
varying vec3 pos;
varying vec3 tangent;

void main(void)
{
  normal = normalize((mat[3]*vec4(inNormal,1.0)).xyz);
  tangent = normalize((mat[3]*vec4(inTangent,1.0)).xyz);
  //tangent = normalize(cross(normal, vec3(0.0, 1.0, 0.0)));
  vec3 bitangent = cross(normal, tangent);
  vec3 position = (mat[1]*vec4(inPosition,1.0)).xyz;
  //  vec3 tmplightPos = (mat[1]*vec4(lightPos,1.0)).xyz;
  vec3 tmplightPos = lightPos;


  viewDir = -vec3(dot(position, tangent),
                 dot(position, bitangent),
                 dot(position, normal));
  vec3 tmplightDir = normalize(tmplightPos-position);
  lightDir = vec3(dot(tmplightDir, tangent),
                  dot(tmplightDir, bitangent),
                  dot(tmplightDir, normal));
  color = inColor;
  texcoord = inTexCoord0;
  gl_Position = mat[0]*mat[1]*vec4(inPosition, 1.0);
//  gl_Position = vec4(inPosition, 1.0);
//  position = gl_Position.xyz;
//  gl_Position = mat[0]*mat[1]*vec4(inPosition, 1.0);
  pos = gl_Position.xyz;
}
