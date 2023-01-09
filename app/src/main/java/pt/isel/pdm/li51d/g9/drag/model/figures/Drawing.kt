package pt.isel.pdm.li51d.g9.drag.model.figures

import android.graphics.Canvas
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Drawing(val shapes : MutableList<ShapeData> = ArrayList()) : Parcelable{

    fun addShape(shape : Shape) = shapes.add(shape.toDrawingData())

    fun draw(canvas : Canvas) {
        for(shape in shapes)
            Shape.fromDrawingData(shape).draw(canvas)
    }



    fun removeLastShape() {
        if(shapes.size > 0) shapes.removeLast()
    }
}