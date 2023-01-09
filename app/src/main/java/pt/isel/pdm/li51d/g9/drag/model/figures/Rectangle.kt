package pt.isel.pdm.li51d.g9.drag.model.figures

import android.graphics.Canvas
import pt.isel.pdm.li51d.g9.drag.game.DrawingMode

class Rectangle(position1: Position, strokeWidth: Float) : Shape(position1, strokeWidth) {
    override fun draw(canvas: Canvas) {
        val  initialX = map(initialPosition.x,0f,canvas.width.toFloat(), -1f,1f)
        val  initialY = map(initialPosition.y,0f,canvas.height.toFloat(), -1f,1f)
        val  finalX = map(finalPosition.x,0f,canvas.width.toFloat(), -1f,1f)
        val  finalY = map(finalPosition.y,0f,canvas.height.toFloat(), -1f,1f)
        canvas.drawRect(initialX, initialY,finalX,finalY,paint)
    }

    override fun toDrawingData(): ShapeData {
        val positions : List<Position> = listOf(initialPosition,finalPosition)
        return ShapeData(positions,strokeWidth, DrawingMode.RECT)
    }

}