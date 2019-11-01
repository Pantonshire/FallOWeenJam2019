package com.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.game.maths.Angle
import com.game.maths.Vec

class Canvas(val resX: Float = 640f, val resY: Float = 360f) {

    private val spriteBatch = SpriteBatch()
    private val camera = OrthographicCamera()

    private var viewport = FitViewport(resX, resY, camera)

    init {
        this.updateViewport(Gdx.graphics.width, Gdx.graphics.height)
    }

    fun project(worldPos: Vec) =
            Vec(this.camera.project(worldPos.toVector3()))

    fun unproject(screenPos: Vec) =
            Vec(this.camera.unproject(screenPos.toVector3()))

    fun updateViewport(width: Int, height: Int) {
        this.viewport.update(width, height, true)
    }

    fun getXDisplayRange(): ClosedFloatingPointRange<Float> {
        val hWidth = this.viewport.worldWidth / 2f
        return (this.camera.position.x - hWidth)..(this.camera.position.x + hWidth)
    }

    fun getYDisplayRange(): ClosedFloatingPointRange<Float> {
        val hHeight = this.viewport.worldHeight / 2f
        return (this.camera.position.y - hHeight)..(this.camera.position.y + hHeight)
    }

    fun beginBatch() {
        this.camera.update()
        this.spriteBatch.projectionMatrix = camera.combined
        this.spriteBatch.begin()
    }

    fun endBatch() {
        this.spriteBatch.end()
    }

    fun dispose() {
        this.spriteBatch.dispose()
    }

    fun drawText(
            text: String,
            position: Vec,
            font: BitmapFont,
            colour: Color = Color.BLACK,
            centreX: Boolean = false,
            centreY: Boolean = false,
            scale: Float = 1f
    ) {
        font.data.setScale(scale, scale)
        val layout = GlyphLayout(font, text)
        val xOffset = if (centreX) { -layout.width / 2f } else { 0f }
        val yOffset = if (centreY) { layout.height / 2f } else { layout.height }
        font.color = colour
        font.draw(this.spriteBatch, text, position.x + xOffset, position.y + yOffset)
    }

    fun drawTexture(
            texture: Texture,
            position: Vec,
            width: Float = texture.width.toFloat(),
            height: Float = texture.height.toFloat()
    ) {
        this.spriteBatch.draw(texture, position.x, position.y, width, height)
    }

    fun drawTextureCentred(
            texture: Texture,
            position: Vec,
            width: Float = texture.width.toFloat(),
            height: Float = texture.height.toFloat()
    ) {
        drawTexture(texture, position - Vec(texture.width / 2f, texture.height / 2f), width, height)
    }

    fun drawRegion(
            region: TextureRegion,
            position: Vec,
            xScale: Float = 1f,
            yScale: Float = 1f,
            rotation: Angle = Angle.ZERO,
            width: Float = region.regionWidth.toFloat(),
            height: Float = region.regionHeight.toFloat(),
            originX: Float = width / 2f,
            originY: Float = height / 2f
    ) {
        this.spriteBatch.draw(region, position.x, position.y, originX, originY, width, height, xScale, yScale, rotation.libGDXForm())
    }

    fun drawRegionCentred(
            region: TextureRegion,
            position: Vec,
            xScale: Float = 1f,
            yScale: Float = 1f,
            rotation: Angle = Angle.ZERO,
            width: Float = region.regionWidth.toFloat(),
            height: Float = region.regionHeight.toFloat(),
            originX: Float = width / 2f,
            originY: Float = height / 2f
    ) {
        drawRegion(region, position - Vec(region.regionWidth / 2f, region.regionHeight / 2f), xScale, yScale, rotation, width, originX, originY)
    }

    fun getVisibleRange(): Pair<ClosedFloatingPointRange<Float>, ClosedFloatingPointRange<Float>> {
        val centre = this.camera.position
        val hWidth = Gdx.graphics.width / 2f
        val hHeight = Gdx.graphics.height / 2f
        return Pair((centre.x - hWidth)..(centre.x + hWidth), (centre.y - hHeight)..(centre.y + hHeight))
    }

}