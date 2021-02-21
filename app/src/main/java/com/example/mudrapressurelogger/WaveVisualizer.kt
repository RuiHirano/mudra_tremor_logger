package com.example.mudrapressurelogger

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class WaveVisualizer : SurfaceView, SurfaceHolder.Callback, Runnable {

    val _paint = Paint()
    var _buffer: List<Float> = listOf()
    var _holder: SurfaceHolder
    var _thread: Thread? = null
    var _logger: Logger? = null
    var _zscore: ZScore = ZScore()

    override fun run() {
        while(_thread != null){
            doDraw(_holder)
        }
    }

    constructor(context: Context, surface: SurfaceView)
            : super(context) {

        _holder = surface.holder
        _holder.addCallback(this)

        // Logger: 値を保持する
        _logger = Logger(10000)

        // 線の太さ、アンチエイリアス、色、とか
        _paint.strokeWidth  = 2f
        _paint.isAntiAlias  = true
        _paint.color        = Color.WHITE

        // この2つを書いてフォーカスを当てないとSurfaceViewが動かない？
//        isFocusable = true
//        requestFocus()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if(holder != null){
            val canvas = holder.lockCanvas()

            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        _thread = Thread(this)
        _thread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        _thread = null
    }

    fun update(buffer: ShortArray, size: Int) {
        //_buffer = buffer.copyOf(size)
//      postInvalidate()
    }

    fun add(value: Float) {
        _logger?.add(value)
        var data = _logger?.get(1000)
        Log.d("logger",  "size: ${data?.size}")
        if(data != null){
            // 標準化を行う
            //_buffer = _zscore.standardize(data).takeLast(1000)
            _buffer = data
        }
    }

    private fun doDraw(holder: SurfaceHolder) {
        if(_buffer.size == 0){
            return
        }

        try {
            val canvas: Canvas = holder.lockCanvas()

            if (canvas != null) {
                canvas.drawColor(Color.BLACK)

                val baseLine: Float = canvas.height / 2f
                val magnification = 1 // 倍率
                var oldX: Float = 0f
                var oldY: Float = baseLine
                Log.d("logger",  "size2: ${_buffer?.size}")

                for ((index, value) in _buffer.withIndex()) {
                    val x: Float = canvas.width.toFloat() / _buffer.size.toFloat() * index.toFloat()
                    val y: Float = _buffer[index] * magnification + baseLine

                    canvas.drawLine(oldX, oldY, x, y, _paint)

                    oldX = x
                    oldY = y
                }

                _buffer = listOf()

                holder.unlockCanvasAndPost(canvas)
            }
        }catch(e: Exception){
            Log.e(this.javaClass.name, "doDraw", e)
        }
    }
}