package com.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.game.gamestate.GameStateManager
import com.game.gamestate.TestState
import com.game.resources.AssetManagerWrapper

class Main: ApplicationAdapter() {

    val gsm = GameStateManager()

    override fun create() {
        AssetManagerWrapper.INSTANCE.initialise()
        gsm.queueState(TestState())
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        gsm.update(Gdx.graphics.deltaTime)
        AssetManagerWrapper.INSTANCE.waitLoadAssets() //replaced update()
        gsm.render()
    }

    override fun dispose() {
        gsm.onExit()
    }

}