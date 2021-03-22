package com.example.mudrapressurelogger
import android.util.Log
import java.io.FileWriter
import java.io.IOException


class Logger constructor(capacity: Int = 10000, path: String){
    private var capacity: Int
    private var log: List<Float>
    private var isStart: Boolean
    private var path: String
    //private var fileWriter: FileWriter

    init {
        this.log = listOf()
        this.capacity = capacity
        //this.fileWriter = FileWriter("person.csv");
        this.isStart = false
        this.path = path
    }

    fun start(){
        this.isStart = true
    }

    fun stop(){
        this.isStart = false
        writeData()
    }

    fun writeData(){
        try{

            Log.d("Logger", " write data: ${this.path}")
            val fw = FileWriter(this.path)
            fw.write("person test");
            fw.close();
        }catch (e: IOException){
            Log.d("Logger", "error: ${e} ${this.path}")
        }
    }

    fun add(value: Float){
        if (this.isStart) {
            this.log += value
            if (this.log.size > capacity) {
                this.log.drop(1)
            }
        }
    }

    fun get(size: Int): List<Float>{
        return this.log.takeLast(size)
    }

    fun getStatus(): Boolean{
        return this.isStart
    }
}

class CSVWriter constructor(){
    private var log: List<Float>

    init {
        this.log = listOf()
    }
    fun add(value: Float){
    }
    fun get(size: Int): List<Float>{
        return this.log.takeLast(size)
    }
}