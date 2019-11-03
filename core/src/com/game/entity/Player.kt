package com.game.entity

import com.badlogic.gdx.graphics.Color
import com.game.gamestate.World
import com.game.graphics.Canvas
import com.game.level.Modifiers
import com.game.maths.Angle
import com.game.maths.Maths
import com.game.maths.Vec
import com.game.random.Dice
import com.game.resources.AssetManagerWrapper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sign

class Player(world: World, modifiers: Array<String>, position: Vec): Playable(world, Vec(10f, 19f), modifiers, position) {

    val animationPath = "player"
    val animation = AssetManagerWrapper.INSTANCE.fetchAnimation(animationPath)

    var currentAnimation = "idle"
    var animationTime = 0f

    var wasOnGround = true

    override fun entityUpdateLate(delta: Float) {
        if (onGround && currentAnimation == "jump_nr") {
            currentAnimation = "idle"
            animationTime = 0f
        }

        super.entityUpdateLate(delta)

        val lastAnimation = currentAnimation
        currentAnimation = animationFromAction(action)

        if (currentAnimation != lastAnimation) {
            animationTime = 0f
        } else {
            animationTime += delta
        }

        if (velocity.x != 0f && onGround && framesAlive % 10 == 0) {
            world.queueSpawn(Particle(world, Vec(position.x, position.y - 8), Dice.FAIR.rollF(0.8f..1.2f), if (velocity > 0f) { Angle.HALF - Angle(Dice.FAIR.rollF(0f..0.2f)) } else { Angle(Dice.FAIR.rollF(0f..0.2f)) }) )
        }

        if (onGround && !wasOnGround) {
            for (i in 0..4) {
                world.queueSpawn(Particle(world, Vec(position.x, position.y - 8), Dice.FAIR.rollF(0.2f..0.6f), Angle.HALF - Angle(Dice.FAIR.rollF(0f..0.4f))))
                world.queueSpawn(Particle(world, Vec(position.x, position.y - 8), Dice.FAIR.rollF(0.2f..0.6f), Angle(Dice.FAIR.rollF(0f..0.4f))))
            }
        }

        wasOnGround = onGround
    }

    override fun draw(canvas: Canvas) {
        if (modifier(Modifiers.FADE)) {
            canvas.colour = Color(1f, 1f, 1f, Maths.clamp(cos(framesAlive / 30f) + 0.75f, 0f, 1f))
            canvas.drawRegionCentred(animation[currentAnimation].getKeyFrame(animationTime), position, xScale = if(facingRight) { 1f } else { -1f }, yScale = sign(gravity))
            canvas.colour = Color.WHITE
        } else {
            canvas.drawRegionCentred(animation[currentAnimation].getKeyFrame(animationTime), position, xScale = if(facingRight) { 1f } else { -1f }, yScale = sign(gravity))
        }

//        canvas.drawTexture(AssetManagerWrapper.INSTANCE.getTexture("debug.png"), position)
//        canvas.drawTexture(AssetManagerWrapper.INSTANCE.getTexture("debug.png"), position - extents.xComponent())
//        canvas.drawTexture(AssetManagerWrapper.INSTANCE.getTexture("debug.png"), position + extents.xComponent())
    }

    override fun onRemoved() {
        AssetManagerWrapper.INSTANCE.unload(animationPath)
    }

    override fun onSpawn() {

    }

    private fun animationFromAction(action: String) = when(action) {
        "jump"  -> "jump_nr"
        else    -> action
    }

}