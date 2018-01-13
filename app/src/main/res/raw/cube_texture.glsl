#Shader Vertex
//this is our x,y position which we will pass from Java code
//it is a simple variable, and can be named anything
// remember it is attribute i.e. one of the property and in this case,it is point (x,y)
attribute vec4 a_position;
uniform mat4 u_matrix;
attribute vec2 a_texturePos;
varying vec2 v_texturePos;
void main() {
    //we are initializing or settign value to gl_Position
    //this gl_Position is variable of OpenGl which we can't modiy
    gl_Position = u_matrix*a_position;
    v_texturePos = a_texturePos;

}

#Shader Fragment
//seems like color should always be on uniform vec4
//I tried using attribute vec4 and it threw error while creating the shader
varying vec2 v_texturePos;
uniform sampler2D u_textureUnit;
void main(){
    gl_FragColor = texture2D(u_textureUnit,vec2((v_texturePos.x-1.0)*(-1.0),(v_texturePos.y-1.0))*(-1.0));
}
