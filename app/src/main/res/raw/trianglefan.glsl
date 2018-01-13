#Shader Vertex
attribute vec4 coordinate;
attribute vec4 colorValue;
varying vec4 colorValueVarying;
void main() {
    colorValueVarying = colorValue;
    gl_Position = coordinate;
}

#Shader Fragment
precision mediump float;
//this comes from vertex shader value
varying vec4 colorValueVarying;
void main() {
 gl_FragColor = colorValueVarying;
}
