#version 120

uniform float time;
uniform vec2 resolution;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;
    
    float v1 = sin(uv.x * 5.0 + time);
    float v2 = sin(5.0 * (uv.x * sin(time / 2.0) + uv.y * cos(time / 3.0)) + time);
    float v3 = sin(sqrt((uv.x - 0.5) * (uv.x - 0.5) + (uv.y - 0.5) * (uv.y - 0.5)) * 8.0 + time);
    float v = v1 + v2 + v3;
    
    vec3 color;
    color.r = sin(v * 3.14159) * 0.5 + 0.5;
    color.g = sin(v * 3.14159 + 2.094) * 0.5 + 0.5;
    color.b = sin(v * 3.14159 + 4.188) * 0.5 + 0.5;
    
    gl_FragColor = vec4(color, 1.0);
}
