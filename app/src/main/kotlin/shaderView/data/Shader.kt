package shaderView.data

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL2GL3
import com.jogamp.opengl.util.PMVMatrix
import shaderView.glShaderSource
import java.nio.file.Path

class Shader(vshaderfile: Path, fshaderfile: Path) {
	private val vertexShaderSrc = vshaderfile.toFile().readText()
	private val fragmentShaderSrc = fshaderfile.toFile().readText()
	var iD = 0
	var uniformMat = 0
	var uniformLightPos = 0
	var uniformLightColor = 0

	fun init(gl: GL2ES2) {
		iD = gl.glCreateProgram()
		var shaderid: Int = gl.glCreateShader(GL2GL3.GL_VERTEX_SHADER)
		gl.glShaderSource(shaderid, vertexShaderSrc)
		gl.glCompileShader(shaderid)
		checkCompileError(gl, shaderid)
		gl.glAttachShader(iD, shaderid)
		gl.glDeleteShader(shaderid)
		shaderid = gl.glCreateShader(GL2GL3.GL_FRAGMENT_SHADER)
		gl.glShaderSource(shaderid, fragmentShaderSrc)
		gl.glCompileShader(shaderid)
		checkCompileError(gl, shaderid)
		gl.glAttachShader(iD, shaderid)
		gl.glDeleteShader(shaderid)
		gl.glBindAttribLocation(iD, Object3D.VERTEXPOSITION, "inposition")
		gl.glBindAttribLocation(iD, Object3D.VERTEXCOLOR, "incolor")
		gl.glBindAttribLocation(iD, Object3D.VERTEXNORMAL, "innormal")
		gl.glBindAttribLocation(iD, Object3D.VERTEXTEXCOORD0, "intexcoord0")
		gl.glBindAttribLocation(iD, Object3D.VERTEXTANGENT, "intangent")
		link(gl)
	}

	fun link(gl: GL2ES2) {
		val linkStatus = IntArray(1)
		gl.glLinkProgram(iD)
		gl.glGetProgramiv(iD, GL2ES2.GL_LINK_STATUS, linkStatus, 0)
		if (linkStatus[0] == GL.GL_FALSE) {
			val logMax = 8192
			val log = ByteArray(logMax)
			val logLength = IntArray(1)
			System.err.println("link error")
			gl.glGetProgramInfoLog(iD, logMax, logLength, 0, log, 0)
			showLog(log, logLength[0])
			System.exit(1)
		}
		uniformMat = gl.glGetUniformLocation(iD, "mat")
		uniformLightPos = gl.glGetUniformLocation(iD, "lightpos")
		uniformLightColor = gl.glGetUniformLocation(iD, "lightcolor")
	}

	private fun checkCompileError(gl: GL2ES2, shaderid: Int) {
		val compileStatus = IntArray(1)
		gl.glGetShaderiv(shaderid, GL2ES2.GL_COMPILE_STATUS, compileStatus, 0)
		if (compileStatus[0] == GL.GL_FALSE) {
			val logMax = 8192
			val log = ByteArray(logMax)
			val logLength = IntArray(1)
			gl.glGetShaderInfoLog(shaderid, logMax, logLength, 0, log, 0)
			showLog(log, logLength[0])
			System.exit(1)
		}
	}

	fun setMatrixAndLight(gl: GL2ES2, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		gl.glUniformMatrix4fv(uniformMat, 4, false, mats.glGetPMvMvitMatrixf())
		lightpos.let { (x, y, z) -> gl.glUniform3f(uniformLightPos, x, y, z) }
		lightpos.let { (r, g, b) -> gl.glUniform3f(uniformLightColor, r, g, b) }
	}

	fun validateProgram(gl: GL2ES2) {
		val validateStatus = IntArray(1)
		gl.glValidateProgram(iD)
		gl.glGetProgramiv(iD, GL2ES2.GL_VALIDATE_STATUS, validateStatus, 0)
		if (validateStatus[0] == GL.GL_FALSE) {
			val logMax = 8192
			val log = ByteArray(logMax)
			val logLength = IntArray(1)
			System.err.println("validate error")
			gl.glGetProgramInfoLog(iD, logMax, logLength, 0, log, 0)
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
