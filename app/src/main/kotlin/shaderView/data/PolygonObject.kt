package shaderView.data

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.util.PMVMatrix
import shaderView.addBuffer
import shaderView.addTexture
import shaderView.bindBuffer
import shaderView.bindTexture

class TexturedObject(
	gl: GL2ES2,
	polygonSet: PolygonSet,
	texture: TextureImage,
	shader: Shader,
) : Object3D(shader) {

	private val vertexBufferId = gl.addBuffer(GL.GL_ARRAY_BUFFER, polygonSet.vertexArray)
	private val elementBufferId = gl.addBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, polygonSet.elementArray)
	private val elementSize = polygonSet.elementArray.size

	private val textureId = gl.addTexture(GL.GL_TEXTURE0, texture)
	private val textureUniform = bindProgram(gl) {
		val id = glGetUniformLocation(shader.id, "texture0")
		glUniform1i(id, 0)
		glBindTexture(GL2.GL_TEXTURE_2D, 0)
		id
	}

	override fun display(gl: GL2ES2, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		bindProgram(gl) {
			shader.setMatrixAndLight(gl, mats, lightpos, lightcolor)
			bindTexture(GL2.GL_TEXTURE_2D, textureId) {
				glUniform1i(textureUniform, 0)
				bindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId) {
					glVertexAttribPointer(VERTEXPOSITION, 3, GL.GL_FLOAT, false, 48, 0)
					glVertexAttribPointer(VERTEXNORMAL, 3, GL.GL_FLOAT, false, 48, Vertex.OFFSET_NORMAL.toLong())
					glVertexAttribPointer(VERTEXCOLOR, 4, GL.GL_FLOAT, false, 48, Vertex.OFFSET_COLOR.toLong())
					glVertexAttribPointer(VERTEXTEXCOORD0, 2, GL.GL_FLOAT, false, 48, Vertex.OFFSET_TEXCOORD.toLong())
					bindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId) {
						glEnableVertexAttribArray(VERTEXPOSITION)
						glEnableVertexAttribArray(VERTEXCOLOR)
						glEnableVertexAttribArray(VERTEXNORMAL)
						glEnableVertexAttribArray(VERTEXTEXCOORD0)

						glDrawElements(GL.GL_TRIANGLES, elementSize, GL.GL_UNSIGNED_INT, 0)

						glDisableVertexAttribArray(VERTEXPOSITION)
						glDisableVertexAttribArray(VERTEXNORMAL)
						glDisableVertexAttribArray(VERTEXCOLOR)
						glDisableVertexAttribArray(VERTEXTEXCOORD0)
					}
				}
			}
		}
	}
}
