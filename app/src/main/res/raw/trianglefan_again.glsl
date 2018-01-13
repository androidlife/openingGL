#Shader Vertex
attribute vec4 a_Position;
attribute vec4 a_Color;
varying vec4 v_Color;
uniform mat4 u_Matrix;
void main() {

   //gl_Position = u_Matrix*a_Position;
   gl_Position = a_Position;
   v_Color = a_Color;
}

#Shader Fragment
precision mediump float;
//this comes from vertex shader value
varying vec4 v_Color;
void main() {
 gl_FragColor = v_Color;
}
