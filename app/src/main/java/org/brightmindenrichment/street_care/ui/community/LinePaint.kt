package org.brightmindenrichment.street_care.ui.community

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.util.Extensions

class LinePaint: RecyclerView.ItemDecoration() {
    private val paint: Paint = Paint()

    init {
        paint.color = Color.BLACK
        paint.strokeWidth = 1f

    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount

         // Pixel value

        var density: Float = parent.resources.displayMetrics.density // Get device density

        //var dp = px / density
        var pxLeft = 45*density
        var pxCircleTop = 5*density
        var pxRadius = 5*density

        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1)

            val childType = parent.getChildViewHolder(child).itemViewType
            val nextChildType = parent.getChildViewHolder(nextChild).itemViewType

            if(childType == Extensions.TYPE_NEW_DAY){
                canvas.drawCircle(
                    (child.left + pxLeft).toFloat(),
                    (child.top + pxCircleTop).toFloat(),
                    pxRadius.toFloat(),
                    paint
                )
            }
            if(nextChildType == Extensions.TYPE_NEW_DAY){
                canvas.drawCircle(
                    (nextChild.left + pxLeft).toFloat(),
                    (nextChild.top + pxCircleTop).toFloat(),
                    pxRadius.toFloat(),
                    paint
                )
            }

            if(childType == Extensions.TYPE_DAY || childType == Extensions.TYPE_NEW_DAY){
                if(nextChildType == Extensions.TYPE_DAY || nextChildType == Extensions.TYPE_NEW_DAY){
                    canvas.drawLine(
                        (child.left + pxLeft).toFloat(),
                        (child.top).toFloat(),
                        (nextChild.left + pxLeft).toFloat(),
                        (nextChild.bottom).toFloat(),
                        paint
                    )
                }
                else{
                    canvas.drawLine(
                        (child.left + pxLeft).toFloat(),
                        (child.top).toFloat(),
                        (child.left + pxLeft).toFloat(),
                        (child.bottom).toFloat(),
                        paint
                    )
                }
            }

        }
    }
}