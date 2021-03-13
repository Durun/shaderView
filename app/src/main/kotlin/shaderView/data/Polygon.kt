package shaderView.data

interface PolygonSet {
    val vertexArray: FloatArray
    val elementArray: IntArray

    operator fun plus(other: PolygonSet): PolygonSet {
        return MultiPolygon(this, other)
    }
}

class MultiPolygon(p1: PolygonSet, p2: PolygonSet) : PolygonSet {
    override val vertexArray: FloatArray = p1.vertexArray + p2.vertexArray
    override val elementArray: IntArray = p1.elementArray + p2.elementArray.map {
        it + (p1.vertexArray.size) / 12
    }
}

data class Polygon(
    val vertice: List<Vertex>
) : PolygonSet {
    override val vertexArray: FloatArray
    override val elementArray: IntArray

    init {
        val buffer = vertice.distinct()
        val elements = vertice.map { buffer.indexOf(it) }

        // convert to triangles
        val triangles = elements.mapIndexedNotNull { i, v ->
            when (i) {
                0 -> null
                elements.lastIndex -> null
                else -> {
                    val next = elements[i + 1]
                    listOf(elements.first(), v, next)
                }
            }
        }

        elementArray = triangles.flatten().toIntArray()
        vertexArray = buffer.toFloatArray()
    }

    companion object {
        fun of(vararg vertice: Vertex): Polygon = Polygon(vertice.toList())
    }
}

data class Sheet(
    val vertice: List<Vertex>
) : PolygonSet {
    override val vertexArray: FloatArray
        get() = TODO("Not yet implemented")
    override val elementArray: IntArray
        get() = TODO("Not yet implemented")
}