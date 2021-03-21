package shaderView

import com.jogamp.newt.event.KeyEvent
import com.jogamp.newt.event.KeyListener
import com.jogamp.newt.event.MouseEvent
import com.jogamp.newt.event.MouseListener
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.util.PMVMatrix
import shaderView.data.*
import shaderView.render.Object3D
import shaderView.render.Shader
import shaderView.render.textured
import java.nio.file.Path

fun main() {
    val app = AppBase("shader", 960, 720)
    app.start(AppListener(app.window))
}

class AppListener(private val window: GLWindow) : GLEventListener {
    companion object {
        val width = 480
        val height = 320
        val titles = listOf(
            "Wireframe",
            "BRDF",
            "BRDF + Parallax occlusion mapping"
        )
        val lightPos = Vec3(0.0f, 0.0f, 10f)
        val lightcolor = Vec3(1.0000f, 0.9434f, 0.9927f) // D65 light
    }

    val shaders: MutableList<Shader> = mutableListOf()
    val objects: MutableList<Object3D> = mutableListOf()

    private val mats = PMVMatrix()
    var mouseX: Int = width / 2
    var mouseY: Int = height / 2


    override fun init(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2ES2
        gl.glViewport(0, 0, width, height)

        // Clear color buffer with black
        gl.apply {
            lightcolor.let { (r, g, b) -> glClearColor(r, g, b, 1f) }
            glClearDepth(1.0)
            glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
            glEnable(GL2.GL_DEPTH_TEST)
            glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1)
            glFrontFace(GL.GL_CCW)
            glEnable(GL.GL_CULL_FACE)
            glCullFace(GL.GL_BACK)
        }

        shaders.apply {
            val vertexShader = Path.of("app/src/main/resources/vertex.vert")
            add(Shader(gl, vertexShader, Path.of("app/src/main/resources/edge.frag")))
            add(Shader(gl, vertexShader, Path.of("app/src/main/resources/brick.frag")))
        }

        val brickNormal = loadFileTexture(Path.of("app/src/main/resources/brick_n.png"))
        val brickDiffuse = loadFileTexture(Path.of("app/src/main/resources/brick.png"))
        val brickHeight = loadFileTexture(Path.of("app/src/main/resources/brick_h.png"))
        val brickTextures = listOf(brickNormal, brickDiffuse, brickHeight)


        val brickColor = Vec4(0.4f, 0.2f, 0.2f, 1f)
        objects.add(makePlane(1.3f, color = brickColor).textured(gl, brickTextures, shaders[0]))
        objects.add(makePlane(1.3f, color = brickColor).textured(gl, brickTextures, shaders[1]))

        gl.glUseProgram(0)
    }

    override fun display(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2ES2

        gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)

        mats.glMatrixMode(GL2.GL_PROJECTION)
        mats.glLoadIdentity()
        mats.glFrustumf(-0.5f, 0.5f, -0.5f, 0.5f, 1f, 100f)
        mats.glMatrixMode(GL2.GL_MODELVIEW)
        mats.glLoadIdentity()
        mats.glTranslatef(0f, 0f, -4.0f)

        objects[select].displayAt(gl, mats, lightPos, lightcolor) {
            glRotatef((mouseY - height / 2).toFloat() + 45, 1f, 0f, 0f)
            glRotatef((mouseX - width / 2).toFloat(), 0f, 1f, 0f)
            glRotatef(90f, 1f, 0f, 0f)
            glRotatef(45f, 0f, 0f, 1f)
        }
        window.title = titles.getOrNull(select)

        gl.glFlush()
    }

    override fun dispose(drawable: GLAutoDrawable?) {}
    override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {}


    var select = 0

    val mouseListener: MouseListener = AppMouseListener()

    inner class AppMouseListener : MouseListener {
        override fun mouseDragged(e: MouseEvent) {
            mouseX = e.x
            mouseY = e.y
        }

        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseClicked(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseMoved(e: MouseEvent?) {}
        override fun mouseWheelMoved(e: MouseEvent?) {}
    }

    val keyListener: KeyListener = AppKeyListener()

    inner class AppKeyListener : KeyListener {
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_LEFT -> {
                    select = (select + objects.size - 1) % objects.size
                }
                KeyEvent.VK_RIGHT -> {
                    select = (select + 1) % objects.size
                }
            }
        }

        override fun keyReleased(e: KeyEvent?) {}
    }
}
