package shaderView.data

import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.PMVMatrix

abstract class Object3D(
	protected val shader: Shader
) {
	private val storedprogramID = IntArray(1)

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
		gl.glGetIntegerv(GL3.GL_CURRENT_PROGRAM, storedprogramID, 0)
		gl.glUseProgram(shader.id)
		val result = runCatching { gl.block() }
		gl.glUseProgram(storedprogramID[0])
		return result.getOrThrow()
	}

	companion object {
		const val VERTEXPOSITION = 0
		const val VERTEXCOLOR = 1
		const val VERTEXTEXCOORD0 = 3
		const val VERTEXNORMAL = 2
		const val VERTEXTANGENT = 4
	}
}
