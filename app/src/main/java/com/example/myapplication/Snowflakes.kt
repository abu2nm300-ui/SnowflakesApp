package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

data class Snowflake(var x: Float,
                     var y: Float,
                     val velocity: Float,
                     val amplitude: Float,
                     val frequency: Float,
                     val radius: Float,
                     val color: Int)

lateinit var snow: Array<Snowflake>
val paint = Paint()
var h = 1000; var w = 1000


open class Snowflakes(ctx: Context) : View(ctx) {
    lateinit var controller: MoveTask
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLUE)

        for (s in snow) {
            paint.color = s.color
            canvas.drawCircle(s.x, s.y, s.radius, paint)
        }

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top; w = right - left

        val r = Random(0)
        r.nextFloat()

        snow = Array(10) { Snowflake(x = r.nextFloat() * w,
            y =  r.nextFloat() * h,
            velocity = 15 + 10 * r.nextFloat(),
            amplitude = r.nextFloat(),
            frequency = r.nextFloat(),
            radius = 30 + 20 * r.nextFloat(),
            color = Color.rgb((255 * r.nextFloat()).toInt(),
                (255 * r.nextFloat()).toInt(),
                (255 * r.nextFloat()).toInt()))
        }

        Log.d("mytag", "snow: " + snow.contentToString())
    }

    fun moveSnowflakes() {
        for (s in snow) {
            s.y += s.velocity
            if (s.y > h) {
                s.y -= h
            }
            s.x += (s.amplitude * sin(s.frequency * s.y)).toFloat()
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //moveSnowflakes()
        //invalidate() // перерисовать View
        controller = MoveTask(this)
        controller.start()

        return false

    }

    class MoveTask(private val snowflakes: Snowflakes) {
        private var asyncTask: MoveAsyncTask? = null

        fun start(delayMs: Long = 200L) {
            stop()
            asyncTask = MoveAsyncTask(snowflakes, delayMs).also { it.execute() }
        }

        fun stop() {
            asyncTask?.cancel(true)
            asyncTask = null
        }

        private class MoveAsyncTask(
            private val snowflakes: Snowflakes,
            private val delayMs: Long
        ) : AsyncTask<Void, Void, Void>() {

            @Volatile
            private var running = true

            override fun doInBackground(vararg params: Void?): Void? {
                while (running && !isCancelled) {
                    try {
                        Thread.sleep(delayMs)
                    } catch (e: InterruptedException) {
                        break
                    }
                    if (isCancelled) break
                    snowflakes.moveSnowflakes()
                }
                return null
            }

            override fun onCancelled() {
                running = false
            }
        }
    }
}