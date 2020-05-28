#version 150

in vec2 coord;
in float texType;

uniform float type;
uniform vec3 particleColor;
uniform float mousePosX;
uniform float alpha;
uniform float time;

out vec4 outColor; // (vždy jediný) výstup z fragment shaderu

void main() {

	vec4 simpleColor = vec4(0.2, 0.7, 0.5, 0.5);

	if(texType == 0.0) {
		// texType is transported from vertex shader
		outColor = vec4(coord, 0f, 1.0);
		if(type == 13.0) {
			outColor.z = outColor.y * mousePosX;

		}
	}
	else {
		if(type != 13.0) {
			outColor = simpleColor;//vec4(coord, 0f, 1.0);
		}
		else {
			// set colorful particles, each particle hold it's color, color may change during lifetime
			outColor = vec4(particleColor, 1.0);
		}
	}
}
