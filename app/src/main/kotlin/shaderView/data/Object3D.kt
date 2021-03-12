package shaderView.data

import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.PMVMatrix

abstract class Object3D {
	private val storedprogramID = IntArray(1)

	abstract fun init(gl: GL3, mat: PMVMatrix, shader: Shader)
	abstract fun display(gl: GL3, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>)
	fun bindProgram(gl: GL2ES2, shader: Shader) {
		gl.glGetIntegerv(GL3.GL_CURRENT_PROGRAM, storedprogramID, 0)
		gl.glUseProgram(shader.iD)
	}

	fun unbindProgram(gl: GL2ES2) {
		gl.glUseProgram(storedprogramID[0])
	}

	companion object {
		const val VERTEXPOSITION = 0
		const val VERTEXCOLOR = 1
		const val VERTEXTEXCOORD0 = 3
		const val VERTEXNORMAL = 2
		const val VERTEXTANGENT = 4
	}
}
