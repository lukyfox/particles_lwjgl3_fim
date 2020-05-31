#version 150
in vec2 inPosition; // input from the vertex buffer
out vec2 fragCoord;
out vec3 fragParticleColor;
out float fragMousePosX;
out float fragAlpha;
out float fragTime;

uniform vec3 particleColor;
uniform float mousePosX;
uniform float alpha;
uniform mat4 view;
uniform mat4 projection;
uniform float type;
uniform float time;
uniform vec3 particlePosition;
uniform float locViewType;

const float PI = 3.1415;

//particle
vec3 getSphericalParticle(vec2 vec) {
	float x, y, z;

	float az = vec.x * PI; // <-1;1> -> <-PI;PI>
	float ze = vec.y * PI / 2.0; // <-1;1> -> <-PI/2;PI/2>
	float r = 0.02;

	x = r * cos(az) * cos(ze) + particlePosition.x/250;
	y = r * sin(az) * cos(ze) + particlePosition.y/250;
	z = r * sin(ze) + particlePosition.z / 250; // + sin(time+0.5)*2;

	return vec3(x, y, z);
}

vec3 getBlackHole(vec2 vec) {
	float x, y, z;

	float az = vec.x * PI; // <-1;1> -> <-PI;PI>
	float ze = vec.y * PI / 2.0; // <-1;1> -> <-PI/2;PI/2>
	float r = 0.05;

	x = r * cos(az) * cos(ze) + particlePosition.x/250;
	y = r * sin(az) * cos(ze) + particlePosition.y/250;
	z = r * sin(ze) + particlePosition.z/250;

	return vec3(x, y, z);
}

void main() {
	// grid máme od 0 do 1 a chceme od -1 od 1
	vec2 position = inPosition * 2.0 - 1.0;
	fragCoord = inPosition;
	fragParticleColor = particleColor;
	fragMousePosX = mousePosX;
	fragAlpha = alpha;
	fragTime = time;

	if (type == 0.0){
		fragPos4 = vec4(position, 0.0, 0.5);
	}
	if (type == 13.0) {
		fragPos4 = vec4(getSphericalParticle(position), 1.0);
	}
	if (type == 14.0) {
		fragPos4 = vec4(getBlackHole(position), 1.0);
		fragParticleColor = vec3(0f, 0f, 0f);
	}

	fragViewDirection = - (view * fragPos4).xyz;
	gl_Position = projection * view * fragPos4;


}
