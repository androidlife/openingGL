#Shader Vertex
//this is our x,y position which we will pass from Java code
//it is a simple variable, and can be named anything
// remember it is attribute i.e. one of the property and in this case,it is point (x,y)
attribute vec4 points;
attribute vec4 color;
varying vec4 colorVary;
void main() {
    //we are initializing or settign value to gl_Position
    //this gl_Position is variable of OpenGl which we can't modiy
    gl_Position = points;
    colorVary = color;
}

#Shader Fragment
//we are passing this value for VERTEX through color
// so my guess right now is that the color value gets some value
// in our code in java
// this value is reflected in colorVary which is used for pixel coloring
//Q) Why we need to define in two parts, one in  VERTEX and one in Shader
varying vec4 colorVary;
void main(){
    gl_FragColor = colorVary;
}
