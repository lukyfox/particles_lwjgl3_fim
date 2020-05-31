package task2_game;

import lwjglutils.*;
import main.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL30;
import transforms.Camera;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

    private int shaderProgramViewer;

    private OGLBuffers buffers;
    // pozorovatel
    private Camera camera;


    // lokatory z pohledu pozorovatele
    private int locView, locProjection, locType, locTime;
    // lokatory pro particle
    private int locParticlePosition, locParticleColor, locViewType, locMouseX, locAlpha;

    private float time = 0;
    private OGLRenderTarget renderTarget;
    private Mat4PerspRH projection;

    private Vec3D initialViewVector;
    private int typeView = 0;
    private static int TypeViewMax = 1;

    private ParticleManager particleMan, blackHoleMan;
    private boolean attactionActive = false;
    private Particle blackHoleParticle;
    private int consumedParticles;

    public Renderer()  {
        this.particleMan = new ParticleManager(250, false);
        this.blackHoleMan = new ParticleManager(1, true);
        this.blackHoleParticle = blackHoleMan.getParticleById(1, true);
        this.consumedParticles = 0;

    }

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        // Set the clear color
        glClearColor(0.9f, 0.9f, 0.9f, 0.0f);
        textRenderer = new OGLTextRenderer(width, height);
        textRenderer.setColor(Color.DARK_GRAY);
        glEnable(GL_DEPTH_TEST); // zapne z-test (z-buffer) - až po new OGLTextRenderer

        shaderProgramViewer = ShaderUtils.loadProgram("/task02");

        // define uniform variables
        locView = glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
        locType = glGetUniformLocation(shaderProgramViewer, "type");
        locParticlePosition = glGetUniformLocation(shaderProgramViewer, "particlePosition");
        locParticleColor = glGetUniformLocation(shaderProgramViewer, "particleColor");
        locViewType = glGetUniformLocation(shaderProgramViewer, "viewType");
        locTime = glGetUniformLocation(shaderProgramViewer, "time");
        locMouseX = glGetUniformLocation(shaderProgramViewer, "mousePosX");
        locAlpha = glGetUniformLocation(shaderProgramViewer, "alpha");

        //buffers = GridFactory.generateGrid(50, 50);
        renderTarget = new OGLRenderTarget(1024, 1024);

        initialViewVector = new Vec3D(3f, 3f, 3f);
        camera = resetCamera(initialViewVector);

        projection = new Mat4PerspRH(
                Math.PI / 3,
                LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,
                1,
                20
        );

    }

    public Camera resetCamera(Vec3D vec3d) {
        return new Camera()
                .withPosition(vec3d)
                .withAzimuth(5 / 4f * Math.PI)
                .withZenith(-1 / 5f * Math.PI);
    }

    @Override
    public void display() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        time += 0.01;
        glEnable(GL_DEPTH_TEST); // zapnout z-test (kvůli textRenderer)
        buffers = GridFactory.generateGrid(100, 100);
        renderFromViewer();
    }


    private void renderFromViewer() {
        AtomicReference<Double> distance = new AtomicReference<>((double) 0);
        AtomicReference<Float> param = new AtomicReference<>((float) 0);
        glUseProgram(shaderProgramViewer);

        glViewport(0, 0, width, height);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,0);
        glClearColor(0.9f, 0.9f, 0.9f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // send typeView to vertex as uniform, will be used for colorful particles with alpha enabled
        glUniform1f(locViewType, (float)typeView);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);
        if(typeView == 1) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());
        glUniform1f(locTime, time);
        //renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);

        // draw surface
        glUniform1f(locType, 0f);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);

        // draw black hole
        glUniform1f(locType, 14f);
        Particle blackHole = blackHoleMan.getParticleById(1,true);
        Vec3D black = blackHole.getColor();
        glUniform3f(locParticleColor, (float)black.getX(), (float)black.getY(), (float)black.getZ());
        Vec3D blackHolePos = blackHole.getPosition();
        glUniform3f(locParticlePosition, (float)blackHolePos.getX(), (float)blackHolePos.getY(), (float)blackHolePos.getZ());
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);

        // draw particles and solve collision with black hole if activated
        particleMan.getActiveParticleList().forEach(particle -> {
            // draw spherical particle
            glUniform1f(locType, 13f);
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);

            // get position of black hole particle and change move direction of all particles into...
            Vec3D pos = particle.getPosition();
            if(attactionActive) {
                //Vec3D blackHolePosition = new Vec3D();
                if(blackHole.getID() != 0) {
                    Vec3D blackHolePosition = blackHole.getPosition();
                    distance.set(Math.sqrt((Math.pow(Math.abs(blackHolePosition.getX() - pos.getX()), 2) + Math.pow(Math.abs(blackHolePosition.getY() - pos.getY()), 2)) + (Math.pow(Math.abs(blackHolePosition.getX() - pos.getX()), 2))));
                    Vec3D newVelocity = new Vec3D(blackHolePosition.getX()-pos.getX(), blackHolePosition.getY()-pos.getY(), blackHolePosition.getZ()-pos.getZ());
                    // with shorter distance between particke and black hole the power of attraction is growing a little
                    param.set((float) (4.0/distance.get()));
                    float paramX =(float) (4.0/distance.get());
                    float paramY = (float) (4.0/distance.get());
                    float paramZ = (float) (4.0/distance.get());
                    // change direction to black hole destination and update speed of particle
                    particle.changeDirectionVelocityXYZ(newVelocity, paramX, paramY, paramZ);
                    // particle is destroyed when gets too close - basic step is 10pts, so 5pts as limit is sufficient and solves some trouble with distance rounding (never-ending fall to singularity)
                    if(distance.get()<=5f) {
                        //System.out.println("particle destroyed");
                        particle.setTtl(0);
                        consumedParticles++;
                    }
                }
            }

            // when particle has less that 25% ttl, then invert and slow down move along z-axis
            float ttl = (float)particle.getTtl() / (float)particle.getDuration();
            if(!attactionActive && ttl < 0.25f && particle.getVelocity().getZ() > 0) {
                particle.changeDirectionVelocityZ(-0.5f);
            }

            glUniform3f(locParticlePosition, (float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);

            // board color (depends on locMoveType and mouse X position)
            glUniform1f(locMouseX, (float)oldMx/(float)width);
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);

            Vec3D col = particle.getColor();
            glUniform3f(locParticleColor, (float)col.getX(), (float)col.getY(), (float)col.getZ());
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);
            glUniform1f(locAlpha, (float)particle.getTtl() / (float)particle.getDuration());
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);


            // swarm dance :)
            //glUniform1f(locMoveType, (float)typeView);
            //buffers.draw(GL_TRIANGLES, shaderProgramViewer);


        });
        // update or deactivate particles
        particleMan.updateParticleList();


        // create and draw text
        String sceneDescription;
        String text = "window size = " + width + 'x' + height;
        text += ". Click right mouse button to switch modes. Use WSAD, arrow up/down to move and Home to reset position.";
        textRenderer.clear();
        textRenderer.addStr2D(10, 20, text);
        textRenderer.setColor(Color.DARK_GRAY);
        if(typeView == 0) {sceneDescription = "Plane and Particles - coordinates to color, move mouse horizontally to change color";}
        else if(typeView == 1) {sceneDescription = "Plane and colorful Particles";}
        else {sceneDescription = "UNSUPPORTED MODE! (Forgotten TODO?)";}

        //Vec3D blackHolePos = blackHoleParticle.getPosition();
        String temp = " Black hole position: (" + blackHolePos.getX() + ", " + blackHolePos.getY() + ", " + blackHolePos.getZ() + "), distance: " + distance;
        text = "Display mode = " + typeView + " / " + TypeViewMax + ": " + sceneDescription + temp;
        textRenderer.addStr2D(10, 40, text);

        text = "Consumed particles = " + consumedParticles + ", " + param.get();
        textRenderer.addStr2D(10, 60, text);


        textRenderer.draw();
    }


    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private double oldMx, oldMy;
    private boolean mousePressed;

    private GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI / 2 * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI / 2 * (oldMy - y) / LwjglWindow.HEIGHT);
            }
            oldMx = x;
            oldMy = y;
        }
    };

    private GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            mousePressed = action == GLFW_PRESS;
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                System.out.println(xPos[0] + ", " + yPos[0]);
            }
            if (button == GLFW_MOUSE_BUTTON_RIGHT && mousePressed) {
                if (typeView < TypeViewMax) {typeView += 1;}
                else {typeView = 0;}

            }
        }
    };

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {

            Vec3D blackHolePos = blackHoleParticle.getPosition();
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W:
                        camera = camera.forward(0.1);
                        break;
                    case GLFW_KEY_S:
                        camera = camera.backward(0.1);
                        break;
                    case GLFW_KEY_A:
                        camera = camera.left(0.1);
                        break;
                    case GLFW_KEY_D:
                        camera = camera.right(0.1);
                        break;
                    case GLFW_KEY_UP:
                        camera = camera.up(0.1);
                        break;
                    case GLFW_KEY_DOWN:
                        camera = camera.down(0.1);
                        break;

                    // black hole movements
                    case GLFW_KEY_KP_8:
                        if(blackHolePos.getY() > -500f) {
                            blackHoleParticle.setPosition(new Vec3D(blackHolePos.getX(), blackHolePos.getY() - 10f, blackHolePos.getZ()));
                        }
                        break;
                    case GLFW_KEY_KP_2:
                        if(blackHolePos.getY() < 500f) {
                            blackHoleParticle.setPosition(new Vec3D(blackHolePos.getX(), blackHolePos.getY() + 10f, blackHolePos.getZ()));
                        }
                        break;
                    case GLFW_KEY_KP_6:
                        if(blackHolePos.getX() > -500f) {
                            blackHoleParticle.setPosition(new Vec3D(blackHolePos.getX() - 10f, blackHolePos.getY(), blackHolePos.getZ()));
                        }
                        break;
                    case GLFW_KEY_KP_4:
                        if(blackHolePos.getX() < 500f) {
                            blackHoleParticle.setPosition(new Vec3D(blackHolePos.getX() + 10f, blackHolePos.getY(), blackHolePos.getZ()));
                        }
                        break;
                    case GLFW_KEY_KP_3:
                        float step = 10f;
                        if(blackHolePos.getZ() - step <= 2f) { step = (float) blackHolePos.getZ() - 2f;}
                        if(blackHolePos.getZ() >= 2f) {
                            blackHoleParticle.setPosition(new Vec3D(blackHolePos.getX(), blackHolePos.getY(), blackHolePos.getZ() - step));
                        }
                        break;
                    case GLFW_KEY_KP_9:
                        if(blackHolePos.getZ() < 500f) {
                            step = 10f;
                            if(blackHolePos.getZ() + step >= 500) { step = 500f - (float) blackHolePos.getZ();}
                            blackHoleParticle.setPosition(new Vec3D(blackHolePos.getX(), blackHolePos.getY(), blackHolePos.getZ() + step));
                        }
                        break;

                        case GLFW_KEY_KP_5:
                        // press 5 o numerical keyboard to activate attraction
                            if(attactionActive) {
                                attactionActive = false;
                            }
                            else {
                                attactionActive = true;
                            }
                        break;

                        //reset view
                    case GLFW_KEY_HOME:
                        camera = resetCamera(initialViewVector);
                        break;

                }
            }
        }
    };

}
