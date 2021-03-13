package shaderView.data

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun makePlane(sizeX: Float, sizeY: Float = sizeX, color: Vec4<Float> = Vec4(0.5f, 0.5f, 0.5f, 1f)): PolygonSet {
	val normal = Vec3(0f, 0f, -1f)
	val vertice = listOf(
		Vertex(Vec3(-sizeX, -sizeY, 0f), normal, color, Vec2(0f, 0f)),
		Vertex(Vec3(sizeX, -sizeY, 0f), normal, color, Vec2(1f, 0f)),
		Vertex(Vec3(sizeX, sizeY, 0f), normal, color, Vec2(1f, 1f)),
		Vertex(Vec3(-sizeX, sizeY, 0f), normal, color, Vec2(0f, 1f))
	)
	val front = Polygon(vertice)
	val back = Polygon(vertice.reversed())
	return front + back
}

fun makeCylinder(n: Int, radius: Float, height: Float, color: Vec4<Float> = Vec4(0.5f, 0.5f, 0.5f, 1f)): PolygonSet {
	val angles = (0 until n).map { (it * 2 * Math.PI / n) }
	val topVertices = angles.map { angle ->
		Vertex(
			position = Vec3(
				x = (radius * cos(angle)).toFloat(),
				y = (radius * sin(angle)).toFloat(),
				z = -height / 2f
			),
			normal = Vec3(0f, 0f, -1f),
			color = color,
			textureCoord = Vec2(
				x = (-0.5 * cos(angle) + 0.5f).toFloat(),
				y = (0.5 * sin(angle) + 0.5f).toFloat()
			)
		)
	}
	val bottomVertices = topVertices.map {
		it.copy(
			position = it.position.copy(z = -it.position.z),
			normal = -it.normal
		)
	}
	val top = Polygon(topVertices.asReversed())
	val bottom = Polygon(bottomVertices)
	val side = topVertices.zip(bottomVertices)
		.flatMapIndexed { i, (vTop, vBottom) ->
			val (x, y) = vTop.position
			val r = sqrt(x * x + y * y)
			val normal = Vec3(x / r, y / r, 0f)
			val textureX = if (i < n / 2) 2.0f * i / n else 2.0f * (n - i) / n
			listOf(
				vTop.copy(
					normal = normal,
					textureCoord = Vec2(textureX, 0f)
				),
				vBottom.copy(
					normal = normal,
					textureCoord = Vec2(textureX, 1f)
				)
			)
		}
		.let { Sheet(it, looped = true) }
	return top + bottom + side
}