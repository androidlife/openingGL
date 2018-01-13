#Shader Vertex
attribute vec4 a_position;
uniform mat4 u_matrix;

attribute vec2 a_textureCoordinate;
varying vec2 v_textureCoordinate;
void main() {
    v_textureCoordinate = a_textureCoordinate;
    gl_Position = u_matrix * a_position;
    //gl_Position = a_position;
}

#Shader Fragment
varying vec2 v_textureCoordinate;
uniform sampler2D u_textureUnit;
void main(){
  gl_FragColor = texture2D(u_textureUnit,vec2((v_textureCoordinate.x-1.0)*(-1.0),(v_textureCoordinate.y-1.0)*(-1.0)));
}

