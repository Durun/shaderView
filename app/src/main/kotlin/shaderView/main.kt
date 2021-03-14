package shaderView

import com.jogamp.newt.event.KeyEvent
import com.jogamp.newt.event.KeyListener
import com.jogamp.newt.event.MouseEvent
import com.jogamp.newt.event.MouseListener
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
	AppBase("shader", 480, 320, AppListener()).start()
}

class AppListener : GLEventListener {
	companion object {
		val width = 480
		val height = 320
		val bgColor = Vec4(0.5f, 0.5f, 0.5f, 1f)
	}

	val shaders: MutableList<Shader> = mutableListOf()
	val objects: MutableList<Object3D> = mutableListOf()

	val mats = PMVMatrix()
	var t = 0f
	var mouseX: Int = width / 2
	var mouseY: Int = height / 2

	val lightpos = Vec3(0.0f, 0.0f, 10f)
	val lightcolor = Vec3(1.0000f, 0.9434f, 0.9927f) // D65 light

	override fun init(drawable: GLAutoDrawable) {
		val gl = drawable.gl.gL2ES2
		gl.glViewport(0, 0, width, height)

		// Clear color buffer with black
		gl.apply {
			bgColor.let { (r, g, b, a) -> glClearColor(r, g, b, a) }
			glClearDepth(1.0)
			glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
			glEnable(GL2.GL_DEPTH_TEST)
			glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1)
			glFrontFace(GL.GL_CCW)
			glEnable(GL.GL_CULL_FACE)
			glCullFace(GL.GL_BACK)
		}

		shaders.apply {
			val shader0 = Shader(
				gl,
				Path.of("app/src/main/resources/simple.vert"),
				Path.of("app/src/main/resources/simple.frag")
			)
			add(shader0)

			val shader1 = Shader(
				gl,
				Path.of("app/src/main/resources/vertex.vert"),
				Path.of("app/src/main/resources/brick.frag")
			)
			add(shader1)

			val shader2 = Shader(
				gl,
				Path.of("app/src/main/resources/vertex.vert"),
				Path.of("app/src/main/resources/tangent.frag")
			)
			add(shader2)

			add(
				Shader(
					gl,
					Path.of("app/src/main/resources/vertex.vert"),
					Path.of("app/src/main/resources/sofa.frag")
				)
			)

			add(
				Shader(
					gl,
					Path.of("app/src/main/resources/vertex.vert"),
					Path.of("app/src/main/resources/copper.frag")
				)
			)

			add(
				Shader(
					gl,
					Path.of("app/src/main/resources/vertex.vert"),
					Path.of("app/src/main/resources/wood.frag")
				)
			)
		}

		val brickNormal = loadFileTexture(Path.of("app/src/main/resources/brick_n.png"))
		val brickDiffuse = loadFileTexture(Path.of("app/src/main/resources/brick.png"))
		val brickHeight = loadFileTexture(Path.of("app/src/main/resources/brick_h.png"))
		val brickTextures = listOf(brickNormal, brickDiffuse, brickHeight)

		val rockDiffuse = loadFileTexture(Path.of("app/src/main/resources/rock_d.png"))
		val rockHeight = loadFileTexture(Path.of("app/src/main/resources/rock_h.png"))
		val rockTextures = listOf(brickNormal, rockDiffuse, brickHeight)

		val sofa = listOf(
			loadFileTexture(Path.of("app/src/main/resources/sofa_d.png")),
			loadFileTexture(Path.of("app/src/main/resources/sofa_n.png")),
			loadFileTexture(Path.of("app/src/main/resources/sofa_hmr.png"))
		)

		val copper = listOf(
			loadFileTexture(Path.of("app/src/main/resources/10yen_n.png")),
			loadFileTexture(Path.of("app/src/main/resources/10yen_h.png"))
		)

		val wood = listOf(
			loadFileTexture(Path.of("app/src/main/resources/wood_d.png")),
			loadFileTexture(Path.of("app/src/main/resources/wood_n.png")),
			loadFileTexture(Path.of("app/src/main/resources/wood_hmr.png"))
		)

		val plane3 = makePlane(1.3f).textured(gl, rockTextures, shaders[1])

		objects.add(makePlane(1.3f, color = Vec4(0.4f, 0.2f, 0.2f, 1f)).textured(gl, brickTextures, shaders[1]))
		objects.add(plane3)
		objects.add(makePlane(1.3f, texScale = 0.4f).textured(gl, sofa, shaders[3]))
		objects.add(makePlane(1.3f, texScale = 2f).textured(gl, copper, shaders[4]))
		objects.add(makePlane(1.3f).textured(gl, wood, shaders[5]))


		objects.add(
			makeCylinder(4, 1.3f, 1f, color = Vec4(0.4f, 0.2f, 0.2f, 1f)).textured(
				gl,
				brickTextures,
				shaders[1]
			)
		)

		gl.glUseProgram(0)
	}

	override fun display(drawable: GLAutoDrawable) {
		val gl = drawable.gl.gL2ES2

		gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)

		t += 0.3f
		mats.glMatrixMode(GL2.GL_PROJECTION)
		mats.glLoadIdentity()
		mats.glFrustumf(-0.5f, 0.5f, -0.5f, 0.5f, 1f, 100f)
		mats.glMatrixMode(GL2.GL_MODELVIEW)
		mats.glLoadIdentity()
		mats.glTranslatef(0f, 0f, -4.0f)

		objects[select].displayAt(gl, mats, lightpos, lightcolor) {
			glRotatef((mouseY - height / 2).toFloat() + 45, 1f, 0f, 0f)
			glRotatef((mouseX - width / 2).toFloat(), 0f, 1f, 0f)
			glRotatef(90f, 1f, 0f, 0f)
			glRotatef(45f, 0f, 0f, 1f)
		}

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
