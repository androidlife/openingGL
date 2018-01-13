#Shader Vertex

attribute vec4 a_position;
uniform mat4 u_matrix;
void main() {
    gl_Position = u_matrix*a_position;
}

#Shader Fragment
precision mediump float;
uniform vec4 u_color;
void main(){
    gl_FragColor = u_color;
}
