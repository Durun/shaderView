//#version 120
//
// simple.vert
//
//invariant gl_Position; // invariant out gl_Position; //for #version 130
attribute vec3 inPosition;//in vec3 position;          //for #version 130
attribute vec4 inColor;
attribute vec3 inNormal;
attribute vec2 inTexCoord0;
varying vec3 normal;
varying vec4 color;
varying vec2 texcoord;
uniform mat4 mat[4];

void main(void)
{
  normal = normalize((mat[3]*vec4(inNormal,1.0)).xyz);
  color = inColor;
  texcoord = inTexCoord0;
  gl_Position = mat[0]*mat[1]*vec4(inPosition, 1.0);
//  gl_Position = vec4(inPosition, 1.0);

}