package com.game.gamestate

import com.game.graphics.Canvas
import com.game.maths.Vec

abstract class GameState {

    private val canvas = Canvas()

    abstract fun update(delta: Float)

    abstract fun draw(canvas: Canvas)

    abstract fun onEnter()

    abstract fun onExit()

    fun render() {
        this.canvas.beginBatch()
        this.draw(this.canvas)
        this.canvas.endBatch()
    }

    fun resize(width: Int, height: Int) {
        this.canvas.updateViewport(width, height)
    }

    fun toScreenPos(worldPos: Vec) =
            this.canvas.project(worldPos)

    fun toWorldPos(screenPos: Vec) =
            this.canvas.unproject(screenPos)

}