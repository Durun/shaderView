package shaderView

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.util.PMVMatrix
import shaderView.data.*
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
	val objects: MutableCollection<Object3D> = mutableListOf()

	val mats = PMVMatrix()
	var t = 0f

	val lightpos = Vec3(0.0f, 0.0f, 30f)
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
		}
		objects.add(Plane(gl, DotImage(512, 512), shaders[0]))
		objects.add(Plane(gl, DotImage(256, 512), shaders[0]))
		objects.add(Plane(gl, FileImage("app/src/main/resources/BrickNormalMap.png"), shaders[0]))
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


		objects.forEachIndexed { i, it ->
			it.displayAt(gl, mats, lightpos, lightcolor) {
				glTranslatef(i % 3 - 1f, 0.7f - i / 3 * 1.4f, 0f)
				glRotatef(t, 0.3f, 1f, 0f)
				glRotatef(45f, 0f, 0f, 1f)
			}
		}

		gl.glFlush()
	}

	override fun dispose(drawable: GLAutoDrawable?) {}
	override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {}
}