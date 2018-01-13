#Shader Vertex
attribute vec4 a_position;
uniform mat4 u_matrix;
attribute vec4 a_color;
varying vec4 v_color;
void main() {
  gl_Position = u_matrix*a_position;
  v_color = a_color;
}
#Shader Fragment
//seems like defining precision is also important on VR
precision mediump float;
varying vec4 v_color;
void main(){
 gl_FragColor = v_color;
}
