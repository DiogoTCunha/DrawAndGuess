package pt.isel.pdm.li51d.g9.drag.model.figures

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import pt.isel.pdm.li51d.g9.drag.game.DrawingMode


class Free(position1: Position, strokeWidth: Float) : Shape(position1, strokeWidth) {
    private val path = Path()
    private val positions = ArrayList<Position>()
    init {
        path.moveTo(position1.x, position1.y)
        positions.add(initialPosition)
    }

    override fun draw(canvas: Canvas) {
        val scaleMatrix = Matrix()
        val drawableRect = RectF(-1f, -1f, 1f, 1f)
        val viewRect = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())

        //scaleMatrix.setScale(canvas.width.toFloat() / 2, canvas.height.toFloat() / 2)
        //scaleMatrix.setTranslate(canvas.width.toFloat() / 2, canvas.height.toFloat() / 2)
        val pathToDraw = Path()
        scaleMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
        path.transform(scaleMatrix, pathToDraw)
        canvas.drawPath(pathToDraw, paint)
    }

    override fun addPosition(p: Position) {
        path.lineTo(p.x, p.y)
        positions.add(p)
    }

    override fun toDrawingData(): ShapeData {
        return ShapeData(positions, strokeWidth, DrawingMode.FREE)
    }

}