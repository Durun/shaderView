package shaderView.data

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

interface TextureImage {
	val width: Int
	val height: Int
	val byteBuffer: ByteBuffer?

	fun getByteBufferOfLevel(level: Int): ByteBuffer?
}

internal object TextureImageUtil {
	fun subsampling(
		`in`: ByteBuffer, out: ByteBuffer, originalw: Int,
		originalh: Int, level: Int
	) {
		`in`.rewind()
		out.rewind()
		val step = 4 * (1 shl level)
		var y = 0
		while (y < originalh) {
			var x = 0
			while (x < originalw) {
				val base = (y * originalw + x) * 4
				out.put(`in`[base])
				out.put(`in`[base + 1])
				out.put(`in`[base + 2])
				out.put(`in`[base + 3])
				x += 1 shl level
			}
			y += 1 shl level
		}
		out.rewind()
		`in`.rewind()
	}
}

class DotImage(override val width: Int, override val height: Int) : TextureImage {
	private val image: BufferedImage
	private val buff: ByteBuffer
	private val tmpbuff: ByteBuffer

	override val byteBuffer: ByteBuffer
		get() {
			buff.rewind()
			return buff
		}

	override fun getByteBufferOfLevel(level: Int): ByteBuffer {
		TextureImageUtil.subsampling(buff, tmpbuff, width, height, level)
		return tmpbuff
	}

	init {
		val graphics: Graphics2D
		image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		graphics = image.createGraphics()
		for (y in 0 until height) {
			for (x in 0 until width) {
				val v = ((1.0 + Math.sin(x / 8.0 * Math.PI)) *
						(1.0 + Math.sin(y / 8.0 * Math.PI)) / 4.0).toFloat()
				graphics.color = Color(v, v, v)
				graphics.drawLine(x, y, x, y)
			}
		}
		buff = ByteBuffer.allocate(height * width * 4)
		tmpbuff = ByteBuffer.allocate(height * width * 4)
		for (y in 0 until height) {
			for (x in 0 until width) {
				buff.putInt(image.getRGB(x, y) shl 8 or 255)
			}
		}
	}
}

