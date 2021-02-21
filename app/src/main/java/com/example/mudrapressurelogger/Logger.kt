package com.example.mudrapressurelogger

class Logger constructor(capacity: Int = 10000){
    private var capacity: Int
    private var log: List<Float>

    init {
        this.log = listOf()
        this.capacity = capacity
    }
    fun add(value: Float){
        this.log += value
        if(this.log.size > capacity){
            this.log.drop(1)
        }
    }
    fun get(size: Int): List<Float>{
        return this.log.takeLast(size)
    }
}