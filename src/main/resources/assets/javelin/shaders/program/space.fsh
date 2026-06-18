#version 120

uniform float time;
uniform vec2 resolution;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;
    
    // Stars
    float stars = 0.0;
    for (int i = 0; i < 5; i++) {
        vec2 p = fract(uv * (10.0 + float(i) * 7.0) + float(i) * 0.1);
        float d = length(p - 0.5);
        float brightness = smoothstep(0.02, 0.0, d);
        float twinkle = sin(time * (1.5 + float(i) * 0.5) + float(i) * 3.14) * 0.5 + 0.5;
        stars += brightness * twinkle;
    }
    
    // Nebula gradient
    float nebula = sin(uv.x * 3.0 + time * 0.2) * cos(uv.y * 2.0 + time * 0.15);
    nebula = nebula * 0.5 + 0.5;
    
    vec3 skyColor = vec3(0.02, 0.01, 0.05);
    vec3 nebulaColor = mix(vec3(0.1, 0.0, 0.2), vec3(0.0, 0.1, 0.3), nebula);
    vec3 starColor = vec3(1.0, 0.95, 0.8);
    
    vec3 color = skyColor + nebulaColor * 0.3 + starColor * stars;
    
    gl_FragColor = vec4(color, 1.0);
}
