package shaderView

import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow

fun GLWindow.addWindowListener(listener: (WindowEvent?) -> Unit) {
	this.addWindowListener(object : WindowAdapter() {
		override fun windowDestroyNotify(e: WindowEvent?) = listener(e)
	})
}