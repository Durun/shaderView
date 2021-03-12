package shaderView.data

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL2GL3
import com.jogamp.opengl.util.PMVMatrix
import shaderView.attachShader
import java.nio.file.Path

class Shader(gl: GL2ES2, vshaderfile: Path, fshaderfile: Path) {
	private val vertexShaderSrc = vshaderfile.toFile().readText()
	private val fragmentShaderSrc = fshaderfile.toFile().readText()
	val id = gl.glCreateProgram()
	var uniformMat = 0
	var uniformLightPos = 0
	var uniformLightColor = 0

	init {
		gl.apply {
			attachShader(id, GL2GL3.GL_VERTEX_SHADER, vertexShaderSrc)
			attachShader(id, GL2GL3.GL_FRAGMENT_SHADER, fragmentShaderSrc)
			glBindAttribLocation(id, Object3D.VERTEXPOSITION, "inposition")
			glBindAttribLocation(id, Object3D.VERTEXCOLOR, "incolor")
			glBindAttribLocation(id, Object3D.VERTEXNORMAL, "innormal")
			glBindAttribLocation(id, Object3D.VERTEXTEXCOORD0, "intexcoord0")
			glBindAttribLocation(id, Object3D.VERTEXTANGENT, "intangent")
		}
		link(gl)
	}

	fun link(gl: GL2ES2) {
		val linkStatus = IntArray(1)
		gl.glLinkProgram(id)
		gl.glGetProgramiv(id, GL2ES2.GL_LINK_STATUS, linkStatus, 0)
		if (linkStatus[0] == GL.GL_FALSE) {
			val logMax = 8192
			val log = ByteArray(logMax)
			val logLength = IntArray(1)
			System.err.println("link error")
			gl.glGetProgramInfoLog(id, logMax, logLength, 0, log, 0)
			showLog(log, logLength[0])
			System.exit(1)
		}
		uniformMat = gl.glGetUniformLocation(id, "mat")
		uniformLightPos = gl.glGetUniformLocation(id, "lightpos")
		uniformLightColor = gl.glGetUniformLocation(id, "lightcolor")
	}


	fun setMatrixAndLight(gl: GL2ES2, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		gl.glUniformMatrix4fv(uniformMat, 4, false, mats.glGetPMvMvitMatrixf())
		lightpos.let { (x, y, z) -> gl.glUniform3f(uniformLightPos, x, y, z) }
		lightcolor.let { (r, g, b) -> gl.glUniform3f(uniformLightColor, r, g, b) }
	}

	fun validateProgram(gl: GL2ES2) {
		val validateStatus = IntArray(1)
		gl.glValidateProgram(id)
		gl.glGetProgramiv(id, GL2ES2.GL_VALIDATE_STATUS, validateStatus, 0)
		if (validateStatus[0] == GL.GL_FALSE) {
			val logMax = 8192
			val log = ByteArray(logMax)
			val logLength = IntArray(1)
			System.err.println("validate error")
			gl.glGetProgramInfoLog(id, logMax, logLength, 0, log, 0)
			showLog(log, logLength[0])
			System.exit(1)
		}
	}

	private fun showLog(log: ByteArray, length: Int) {
		for (i in 0 until length) {
			System.err.print(log[i].toChar())
		}
	}

}
