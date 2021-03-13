package shaderView.render

import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.PMVMatrix
import shaderView.data.Vec3

abstract class Object3D(
	protected val shader: Shader
) {
	abstract fun display(gl: GL2ES2, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>)

	fun displayAt(
		gl: GL2ES2,
		mats: PMVMatrix,
		lightpos: Vec3<Float>,
		lightcolor: Vec3<Float>,
		translate: PMVMatrix.() -> Unit
	) {
		mats.glPushMatrix()
		mats.translate()
		mats.update()
		display(gl, mats, lightpos, lightcolor)
		mats.glPopMatrix()
	}

	fun <R> bindProgram(gl: GL2ES2, block: GL2ES2.() -> R): R {
		val idBuffer = IntArray(1)
		gl.glGetIntegerv(GL3.GL_CURRENT_PROGRAM, idBuffer, 0)
		gl.glUseProgram(shader.id)
		val result = runCatching { gl.block() }
		gl.glUseProgram(idBuffer[0])
		return result.getOrThrow()
	}

	companion object {
		const val VERTEXPOSITION = 0
		const val VERTEXCOLOR = 1
		const val VERTEXNORMAL = 2
		const val VERTEXTEXCOORD0 = 3
		const val VERTEXTANGENT = 4
	}
}
