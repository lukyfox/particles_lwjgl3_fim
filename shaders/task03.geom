#version 450
#extension GL_EXT_geometry_shader4 : enable

layout(triangles) in;
layout(triangle_strip, max_vertices = 6000) out;

in vec2 geomCoord[];
in vec4 geomPos4[];
in vec3 geomViewDirection[];
in float geomTexType[];
in vec3 geomParticleColor[];
in float geomMousePosX[];
in float geomAlpha[];
in float geomTime[];

out vec2 coord;
out vec4 pos4;
out vec3 viewDirection;
out float texType;
out vec3 fragParticleColor;
out float fragMousePosX;
out float fragAlpha;
out float fragTime;

void main() {

    //for(int i = 0; i < gl_VerticesIn; i++) {
    coord = geomCoord[0];
    EmitVertex();
    coord = geomCoord[1];
    EmitVertex();
    coord = geomCoord[2];
    EmitVertex();

    pos4 = geomPos4[0];
    EmitVertex();
    pos4 = geomPos4[1];
    EmitVertex();
    pos4 = geomPos4[2];
    EmitVertex();

    viewDirection = geomViewDirection[0];
    EmitVertex();
    viewDirection = geomViewDirection[1];
    EmitVertex();
    viewDirection = geomViewDirection[2];
    EmitVertex();

    texType = geomTexType[0];
    EmitVertex();
    texType = geomTexType[1];
    EmitVertex();
    texType = geomTexType[2];
    EmitVertex();

    fragParticleColor = geomParticleColor[0];
    EmitVertex();
    fragParticleColor = geomParticleColor[1];
    EmitVertex();
    fragParticleColor = geomParticleColor[2];
    EmitVertex();

    fragMousePosX = geomMousePosX[0];
    EmitVertex();
    fragMousePosX = geomMousePosX[1];
    EmitVertex();
    fragMousePosX = geomMousePosX[2];
    EmitVertex();

    fragAlpha = geomAlpha[0];
    EmitVertex();
    fragAlpha = geomAlpha[1];
    EmitVertex();
    fragAlpha = geomAlpha[2];
    EmitVertex();

    fragTime = geomTime[0];
    EmitVertex();
    fragTime = geomTime[1];
    EmitVertex();
    fragTime = geomTime[2];
    EmitVertex();

    EndPrimitive();
    //}
}
