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
import com.game.gamestate.World
import com.game.graphics.Canvas
import com.game.maths.Angle
import com.game.maths.Vec
import com.game.resources.AssetManagerWrapper

class Particle(world: World, position: Vec, speed: Float, angle: Angle, val lifetime: Int = 30): Entity(world, Vec(2f, 2f), position) {

    private val texturePath = "particle.png"

    private var ticksLeft = lifetime

    init {
        AssetManagerWrapper.INSTANCE.loadTexture(texturePath)
        velocity = Vec(speed, angle)
    }

    override fun entityUpdateLate(delta: Float) {
        ticksLeft --
        if (ticksLeft <= 0) {
            retire()
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.colour = Color(1f, 1f, 1f, ticksLeft.toFloat() / lifetime.toFloat())
        canvas.drawTextureCentred(AssetManagerWrapper.INSTANCE.getTexture(texturePath), position)
        canvas.colour = Color.WHITE
    }

    override fun onSpawn() {

    }

    override fun onRemoved() {
        AssetManagerWrapper.INSTANCE.unload(texturePath)
    }

}