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

import com.game.gamestate.World
import com.game.graphics.Canvas
import com.game.maths.Vec

abstract class Entity(val world: World, val size: Vec, initialPosition: Vec) {

    val extents = this.size / 2f

    var position = initialPosition
    var velocity = Vec()

    var retired = false
        private set

    abstract fun draw(canvas: Canvas)

    abstract fun onRemoved()

    abstract fun onSpawn()

    open fun entityUpdateEarly(delta: Float) {}

    open fun entityUpdateLate(delta: Float) {}

    open fun handleCollisions(nextH: Vec, nextV: Vec, translation: Vec): Vec =
            translation

    fun update(delta: Float) {
        this.entityUpdateEarly(delta)

        this.velocity = this.handleCollisions(
                Vec(this.position.x + this.velocity.x, this.position.y),
                Vec(this.position.x, this.position.y + this.velocity.y),
                this.velocity
        )

        this.position += this.velocity

        this.entityUpdateLate(delta)
    }

    fun retire() {
        retired = true
    }

    fun intersects(other: Entity, futurePosition: Vec = this.position): Boolean =
            futurePosition.x - this.extents.x < other.position.x + other.extents.x &&
                futurePosition.x + this.extents.x > other.position.x - other.extents.x &&
                futurePosition.y - this.extents.y < other.position.y + other.extents.y &&
                futurePosition.y + this.extents.y > other.position.y - other.extents.y

    fun getOccupyingTilesH(futurePosition: Vec = this.position) =
            this.world.map.toMapX(futurePosition.x - this.extents.x)..this.world.map.toMapX(futurePosition.x + this.extents.x)

    fun getOccupyingTilesV(futurePosition: Vec = this.position) =
            this.world.map.toMapX(futurePosition.y - this.extents.y)..this.world.map.toMapX(futurePosition.y + this.extents.y)

}