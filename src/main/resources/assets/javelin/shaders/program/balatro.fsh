#version 120

uniform float time;
uniform vec2 resolution;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;
    vec2 p = uv * 2.0 - 1.0;
    p.x *= resolution.x / resolution.y;
    
    // Balatro-style psychedelic pattern
    float angle = atan(p.y, p.x);
    float radius = length(p);
    
    float spiral = sin(angle * 6.0 + radius * 10.0 - time * 2.0);
    float rings = sin(radius * 15.0 - time * 3.0);
    float wave = sin(p.x * 8.0 + time) * cos(p.y * 8.0 + time);
    
    float pattern = spiral * 0.4 + rings * 0.3 + wave * 0.3;
    pattern = pattern * 0.5 + 0.5;
    
    // Red/purple palette (Balatro style)
    vec3 col1 = vec3(0.8, 0.1, 0.2);  // Red
    vec3 col2 = vec3(0.3, 0.0, 0.5);  // Purple
    vec3 col3 = vec3(0.1, 0.05, 0.15); // Dark
    
    vec3 color = mix(col3, mix(col2, col1, pattern), smoothstep(0.2, 0.8, pattern));
    
    // Vignette
    float vignette = 1.0 - radius * 0.5;
    color *= vignette;
    
    gl_FragColor = vec4(color, 1.0);
}
