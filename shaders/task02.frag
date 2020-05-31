#version 150

in vec2 fragCoord;
in float fragTexType;
in float fragLocViewType;

uniform float type;
uniform float locViewType;

in vec3 fragParticleColor;
in float fragMousePosX;
in float fragAlpha;
in float fragTime;

out vec4 outColor; // (vždy jediný) výstup z fragment shaderu

void main() {
	vec4 simpleColor = vec4(0.5, 0.75, 0.95, 1.0);

	if(type == 0.0) {
		outColor = simpleColor;
		// change color with horizontal moving the mouse, but board shoudn't be too dark to see black hole
		if(fragMousePosX > 0.2f)
			outColor.xyz = outColor.xyz * fragMousePosX;
		else
			outColor.xyz = outColor.xyz * 0.2f;
	}
	if(type == 13.0) {
		// particles with mapped coordination
		outColor = vec4(fragCoord, 0f, 1.0);
	}
	if (type == 14.0) {
		// black hole shoudl be black
		outColor = vec4(fragParticleColor, 1.0);
	}

	/*
	if(texType == 0.0) {
		// texType is transported from vertex shader
		outColor = vec4(fragCoord, 0f, 1.0);
		if(type == 13.0) {
			outColor.z = outColor.y * fragMousePosX;
		}
	}
	else {
		if(type != 13.0) {
			outColor = simpleColor;//vec4(coord, 0f, 1.0);
		}
		else {
			// set colorful particles, each particle hold it's color, color may change during lifetime
			outColor = vec4(fragParticleColor, fragAlpha);
		}
	}*/
}
