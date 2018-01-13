#Shader Vertex
//this is our x,y position which we will pass from Java code
//it is a simple variable, and can be named anything
// remember it is attribute i.e. one of the property and in this case,it is point (x,y)
precision mediump float;
attribute vec4 a_Position;
varying vec4 v_Position;
void main() {
    //we are initializing or settign value to gl_Position
    //this gl_Position is variable of OpenGl which we can't modiy
    gl_Position = a_Position;
    v_Position = gl_Position;

}

#Shader Fragment
//seems like color should always be on uniform vec4
//I tried using attribute vec4 and it threw error while creating the shader
precision mediump float;
uniform vec4 u_Color;
uniform float u_Width;
varying vec4 v_Position;
void main(){

    if( v_Position.x > u_Width
            && v_Position.x < 1.0 - u_Width
            && v_Position.y > u_Width
            && v_Position.y < 0.5 - u_Width){
            gl_FragColor = vec4(0.0,1.0,0.0,1.0);
        } else {
            gl_FragColor = u_Color;
        }
}
