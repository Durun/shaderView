package shaderView.data

import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.file.Path
import javax.imageio.ImageIO

class TextureImage(image: BufferedImage) {
	val width: Int = image.width
	val height: Int = image.width
	val byteBuffer: ByteBuffer
		get() = data.apply { rewind() }

	private val data: ByteBuffer = ByteBuffer.allocate(height * width * 4)

	init {
		for (y in 0 until height) {
			for (x in 0 until width) {
				val c = image.getRGB(x, y)
				val r = c and 0x00ff0000 shr 8
				val g = c and 0x0000ff00 shl 8
				val b = c and 0x000000ff shl 24
				val a = 0x000000ff
				data.putInt(r or g or b or a)
			}
		}
		data.rewind()
	}
}

fun loadFileTexture(path: Path): TextureImage = TextureImage(ImageIO.read(path.toFile()))
