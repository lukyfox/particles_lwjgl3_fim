package task2;

import lwjglutils.*;
import main.AbstractRenderer;
import main.GridFactory;
import main.LwjglWindow;
import main.ParticleManager;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL30;
import transforms.Camera;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.awt.*;

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
    private int locParticlePosition, locParticleColor, locMoveType, locMouseX, locAlpha;

    private float time = 0;
    private OGLRenderTarget renderTarget;
    private Mat4PerspRH projection;

    private Vec3D initialViewVector;
    private int typeView = 0;
    private static int TypeViewMax = 1;

    private ParticleManager particleMan;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        this.particleMan = new ParticleManager();

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
        locMoveType = glGetUniformLocation(shaderProgramViewer, "swarmDance");
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
        buffers = GridFactory.generateGrid(40, 30);
        renderFromViewer();
    }


    private void renderFromViewer() {
        glUseProgram(shaderProgramViewer);

        glViewport(0, 0, width, height);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,0);
        glClearColor(0.9f, 0.9f, 0.9f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());
        glUniform1f(locTime, time);
        //renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUniform1f(locType, 0f);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);

        particleMan.getActiveParticleList().forEach(particle -> {
            // draw spherical particle
            glUniform1f(locType, 13f);
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);

            // solve position of particle (each particle holds its own position)
            // when particle has less that 25% ttl, then invert and slow down move along z-axis
            float ttl = (float)particle.getTtl() / (float)particle.getDuration();
            if(ttl < 0.25f && particle.getVelocity().getZ() > 0) {
                particle.changeDirectionVelocityZ(-0.5f);
            }
            Vec3D pos = particle.getPosition();
            glUniform3f(locParticlePosition, (float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);

            // draw color (depends on locMoveType and mouse X position)
            glUniform1f(locMouseX, (float)oldMx/(float)width);
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);
            Vec3D col = particle.getColor();
            glUniform3f(locParticleColor, (float)col.getX(), (float)col.getY(), (float)col.getZ());
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);
            glUniform1f(locAlpha, (float)particle.getTtl() / (float)particle.getDuration());
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);


            // swarm dance :)
            glUniform1f(locMoveType, (float)typeView);
            buffers.draw(GL_TRIANGLES, shaderProgramViewer);


        });
        // update or deactivate particles
        particleMan.updateParticleList();


        // create and draw text
        String sceneDescription = "";
        String text = "window size = " + width + 'x' + height;
        text += ". Click right mouse button to switch modes. Use WSAD, arrow up/down to move and Home to reset position.";
        textRenderer.clear();
        textRenderer.addStr2D(10, 20, text);
        textRenderer.setColor(Color.DARK_GRAY);
        if(typeView == 0) {sceneDescription = "Plane and Particles - coordinates to color, move mouse horizontally to change color";}
        else if(typeView == 1) {sceneDescription = "Plane and colorful Particles";}
        else {sceneDescription = "UNSUPPORTED MODE! (Forgotten TODO?)";}

        text = "Display mode = " + typeView + " / " + TypeViewMax + ": " + sceneDescription;
        textRenderer.addStr2D(10, 40, text);

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
                    //reset view
                    case GLFW_KEY_HOME:
                        camera = resetCamera(initialViewVector);
                        break;

                }
            }
        }
    };

}
