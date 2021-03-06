/*
 * Copyright (C) 2019 Thomas Panton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.game.entity

import com.badlogic.gdx.graphics.Color
import com.game.gameplay.Modifiers
import com.game.gamestate.World
import com.game.graphics.Canvas
import com.game.maths.Angle
import com.game.maths.Maths
import com.game.maths.Vec
import com.game.random.Dice
import com.game.resources.AssetManagerWrapper
import com.game.settings.Settings
import kotlin.math.cos
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

        if (Settings.getBool("particles")) {
            val gravDir = sign(gravity)
            val particleY = position.y - (8 * gravDir)

            if (velocity.x != 0f && onGround && framesAlive % 10 == 0) {
                world.queueSpawn(Particle(world, Vec(position.x, particleY), Dice.FAIR.rollF(0.8f..1.2f), if (velocity > 0f) {
                    Angle.HALF - Angle(Dice.FAIR.rollF(0f..0.2f) * gravDir)
                } else {
                    Angle(Dice.FAIR.rollF(0f..0.2f) * gravDir)
                }))
            }

            if ((onGround || justJumped) && !wasOnGround) {
                for (i in 0..4) {
                    world.queueSpawn(Particle(world, Vec(position.x, particleY), Dice.FAIR.rollF(0.2f..0.6f), Angle.HALF - Angle(Dice.FAIR.rollF(0f..0.4f) * gravDir)))
                    world.queueSpawn(Particle(world, Vec(position.x, particleY), Dice.FAIR.rollF(0.2f..0.6f), Angle(Dice.FAIR.rollF(0f..0.4f) * gravDir)))
                }
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