package shaderView

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.PMVMatrix
import shaderView.data.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Plane(shader: Shader) : Object3D(shader) {
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
	private val VertexData = vertice.toFloatArray()
	private val VertexSize = VertexData.size * FLOAT_BYTES
	private val FBVertexData = FloatBuffer.wrap(VertexData)
	private val ElementData = intArrayOf(
		0, 1, 2,  //polygon#0
		0, 2, 3, //pollgon#1
		0, 2, 1,  //polygon#0
		0, 3, 2 //pollgon#1
	)
	private val PolygonCount = ElementData.size / 3
	private val ElementCount = ElementData.size
	private val ElementSize = ElementCount * Integer.SIZE / 8
	private val IBElementData = IntBuffer.wrap(ElementData)
	private var ElementBufferName = 0
	private var ArrayBufferName = 0
	private var TextureName = 0
	private var uniformTexture = 0
	private lateinit var img: TextureImage
	override fun init(gl: GL3) {
		val tmp = IntArray(1)

		ArrayBufferName = gl.addBuffer(GL.GL_ARRAY_BUFFER, VertexData)
		ElementBufferName = gl.addBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ElementData)

		//img = new ImageLoader("circles.png");
		img = DotImage(512, 512)
		gl.glGenTextures(1, tmp, 0)
		TextureName = tmp[0]
		gl.glActiveTexture(GL.GL_TEXTURE0)
		gl.glEnable(GL.GL_TEXTURE_2D)
		gl.glBindTexture(GL2.GL_TEXTURE_2D, TextureName)
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP)
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP)

		gl.glTexImage2D(
			GL2.GL_TEXTURE_2D, 0, GL.GL_RGBA8, img.width,
			img.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
			img.byteBuffer
		)

		bindProgram(gl) {
			uniformTexture = glGetUniformLocation(shader.id, "texture0")
			glUniform1i(uniformTexture, 0)
			glBindTexture(GL2.GL_TEXTURE_2D, 0)
		}
	}

	override fun display(gl: GL3, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		bindProgram(gl) {
			shader.setMatrixAndLight(gl, mats, lightpos, lightcolor)
			glBindTexture(GL2.GL_TEXTURE_2D, TextureName)
			glUniform1i(uniformTexture, 0)
			glBindBuffer(GL.GL_ARRAY_BUFFER, ArrayBufferName)
			glVertexAttribPointer(VERTEXPOSITION, 3, GL.GL_FLOAT, false, 48, 0)
			glVertexAttribPointer(VERTEXNORMAL, 3, GL.GL_FLOAT, false, 48, OFFSET_NORMAL.toLong())
			glVertexAttribPointer(VERTEXCOLOR, 4, GL.GL_FLOAT, false, 48, OFFSET_COLOR.toLong())
			glVertexAttribPointer(VERTEXTEXCOORD0, 2, GL.GL_FLOAT, false, 48, OFFSET_TEXCOORD.toLong())
			glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ElementBufferName)
			glEnableVertexAttribArray(VERTEXPOSITION)
			glEnableVertexAttribArray(VERTEXCOLOR)
			glEnableVertexAttribArray(VERTEXNORMAL)
			glEnableVertexAttribArray(VERTEXTEXCOORD0)
			glDrawElements(GL.GL_TRIANGLES, ElementCount, GL.GL_UNSIGNED_INT, 0)
			glDisableVertexAttribArray(VERTEXPOSITION)
			glDisableVertexAttribArray(VERTEXNORMAL)
			glDisableVertexAttribArray(VERTEXCOLOR)
			glDisableVertexAttribArray(VERTEXTEXCOORD0)
			glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0)
			glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
			glBindTexture(GL2.GL_TEXTURE_2D, 0)
		}
	}

	companion object {
		private const val OFFSET_NORMAL = 3 * FLOAT_BYTES
		private const val OFFSET_COLOR = 6 * FLOAT_BYTES
		private const val OFFSET_TEXCOORD = 10 * FLOAT_BYTES
	}
}
