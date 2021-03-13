package shaderView.data

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.file.Path
import javax.imageio.ImageIO

interface TextureImage {
	val width: Int
	val height: Int
	val byteBuffer: ByteBuffer
	fun getByteBufferOfLevel(level: Int): ByteBuffer
}

internal fun ByteBuffer.subSampleTo(output: ByteBuffer, width: Int, height: Int, level: Int) {
	this.rewind()
	output.rewind()
	var y = 0
	while (y < height) {
		var x = 0
		while (x < width) {
			val base = (y * width + x) * 4
			output.put(this[base])
			output.put(this[base + 1])
			output.put(this[base + 2])
			output.put(this[base + 3])
			x += 1 shl level
		}
		y += 1 shl level
	}
	output.rewind()
	this.rewind()
}

internal class DotImage(override val width: Int, override val height: Int) : TextureImage {
	private val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
	private val buff: ByteBuffer = ByteBuffer.allocate(height * width * 4)
	private val tmpbuff: ByteBuffer = ByteBuffer.allocate(height * width * 4)

	override val byteBuffer: ByteBuffer
		get() = buff.apply { rewind() }

	override fun getByteBufferOfLevel(level: Int): ByteBuffer {
		buff.subSampleTo(tmpbuff, width, height, level)
		return tmpbuff
	}

	init {
		val graphics: Graphics2D = image.createGraphics()
		for (y in 0 until height) {
			for (x in 0 until width) {
				val v = ((1.0 + Math.sin(x / 8.0 * Math.PI)) *
						(1.0 + Math.sin(y / 8.0 * Math.PI)) / 4.0).toFloat()
				graphics.color = Color(v, v, v)
				graphics.drawLine(x, y, x, y)
			}
		}
		for (y in 0 until height) {
			for (x in 0 until width) {
				buff.putInt(image.getRGB(x, y) shl 8 or 255)
			}
		}
	}
}

internal class FileImage(imageFile: Path) : TextureImage {
	private val data: ByteBuffer
	override val width: Int
	override val height: Int
	override val byteBuffer: ByteBuffer
		get() = data.apply { rewind() }

	override fun getByteBufferOfLevel(level: Int): ByteBuffer {
		data.subSampleTo(tmpdata, width, height, level)
		return tmpdata
	}

	init {
		val bimage = ImageIO.read(imageFile.toFile())
		width = bimage.width
		height = bimage.height
		data = ByteBuffer.allocate(bimage.width * bimage.height * 4)
		for (y in 0 until height) {
			for (x in 0 until width) {
				val c = bimage.getRGB(x, y)
				data.putInt(c and 0xff0000 shr 8 or (c and 0xff00 shl 8) or (c and 0xff shl 24) or 255)
			}
		}
		data.rewind()
	}

	private val tmpdata = ByteBuffer.allocate(width * height * 4)
}
