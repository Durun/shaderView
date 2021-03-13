package shaderView.data

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun makePlane(sizeX: Float, sizeY: Float = sizeX, color: Vec4<Float> = Vec4(0.5f, 0.5f, 0.5f, 1f)): PolygonSet {
	val normal = Vec3(0f, 0f, -1f)
	val tangent = Vec3(-1f, 0f, 0f)
	val vertice = listOf(
		Vertex(Vec3(-sizeX, -sizeY, 0f), normal, color, Vec2(0f, 0f), tangent),
		Vertex(Vec3(sizeX, -sizeY, 0f), normal, color, Vec2(1f, 0f), tangent),
		Vertex(Vec3(sizeX, sizeY, 0f), normal, color, Vec2(1f, 1f), tangent),
		Vertex(Vec3(-sizeX, sizeY, 0f), normal, color, Vec2(0f, 1f), tangent)
	)
	val front = Polygon(vertice)
	val back = Polygon(vertice.reversed())
	return front + back
}

fun makeCylinder(
	n: Int,
	radius: Float,
	height: Float,
	color: Vec4<Float> = Vec4(0.5f, 0.5f, 0.5f, 1f),
	smooth: Boolean = false
): PolygonSet {
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
			),
			tangent = Vec3(-1f, 0f, 0f)
		)
	}
	val bottomVertices = topVertices.map {
		it.copy(
			position = it.position.copy(z = -it.position.z),
			normal = -it.normal,
			tangent = -it.tangent
		)
	}
	val top = Polygon(topVertices.asReversed())
	val bottom = Polygon(bottomVertices)
	val side = if (smooth) {
		topVertices.zip(bottomVertices)
			.flatMapIndexed { i, (vTop, vBottom) ->
				val (x, y) = vTop.position
				val r = sqrt(x * x + y * y)
				val normal = Vec3(x / r, y / r, 0f)
				val tangent = Vec3(y / r, -x / r, 0f)
				val textureX = if (i < n / 2) 2.0f * i / n else 2.0f * (n - i) / n
				listOf(
					vTop.copy(
						normal = normal,
						tangent = tangent,
						textureCoord = Vec2(textureX, 0f)
					),
					vBottom.copy(
						normal = normal,
						tangent = tangent,
						textureCoord = Vec2(textureX, 1f)
					)
				)
			}
			.let { Sheet(it, looped = true) }
	} else {
		topVertices
			.mapIndexed { i, _ ->
				val nextI = (i + 1) % n
				val v1 = topVertices[i]
				val v2 = topVertices[nextI]
				val v3 = bottomVertices[nextI]
				val v4 = bottomVertices[i]
				val (x, y) = (v1.position + v4.position) / 2f
				val r = sqrt(x * x + y * y)
				val normal = Vec3(x / r, y / r, 0f)
				val tangent = Vec3(y / r, -x / r, 0f)
				val textureX = if (i < n / 2) 2.0f * i / n else 2.0f * (n - i) / n
				val nextTextureX = if (nextI < n / 2) 2.0f * nextI / n else 2.0f * (n - nextI) / n
				val polygon: PolygonSet = Polygon(
					listOf(
						v1.copy(normal = normal, tangent = tangent, textureCoord = Vec2(textureX, 0f)),
						v2.copy(normal = normal, tangent = tangent, textureCoord = Vec2(nextTextureX, 0f)),
						v3.copy(normal = normal, tangent = tangent, textureCoord = Vec2(nextTextureX, 1f)),
						v4.copy(normal = normal, tangent = tangent, textureCoord = Vec2(textureX, 1f)),
					)
				)
				polygon
			}
			.reduce { acc, polygon -> acc + polygon }
	}
	return top + bottom + side
}