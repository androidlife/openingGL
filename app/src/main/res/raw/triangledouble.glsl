#Shader Vertex
attribute vec4 coordinate;
void main() {
    gl_Position = coordinate;
}

#Shader Fragment
uniform vec4 colorValue;
void main(){
    gl_FragColor = colorValue;
}