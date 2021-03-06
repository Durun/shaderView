package shaderView

import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import shaderView.data.TextureImage
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun GLWindow.addWindowListener(listener: (WindowEvent?) -> Unit) {
	this.addWindowListener(object : WindowAdapter() {
		override fun windowDestroyNotify(e: WindowEvent?) = listener(e)
	})
}

fun GL2ES2.glShaderSource(shaderId: Int, src: String) {
	this.glShaderSource(shaderId, 1, arrayOf(src), arrayOf(src.length).toIntArray(), 0)
}

private fun GL2ES2.checkCompileError(shaderid: Int) {
	val compileStatus = IntArray(1)
	glGetShaderiv(shaderid, GL2ES2.GL_COMPILE_STATUS, compileStatus, 0)
	check(compileStatus[0] == GL.GL_TRUE) {
		val logMax = 8192
		val log = ByteArray(logMax)
		val logLength = IntArray(1)
		glGetShaderInfoLog(shaderid, logMax, logLength, 0, log, 0)
		log.decodeToString(0, logLength[0] - 1)
	}
}

fun GL2ES2.attachShader(programId: Int, type: Int, shaderCode: String) {
	val shaderId = glCreateShader(type)
	glShaderSource(shaderId, shaderCode)
	glCompileShader(shaderId)
	checkCompileError(shaderId)
	glAttachShader(programId, shaderId)
	glDeleteShader(shaderId)
}

private const val maxLogLength = 8192
fun GL2ES2.getProgramInfoLogString(programId: Int): String {
	val buffer = ByteArray(maxLogLength)
	val readLength = IntArray(1)
	glGetProgramInfoLog(programId, maxLogLength, readLength, 0, buffer, 0)
	return buffer.decodeToString(0, readLength[0] - 1)
}

const val FLOAT_BYTES = java.lang.Float.SIZE / 8
fun GL2ES2.addBuffer(target: Int, array: FloatArray): Int {
	val tmp = IntArray(1)
	glGenBuffers(1, tmp, 0)
	val bufferId = tmp[0]
	glBindBuffer(target, bufferId)
	glBufferData(target, array.size * FLOAT_BYTES.toLong(), FloatBuffer.wrap(array), GL.GL_STATIC_DRAW)
	glBindBuffer(target, 0)
	return bufferId
}

fun <R> GL2ES2.bindBuffer(target: Int, bufferId: Int, block: GL2ES2.() -> R): R {
	glBindBuffer(target, bufferId)
	val result = runCatching { this.block() }
	glBindBuffer(target, 0)
	return result.getOrThrow()
}

const val INT_BYTES = Integer.SIZE / 8
fun GL2ES2.addBuffer(target: Int, array: IntArray): Int {
	val tmp = IntArray(1)
	glGenBuffers(1, tmp, 0)
	val bufferId = tmp[0]
	bindBuffer(target, bufferId) {
		glBufferData(target, array.size * INT_BYTES.toLong(), IntBuffer.wrap(array), GL.GL_STATIC_DRAW)
	}
	return bufferId
}

fun <R> GL2ES2.bindTexture(target: Int, textureId: Int, block: GL2ES2.() -> R): R {
	glActiveTexture(textureId)
	glBindTexture(target, textureId)
	val result = runCatching { this.block() }
	glActiveTexture(textureId)
	glBindTexture(target, 0)
	return result.getOrThrow()
}

fun <R> GL2ES2.bindTextures(target: Int, textureIds: List<Int>, block: GL2ES2.() -> R): R {
	textureIds.forEach {
		glActiveTexture(it)
		glBindTexture(target, it)
	}
	val result = runCatching { this.block() }
	textureIds.forEach {
		glActiveTexture(it)
		glBindTexture(target, 0)
	}
	return result.getOrThrow()
}

fun GL2ES2.addTexture(target: Int, image: TextureImage): Int {
	val tmp = IntArray(1)
	glGenTextures(1, tmp, 0)
	val textureId = tmp[0]
	glActiveTexture(target)
	glEnable(GL.GL_TEXTURE_2D)
	glBindTexture(GL2.GL_TEXTURE_2D, textureId)
	glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
	glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
	glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT)
	glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT)
	glTexImage2D(
		GL2.GL_TEXTURE_2D, 0, GL.GL_RGBA8, image.width,
		image.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
		image.byteBuffer
	)
	return textureId
}