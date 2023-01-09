package pt.isel.pdm.li51d.g9.drag.model


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import pt.isel.pdm.li51d.g9.drag.game.DrawingMode
import pt.isel.pdm.li51d.g9.drag.model.figures.Drawing
import pt.isel.pdm.li51d.g9.drag.model.figures.Position
import pt.isel.pdm.li51d.g9.drag.model.figures.Shape

interface DrawingListener{
    fun onDrawnNewShape(shape: Shape)
    fun onUndo()
}

class DrawingBoard: View{

    constructor(context: Context, attrs: AttributeSet)  : super(context, attrs)
    constructor(context: Context) : super(context)

    companion object{
        const val MAX_STROKE_WIDTH = 30f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val newHeight: Int

        val newWidth: Int = measuredWidth
        newHeight = newWidth

        setMeasuredDimension(newWidth, newHeight)
    }

    var listener : DrawingListener? = null

    var strokeWidth : Float = 1f
    set(value) { field = (value* MAX_STROKE_WIDTH)/100 } //stroke width is set in percentage

    var drawingMode : DrawingMode = DrawingMode.FREE

    private var currentDrawing : Drawing? = null
    private var currentShape : Shape? = null
    private val paint = Paint()

    init{
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 10f
    }

    fun undo(){
        listener?.onUndo()
        invalidate()
    }

    fun setDrawing(drawing: Drawing?) {
        currentDrawing=drawing
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        currentDrawing?.draw(canvas)
        currentShape?.draw(canvas)

        //ADD BORDER TO THE CANVAS
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!isEnabled) return false
        val x: Float = 2f * (event.x / this.width) - 1f
        val y: Float = 2f * (event.y / this.height) - 1f
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                currentShape = Shape.instanceOf(drawingMode, Position(x, y), strokeWidth)
            }
            MotionEvent.ACTION_MOVE -> {
                currentShape?.addPosition(Position(x, y))
            }
            MotionEvent.ACTION_UP -> {

                currentShape?.finalPosition = Position(x,y)
                listener?.onDrawnNewShape(currentShape!!)

                currentShape = null
            }
        }
        invalidate()
        return true
    }
}