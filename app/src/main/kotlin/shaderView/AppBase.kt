package shaderView

import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.util.FPSAnimator
import kotlin.system.exitProcess

class AppBase(
    val title: String,
    val width: Int,
    val height: Int
) {
    val window: GLWindow = run {
        val profile = runCatching { GLProfile.get(GLProfile.GL3) }
            .recover { GLProfile.get(GLProfile.GL2ES2) }
            .getOrThrow()
        val caps = GLCapabilities(profile).apply {
            doubleBuffered = true
            numSamples = 8
            sampleBuffers = true
        }
        GLWindow.create(caps).apply {
            title = this@AppBase.title
            setSize(this@AppBase.width, this@AppBase.height)
            addWindowListener { exitProcess(0) }
        }
    }

    fun start(listener: AppListener) {
        window.apply {
            addGLEventListener(listener)
            addMouseListener(listener.mouseListener)
            addKeyListener(listener.keyListener)
        }

        window.isVisible = true

        val animator = FPSAnimator(window, 60)
        animator.start()
        println("Start success.")
    }
}