package shaderView.data

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
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

internal class FileImage(fname: String) : TextureImage {
	private var data: ByteBuffer
	override var width = 0
	override var height = 0
	lateinit var tmpdata: ByteBuffer
	override val byteBuffer: ByteBuffer
		get() {
			data.rewind()
			return data
		}

	override fun getByteBufferOfLevel(level: Int): ByteBuffer {
		data.subSampleTo(tmpdata, width, height, level)
		return tmpdata
	}

	fun save() {
		val img = BufferedImage(
			width, height,  //BufferedImage.TYPE_3BYTE_BGR);
			BufferedImage.TYPE_4BYTE_ABGR
		)
		var r: Int
		var g: Int
		var b: Int
		var a: Int
		val buff = data
		buff.rewind()
		for (y in 0 until height) {
			for (x in 0 until width) {
				/*  in case of BGRA format for Texture */
				b = buff.get().toInt()
				g = buff.get().toInt()
				r = buff.get().toInt()
				a = buff.get().toInt()
				img.setRGB(x, y, a shl 8 or b shl 8 or g shl 8 or r)
			}
		}
		try {
			val file = File("out.png")
			ImageIO.write(img as RenderedImage, "png", file)
		} catch (ex: IOException) {
		}
	}

	init {
		var imagefile: File? = null
		val bimage: BufferedImage?
		var tmp: ByteBuffer? = null
		var tmpw = 0
		var tmph = 0
		try {
			imagefile = File(fname)
			bimage = ImageIO.read(imagefile)
			tmpw = bimage.width
			tmph = bimage.height
			tmp = ByteBuffer.allocate(tmpw * tmph * 4)
			tmpdata = ByteBuffer.allocate(tmpw * tmph * 4)
			for (y in 0 until tmph) {
				for (x in 0 until tmpw) {
					val c = bimage.getRGB(x, y)
					tmp.putInt(c and 0xff0000 shr 8 or (c and 0xff00 shl 8) or (c and 0xff shl 24) or 255)
				}
			}
			tmp.rewind()
		} catch (e: IOException) {
			System.err.println("$e: $imagefile")
			System.exit(1)
		} finally {
			data = tmp!!
			width = tmpw
			height = tmph
		}
	}
}
