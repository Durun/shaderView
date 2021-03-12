package shaderView

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.PMVMatrix
import shaderView.data.*

class Plane(gl: GL2ES2, texture: TextureImage, shader: Shader) : Object3D(shader) {
	private val vertice = run {
		val normal = Vec3(0f, 0f, -1f)
		val red = Vec4(1f, 0f, 0f, 1f)
		listOf(
			Vertex(Vec3(-1f, -1f, 0f), normal, red, Vec2(0f, 0f)),
			Vertex(Vec3(1f, -1f, 0f), normal, red, Vec2(1f, 0f)),
			Vertex(Vec3(1f, 1f, 0f), normal, red, Vec2(1f, 1f)),
			Vertex(Vec3(-1f, 1f, 0f), normal, red, Vec2(0f, 1f))
		)
	}
	private val ElementData = intArrayOf(
		0, 1, 2,  //polygon#0
		0, 2, 3, //pollgon#1
		0, 2, 1,  //polygon#0
		0, 3, 2 //pollgon#1
	)
	private val elementBufferId = gl.addBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ElementData)
	private val vertexBufferId = gl.addBuffer(GL.GL_ARRAY_BUFFER, vertice.toFloatArray())
	private val textureId = gl.addTexture(GL.GL_TEXTURE0, texture)
	private val uniformTexture = bindProgram(gl) {
		val id = glGetUniformLocation(shader.id, "texture0")
		glUniform1i(id, 0)
		glBindTexture(GL2.GL_TEXTURE_2D, 0)
		id
	}

	override fun display(gl: GL3, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		bindProgram(gl) {
			shader.setMatrixAndLight(gl, mats, lightpos, lightcolor)
			bindTexture(GL2.GL_TEXTURE_2D, textureId) {
				glUniform1i(uniformTexture, 0)
				bindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId) {
					glVertexAttribPointer(VERTEXPOSITION, 3, GL.GL_FLOAT, false, 48, 0)
					glVertexAttribPointer(VERTEXNORMAL, 3, GL.GL_FLOAT, false, 48, OFFSET_NORMAL.toLong())
					glVertexAttribPointer(VERTEXCOLOR, 4, GL.GL_FLOAT, false, 48, OFFSET_COLOR.toLong())
					glVertexAttribPointer(VERTEXTEXCOORD0, 2, GL.GL_FLOAT, false, 48, OFFSET_TEXCOORD.toLong())
					bindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId) {
						glEnableVertexAttribArray(VERTEXPOSITION)
						glEnableVertexAttribArray(VERTEXCOLOR)
						glEnableVertexAttribArray(VERTEXNORMAL)
						glEnableVertexAttribArray(VERTEXTEXCOORD0)

						glDrawElements(GL.GL_TRIANGLES, ElementData.size, GL.GL_UNSIGNED_INT, 0)

						glDisableVertexAttribArray(VERTEXPOSITION)
						glDisableVertexAttribArray(VERTEXNORMAL)
						glDisableVertexAttribArray(VERTEXCOLOR)
						glDisableVertexAttribArray(VERTEXTEXCOORD0)
					}
				}
			}
		}
	}

	companion object {
		private const val OFFSET_NORMAL = 3 * FLOAT_BYTES
		private const val OFFSET_COLOR = 6 * FLOAT_BYTES
		private const val OFFSET_TEXCOORD = 10 * FLOAT_BYTES
	}
}
