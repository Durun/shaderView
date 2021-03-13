package shaderView.render

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.util.PMVMatrix
import shaderView.addBuffer
import shaderView.addTexture
import shaderView.bindBuffer
import shaderView.bindTextures
import shaderView.data.PolygonSet
import shaderView.data.TextureImage
import shaderView.data.Vec3
import shaderView.data.Vertex

class TexturedObject(
	gl: GL2ES2,
	polygonSet: PolygonSet,
	textures: List<TextureImage>,
	shader: Shader,
) : Object3D(shader) {

	private val vertexBufferId = gl.addBuffer(GL.GL_ARRAY_BUFFER, polygonSet.vertexArray)
	private val elementBufferId = gl.addBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, polygonSet.elementArray)
	private val elementSize = polygonSet.elementArray.size

	private val textureIds: List<Int> = textures.mapIndexed { i, it ->
		gl.addTexture(GL.GL_TEXTURE0 + i, it)
	}
	private val textureUniforms: List<Int> = bindProgram(gl) {
		textures.mapIndexed { i, _ ->
			glGetUniformLocation(shader.id, "texture$i")
				.also {
					glUniform1i(it, i)
					glBindTexture(GL2.GL_TEXTURE_2D, i)
				}
		}
	}

	override fun display(gl: GL2ES2, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		bindProgram(gl) {
			shader.setMatrixAndLight(gl, mats, lightpos, lightcolor)
			bindTextures(GL2.GL_TEXTURE_2D, textureIds) {
				textureUniforms.forEachIndexed { i, it -> glUniform1i(it, i) }
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

fun PolygonSet.textured(gl: GL2ES2, textures: List<TextureImage>, shader: Shader): Object3D {
	return TexturedObject(gl, this, textures, shader)
}