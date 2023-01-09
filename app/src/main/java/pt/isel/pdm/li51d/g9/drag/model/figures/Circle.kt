package pt.isel.pdm.li51d.g9.drag.model.figures

import android.graphics.Canvas
import pt.isel.pdm.li51d.g9.drag.game.DrawingMode


class Circle(position1: Position, strokeWidth: Float) : Shape(position1,
    strokeWidth
) {

    override fun draw(canvas: Canvas) {
        val  initialX = map(initialPosition.x,0f,canvas.width.toFloat(), -1f,1f)
        val  initialY = map(initialPosition.y,0f,canvas.height.toFloat(), -1f,1f)
        val radius = (getRadius() * kotlin.math.sqrt((canvas.width*canvas.width+canvas.height*canvas.height).toDouble()).toFloat())/2
        canvas.drawCircle(initialX, initialY,radius,paint)
    }

    private fun getRadius(): Float {
            return kotlin.math.sqrt((initialPosition.x - finalPosition.x) * (initialPosition.x - finalPosition.x)
                    + (initialPosition.y - finalPosition.y) * (initialPosition.y - finalPosition.y))
    }


    override fun toDrawingData(): ShapeData {
        val positions : List<Position> = listOf(initialPosition,finalPosition)
        return ShapeData(positions,strokeWidth, DrawingMode.CIRCLE)
    }

}