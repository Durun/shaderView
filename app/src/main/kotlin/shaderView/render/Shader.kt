package shaderView.render

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL2GL3
import com.jogamp.opengl.util.PMVMatrix
import shaderView.attachShader
import shaderView.data.Vec3
import shaderView.getProgramInfoLogString
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
			glBindAttribLocation(id, Object3D.VERTEXPosITION, "inPosition")
			glBindAttribLocation(id, Object3D.VERTEXCOLOR, "inColor")
			glBindAttribLocation(id, Object3D.VERTEXNORMAL, "inNormal")
			glBindAttribLocation(id, Object3D.VERTEXTEXCOORD0, "inTexCoord0")
			glBindAttribLocation(id, Object3D.VERTEXTANGENT, "inTangent")
			link()
		}
	}

	private fun GL2ES2.link() {
		val linkStatus = IntArray(1)
		glLinkProgram(id)
		glGetProgramiv(id, GL2ES2.GL_LINK_STATUS, linkStatus, 0)
		check(linkStatus[0] == GL.GL_TRUE) { "Link error: ${getProgramInfoLogString(id)}" }
		uniformMat = glGetUniformLocation(id, "mat")
		uniformLightPos = glGetUniformLocation(id, "lightPos")
		uniformLightColor = glGetUniformLocation(id, "lightcolor")
	}

	fun setMatrixAndLight(gl: GL2ES2, mats: PMVMatrix, lightPos: Vec3<Float>, lightcolor: Vec3<Float>) {
		gl.glUniformMatrix4fv(uniformMat, 4, false, mats.glGetPMvMvitMatrixf())
		lightPos.let { (x, y, z) -> gl.glUniform3f(uniformLightPos, x, y, z) }
		lightcolor.let { (r, g, b) -> gl.glUniform3f(uniformLightColor, r, g, b) }
	}
}
