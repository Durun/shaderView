package shaderView

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.util.PMVMatrix
import shaderView.data.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Cylinder(
	gl: GL2ES2,
	num: Int, radius: Float, height: Float, smooth: Boolean, texScale: FloatArray,
	color: Vec4<Float>, textureimages: List<TextureImage>, shader: Shader
) : Object3D(shader) {
	private val VertexData: FloatArray

	//example for one vertex
	//{ -1.0f,  1.0f,  0f,  0.0f, 0.0f,-1.0f,     0f,1f }
	//  position            normal                texcoord
	private val VertexData2: FloatArray

	//example for one vertex
	//{ 0f,0f,1f,1f }
	//  color
	private val VertexData3: FloatArray

	//example for one vertex
	//{ -1.0f,  1.0f,  0f }
	//  tangent
	private val NormalOffset = FLOAT_BYTES * 3
	private val ColorOffset = 0 //Float.SIZE/8*0;
	private val TangentOffset = 0 //Float.SIZE/8*0;
	private val TexCoordOffset = FLOAT_BYTES * 6 //Float.SIZE/8*10;
	private val VertexCount: Int
	private val VertexSize: Int
	private val VertexSize2: Int
	private val VertexSize3: Int
	private val FBVertexData: FloatBuffer
	private val FBVertexData2: FloatBuffer
	private val FBVertexData3: FloatBuffer
	private val ElementData // { 0,1,2 } example for one polygon
			: IntArray
	private val PolygonCount: Int
	private val ElementCount: Int
	private val ElementSize: Int
	private val IBElementData: IntBuffer
	private var ElementBufferName = 0
	private var ArrayBufferName = 0
	private var ArrayBufferName2 = 0
	private var ArrayBufferName3 = 0
	private val TextureNames: MutableList<Int> = ArrayList()
	private val uniformTextures: MutableList<Int> = ArrayList()
	private val images: List<TextureImage>

	init {
		images = textureimages
		var offset = 0 // center of bottom
		if (smooth) {
			VertexData = FloatArray(8 * ((num + 1) * 4 + 2))
			VertexData2 = FloatArray(4 * ((num + 1) * 4 + 2))
			VertexData3 = FloatArray(3 * ((num + 1) * 4 + 2))
		} else {
			VertexData = FloatArray(8 * ((num + 1) * 2 + 2 + num * 4))
			VertexData2 = FloatArray(4 * ((num + 1) * 2 + 2 + num * 4))
			VertexData3 = FloatArray(3 * ((num + 1) * 2 + 2 + num * 4))
		}
		VertexData[0] = 0.0f
		VertexData[1] = 0.0f
		VertexData[2] = -height / 2f
		VertexData[3] = 0.0f
		VertexData[4] = 0.0f
		VertexData[5] = -1.0f
		VertexData[6] = 0.5f
		VertexData[7] = 0.5f
		setColor(0, color)
		setTangent(0, Vec3(-1f, 0f, 0f))
		offset = 1
		for (i in 0 until num + 1) { // bottom
			val j = i + offset
			VertexData[j * 8 + 0] = (radius * Math.cos(i * 2 * Math.PI / num)).toFloat()
			VertexData[j * 8 + 1] = (radius * Math.sin(i * 2 * Math.PI / num)).toFloat()
			VertexData[j * 8 + 2] = -height / 2f
			VertexData[j * 8 + 3] = 0.0f
			VertexData[j * 8 + 4] = 0.0f
			VertexData[j * 8 + 5] = -1.0f
			VertexData[j * 8 + 6] = (-0.5 * Math.cos(i * 2 * Math.PI / num) + 0.5f).toFloat()
			VertexData[j * 8 + 7] = (0.5 * Math.sin(i * 2 * Math.PI / num) + 0.5f).toFloat()
			setColor(j, color)
			setTangent(j, Vec3(-1f, 0f, 0f))
		}
		offset = num + 1 + 1
		for (i in 0 until num + 1) { //top
			val j = i + offset
			VertexData[j * 8] = (radius * Math.cos(i * 2 * Math.PI / num)).toFloat()
			VertexData[j * 8 + 1] = (radius * Math.sin(i * 2 * Math.PI / num)).toFloat()
			VertexData[j * 8 + 2] = height / 2f
			VertexData[j * 8 + 3] = 0.0f
			VertexData[j * 8 + 4] = 0.0f
			VertexData[j * 8 + 5] = 1.0f
			VertexData[j * 8 + 6] = (0.5 * Math.cos(i * 2 * Math.PI / num) + 0.5f).toFloat()
			VertexData[j * 8 + 7] = (0.5 * Math.sin(i * 2 * Math.PI / num) + 0.5f).toFloat()
			setColor(j, color)
			setTangent(j, Vec3(1f, 0f, 0f))
		}
		offset = (num + 1) * 2 + 1 // center of top
		var j = offset
		VertexData[j * 8] = 0.0f
		VertexData[j * 8 + 1] = 0.0f
		VertexData[j * 8 + 2] = height / 2f
		VertexData[j * 8 + 3] = 0.0f
		VertexData[j * 8 + 4] = 0.0f
		VertexData[j * 8 + 5] = 1.0f
		VertexData[j * 8 + 6] = 0.5f
		VertexData[j * 8 + 7] = 0.5f
		setColor(j, color)
		setTangent(j, Vec3(1f, 0f, 0f))
		if (smooth) {
			offset = (num + 1) * 2 + 2
			for (i in 0 until num + 1) {  // lower side
				j = i + offset
				VertexData[j * 8] = (radius * Math.cos(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 1] = (radius * Math.sin(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 2] = -height / 2f
				VertexData[j * 8 + 3] = Math.cos(i * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 4] = Math.sin(i * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 5] = 0.0f
				VertexData[j * 8 + 6] = i * 1.0f / (num - 1)
				VertexData[j * 8 + 7] = 1.0f
				setColor(j, color)
				setTangent(j, Vec3(VertexData[j * 8 + 1], -VertexData[j * 8], 0f))
			}
			offset = (num + 1) * 3 + 2
			for (i in 0 until num + 1) { // upper side
				j = i + offset
				VertexData[j * 8] = (radius * Math.cos(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 1] = (radius * Math.sin(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 2] = height / 2f
				VertexData[j * 8 + 3] = Math.cos(i * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 4] = Math.sin(i * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 5] = 0.0f
				VertexData[j * 8 + 6] = i * 1.0f / (num - 1)
				VertexData[j * 8 + 7] = 0.0f
				setColor(j, color)
				setTangent(j, Vec3(VertexData[j * 8 + 1], -VertexData[j * 8], 0f))
			}
		} else { //non smooth
			offset = (num + 1) * 2 + 2
			for (i in 0 until num) {  // lower side
				j = i + offset
				VertexData[j * 8] = (radius * Math.cos(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 1] = (radius * Math.sin(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 2] = -height / 2f
				VertexData[j * 8 + 3] = Math.cos((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 4] = Math.sin((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 5] = 0.0f
				VertexData[j * 8 + 6] = i * 1.0f / num
				VertexData[j * 8 + 7] = 1.0f
				setColor(j, color)
				setTangent(j, Vec3(VertexData[j * 8 + 1], -VertexData[j * 8], 0f))
			}
			offset = (num + 1) * 2 + 2 + num
			for (i in 0 until num) {  // lower side 2
				j = i + offset
				VertexData[j * 8] = (radius * Math.cos((i + 1) * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 1] = (radius * Math.sin((i + 1) * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 2] = -height / 2f
				VertexData[j * 8 + 3] = Math.cos((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 4] = Math.sin((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 5] = 0.0f
				VertexData[j * 8 + 6] = (i + 1) * 1.0f / num
				VertexData[j * 8 + 7] = 1.0f
				setColor(j, color)
				setTangent(j, Vec3(VertexData[j * 8 + 1], -VertexData[j * 8], 0f))
			}
			offset = (num + 1) * 2 + 2 + num * 2
			for (i in 0 until num) {  // upper side
				j = i + offset
				VertexData[j * 8] = (radius * Math.cos(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 1] = (radius * Math.sin(i * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 2] = height / 2f
				VertexData[j * 8 + 3] = Math.cos((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 4] = Math.sin((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 5] = 0.0f
				VertexData[j * 8 + 6] = i * 1.0f / num
				VertexData[j * 8 + 7] = 0.0f
				setColor(j, color)
				setTangent(j, Vec3(VertexData[j * 8 + 1], -VertexData[j * 8], 0f))
			}
			offset = (num + 1) * 2 + 2 + num * 3
			for (i in 0 until num) {  // upper side 2
				j = i + offset
				VertexData[j * 8] = (radius * Math.cos((i + 1) * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 1] = (radius * Math.sin((i + 1) * 2 * Math.PI / num)).toFloat()
				VertexData[j * 8 + 2] = height / 2f
				VertexData[j * 8 + 3] = Math.cos((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 4] = Math.sin((i + 0.5) * 2 * Math.PI / num).toFloat()
				VertexData[j * 8 + 5] = 0.0f
				VertexData[j * 8 + 6] = (i + 1) * 1.0f / num
				VertexData[j * 8 + 7] = 0.0f
				setColor(j, color)
				setTangent(j, Vec3(VertexData[j * 8 + 1], -VertexData[j * 8], 0f))
			}
		}

		// scale texcoord
		for (i in 0 until VertexData.size / 8) {
			VertexData[i * 8 + 6] *= texScale[0]
			VertexData[i * 8 + 7] *= texScale[1]
			if (4 <= texScale.size) { // offset
				VertexData[i * 8 + 6] += texScale[2]
				VertexData[i * 8 + 7] += texScale[3]
			}
		}
		ElementData = IntArray(num * 4 * 3)
		for (i in 0 until num) { //bottom
			ElementData[i * 3] = 0
			ElementData[i * 3 + 1] = i + 2
			ElementData[i * 3 + 2] = i + 1
		}
		offset = num
		for (i in 0 until num) { //top
			j = offset + i
			ElementData[j * 3] = (num + 1) * 2 + 1
			ElementData[j * 3 + 1] = num + 2 + i
			ElementData[j * 3 + 2] = num + 3 + i
		}
		offset = num * 2
		if (smooth) {
			for (i in 0 until num) { //side
				j = offset + i * 2
				ElementData[j * 3] = (num + 1) * 2 + 2 + i
				ElementData[j * 3 + 1] = (num + 1) * 3 + 2 + i + 1
				ElementData[j * 3 + 2] = (num + 1) * 3 + 2 + i
				j = offset + i * 2 + 1
				ElementData[j * 3] = (num + 1) * 2 + 2 + i
				ElementData[j * 3 + 1] = (num + 1) * 2 + 2 + i + 1
				ElementData[j * 3 + 2] = (num + 1) * 3 + 2 + i + 1
			}
		} else {
			for (i in 0 until num) { //side
				j = offset + i * 2
				ElementData[j * 3] = (num + 1) * 2 + 2 + i
				ElementData[j * 3 + 1] = (num + 1) * 2 + 2 + num + i
				ElementData[j * 3 + 2] = (num + 1) * 2 + 2 + num * 2 + i
				j = offset + i * 2 + 1
				ElementData[j * 3] = (num + 1) * 2 + 2 + num + i
				ElementData[j * 3 + 1] = (num + 1) * 2 + 2 + num * 3 + i
				ElementData[j * 3 + 2] = (num + 1) * 2 + 2 + num * 2 + i
			}
		}
		VertexCount = VertexData.size / 8
		VertexSize = VertexData.size * java.lang.Float.SIZE / 8
		VertexSize2 = VertexData2.size * java.lang.Float.SIZE / 8
		VertexSize3 = VertexData3.size * java.lang.Float.SIZE / 8
		FBVertexData = FloatBuffer.wrap(VertexData)
		FBVertexData2 = FloatBuffer.wrap(VertexData2)
		FBVertexData3 = FloatBuffer.wrap(VertexData3)
		PolygonCount = ElementData.size / 3
		ElementCount = ElementData.size
		ElementSize = ElementCount * Integer.SIZE / 8
		IBElementData = IntBuffer.wrap(ElementData)
	}

	init {
		val tmp = IntArray(1)
		ArrayBufferName = gl.addBuffer(GL.GL_ARRAY_BUFFER, VertexData)
		ArrayBufferName2 = gl.addBuffer(GL.GL_ARRAY_BUFFER, VertexData2)
		ArrayBufferName3 = gl.addBuffer(GL.GL_ARRAY_BUFFER, VertexData3)
		ElementBufferName = gl.addBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ElementData)

		// images
		for (i in images.indices) {
			gl.glGenTextures(1, tmp, 0)
			TextureNames.add(tmp[0])
			gl.glActiveTexture(GL.GL_TEXTURE0 + i)
			gl.glEnable(GL.GL_TEXTURE_2D)
			gl.glBindTexture(GL2.GL_TEXTURE_2D, TextureNames[i])
			gl.glTexParameteri(
				GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER,
				GL.GL_NEAREST
			)
			gl.glTexParameteri(
				GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_NEAREST
			)
			gl.glTexParameteri(
				GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,  //                       GL2.GL_CLAMP);
				GL2.GL_REPEAT
			)
			gl.glTexParameteri(
				GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,  //                       GL2.GL_CLAMP);
				GL2.GL_REPEAT
			)
			val image: TextureImage = images[i]
			gl.glTexImage2D(
				GL2.GL_TEXTURE_2D, 0, GL.GL_RGBA8, image.width,
				image.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
				image.byteBuffer
			)
		}

		bindProgram(gl) {
			for (i in images.indices) {
				gl.glActiveTexture(GL.GL_TEXTURE0 + i)
				uniformTextures.add(
					gl.glGetUniformLocation(shader.id, "texture$i")
				)
				gl.glUniform1i(
					uniformTextures[i], i
				) //set activetexture number
				gl.glBindTexture(GL2.GL_TEXTURE_2D, TextureNames[i])
			}
		}
	}

	override fun display(gl: GL2ES2, mats: PMVMatrix, lightpos: Vec3<Float>, lightcolor: Vec3<Float>) {
		bindProgram(gl) {
			shader.setMatrixAndLight(gl, mats, lightpos, lightcolor)
			for (i in uniformTextures.indices) {
				glActiveTexture(GL.GL_TEXTURE0 + i)
				glBindTexture(GL2.GL_TEXTURE_2D, TextureNames[i])
				glUniform1i(uniformTextures[i], i)
			}
			glBindBuffer(GL.GL_ARRAY_BUFFER, ArrayBufferName)
			glVertexAttribPointer(
				VERTEXPOSITION, 3, GL.GL_FLOAT,
				false, 32, 0
			)
			glVertexAttribPointer(
				VERTEXNORMAL, 3, GL.GL_FLOAT,
				false, 32, NormalOffset.toLong()
			)
			glVertexAttribPointer(
				VERTEXTEXCOORD0, 2, GL.GL_FLOAT,
				false, 32, TexCoordOffset.toLong()
			)
			glBindBuffer(GL.GL_ARRAY_BUFFER, ArrayBufferName2)
			glVertexAttribPointer(
				VERTEXCOLOR, 4, GL.GL_FLOAT,
				false, 16, ColorOffset.toLong()
			)
			glBindBuffer(GL.GL_ARRAY_BUFFER, ArrayBufferName3)
			glVertexAttribPointer(
				VERTEXTANGENT, 3, GL.GL_FLOAT,
				false, 12, TangentOffset.toLong()
			)
			glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ElementBufferName)
			glEnableVertexAttribArray(VERTEXPOSITION)
			glEnableVertexAttribArray(VERTEXCOLOR)
			glEnableVertexAttribArray(VERTEXNORMAL)
			glEnableVertexAttribArray(VERTEXTEXCOORD0)
			glEnableVertexAttribArray(VERTEXTANGENT)
			glDrawElements(GL.GL_TRIANGLES, ElementCount, GL.GL_UNSIGNED_INT, 0)
			glDisableVertexAttribArray(VERTEXPOSITION)
			glDisableVertexAttribArray(VERTEXNORMAL)
			glDisableVertexAttribArray(VERTEXCOLOR)
			glDisableVertexAttribArray(VERTEXTEXCOORD0)
			glDisableVertexAttribArray(VERTEXTANGENT)
			glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0)
			glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
			for (i in uniformTextures.indices) {
				glActiveTexture(GL.GL_TEXTURE0 + i)
				glBindTexture(GL2.GL_TEXTURE_2D, TextureNames[i])
			}
		}

	}


	private fun setColor(offset: Int, color: Vec4<Float>) {
		VertexData2[offset * 4] = color.x
		VertexData2[offset * 4 + 1] = color.y
		VertexData2[offset * 4 + 2] = color.z
		VertexData2[offset * 4 + 3] = color.w
	}

	private fun setTangent(offset: Int, tangentv: Vec3<Float>) {
		VertexData3[offset * 3] = tangentv.x
		VertexData3[offset * 3 + 1] = tangentv.y
		VertexData3[offset * 3 + 2] = tangentv.z
	}
}
