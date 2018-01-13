#Shader Vertex
//this is our x,y position which we will pass from Java code
//it is a simple variable, and can be named anything
// remember it is attribute i.e. one of the property and in this case,it is point (x,y)
attribute vec4 a_position;
uniform mat4 u_matrix;
varying vec4 v_color;
attribute vec4 a_color;
void main() {
    //we are initializing or settign value to gl_Position
    //this gl_Position is variable of OpenGl which we can't modiy
    gl_Position = u_matrix * a_position;
    v_color = a_color;
}

#Shader Fragment
//seems like color should always be on uniform vec4
//I tried using attribute vec4 and it threw error while creating the shader
varying vec4 v_color;
void main(){
    gl_FragColor = v_color;
}
