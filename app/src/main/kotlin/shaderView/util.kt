package shaderView

import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.GL2ES2

fun GLWindow.addWindowListener(listener: (WindowEvent?) -> Unit) {
	this.addWindowListener(object : WindowAdapter() {
		override fun windowDestroyNotify(e: WindowEvent?) = listener(e)
	})
}

fun GL2ES2.glShaderSource(shaderId: Int, src: String) {
	this.glShaderSource(shaderId, 1, arrayOf(src), arrayOf(src.length).toIntArray(), 0)
}