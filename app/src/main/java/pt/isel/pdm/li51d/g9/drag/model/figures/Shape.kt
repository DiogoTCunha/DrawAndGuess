package pt.isel.pdm.li51d.g9.drag.model.figures

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pt.isel.pdm.li51d.g9.drag.ShapeDTO
import pt.isel.pdm.li51d.g9.drag.model.Input
import pt.isel.pdm.li51d.g9.drag.game.DrawingMode
import java.lang.IllegalStateException


@Parcelize
data class Position(val x : Float , val y : Float) : Parcelable

@Parcelize
class ShapeData(val positions : List<Position>, val strokeWidth: Float, val mode : DrawingMode) : Parcelable{
    companion object{
       fun fromShapeDTO( shape : ShapeDTO ) : ShapeData =
               ShapeData(shape.positions,shape.strokeWidth,DrawingMode.valueOf(shape.mode))
    }
}

abstract class Shape(val initialPosition: Position, val strokeWidth: Float) : Input() {
    var finalPosition: Position = initialPosition
    val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init{
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
    }
    abstract fun draw(canvas : Canvas)

    open fun addPosition(p : Position){
        finalPosition = p
    }




    abstract fun toDrawingData() : ShapeData


    companion object{
        fun map(input : Float,toMin : Float,toMax : Float, fromMin : Float, fromMax : Float) : Float{

            return ((input - fromMin)/(fromMax - fromMin)) * (toMax - toMin) +toMin
        }

        fun instanceOf(mode : DrawingMode, position1: Position, strokeWidth:Float) : Shape {
            return when(mode){
                DrawingMode.LINE -> Line(position1, strokeWidth)
                DrawingMode.CIRCLE -> Circle(position1, strokeWidth)
                DrawingMode.RECT -> Rectangle(position1, strokeWidth)
                DrawingMode.FREE -> Free(position1, strokeWidth)
            }
        }

        fun fromDrawingData(data : ShapeData) : Shape{
            val drawing = when(data.mode){
                DrawingMode.LINE -> Line(data.positions[0], data.strokeWidth)
                DrawingMode.CIRCLE -> Circle(data.positions[0], data.strokeWidth)
                DrawingMode.RECT -> Rectangle(data.positions[0],  data.strokeWidth)
                DrawingMode.FREE -> Free(data.positions[0],  data.strokeWidth)
            }

            for(i in 1 until data.positions.size){
                drawing.addPosition(data.positions[i])
            }

            return drawing
        }
    }
}