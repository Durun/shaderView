package shaderView

import com.jogamp.opengl.*
import shaderView.data.Vec4

fun main() {
	AppBase("shader", 480, 320, AppListener()).start()
}

class AppListener : GLEventListener {
	companion object {
		val width = 480
		val height = 320
		val bgColor = Vec4(0.5f, 0.5f, 0.5f, 1f)
	}

	override fun init(drawable: GLAutoDrawable) {

		val gl: GL3 = drawable.gl.gL3
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
	}

	override fun display(drawable: GLAutoDrawable) {
		val gl: GL3 = drawable.gl.gL3

		gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
		gl.glFlush()
	}

	override fun dispose(drawable: GLAutoDrawable?) {}
	override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {}
}