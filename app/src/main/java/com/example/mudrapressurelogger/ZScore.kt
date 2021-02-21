package com.example.mudrapressurelogger

import android.R.attr.x


class ZScore {
    fun standardize(data: List<Float>): List<Float>{
        var mean = calcMean(data)
        var variance = calcVariance(data, mean)
        var sd = calcStandardDeviation(variance)
        var standardizedData: List<Float> = listOf()
        for (value in data) {
            standardizedData += (value-mean)/sd
        }
        return standardizedData
    }

    // 平均
    fun calcMean(data: List<Float>): Float{
        var sum = 0.0
        var n = data.size
        for (value in data) {
            sum += value //              和を求める
        }
        return (sum / n).toFloat()
    }

    // 分散
    fun calcVariance(data: List<Float>, mean: Float): Float{
        var sum = 0.0
        var n = data.size
        for (value in data) {
            sum += Math.pow((value - mean).toDouble(), 2.toDouble()).toFloat() //              和を求める
        }
        return (sum / n).toFloat()
    }

    // 標準偏差
    fun calcStandardDeviation(variance: Float): Float{
        return Math.sqrt(variance.toDouble()).toFloat()
    }
}