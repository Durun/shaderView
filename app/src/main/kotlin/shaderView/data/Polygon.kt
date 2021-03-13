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
        it + (p1.vertexArray.size) / Vertex.LENGTH
    }
}

class Polygon(vertice: List<Vertex>) : PolygonSet {
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
                    listOf(elements.first(), next, v)
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

class Sheet(vertice: List<Vertex>, looped: Boolean = false) : PolygonSet {
    override val vertexArray: FloatArray
    override val elementArray: IntArray

    init {
        val size = vertice.size
        val firstVertice = if (looped) vertice else vertice.dropLast(2)
        val triangles = firstVertice.mapIndexed { i, _ ->
            val v1 = i
            val v2 = (if (v1 % 2 == 0) i + 2 else i + 1) % size
            val v3 = (if (v1 % 2 == 0) i + 1 else i + 2) % size
            listOf(v1, v2, v3)
        }
        elementArray = triangles.flatten().toIntArray()
        vertexArray = vertice.toFloatArray()
    }
}