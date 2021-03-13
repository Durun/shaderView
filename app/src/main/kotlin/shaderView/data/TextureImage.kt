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
				data.putInt(c and 0xff0000 shr 8 or (c and 0xff00 shl 8) or (c and 0xff shl 24) or 255)
			}
		}
		data.rewind()
	}
}

fun loadFileTexture(path: Path): TextureImage = TextureImage(ImageIO.read(path.toFile()))
