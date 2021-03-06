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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.game.audio.AudioManager
import com.game.audio.SoundCategory
import com.game.gameplay.Modifiers
import com.game.gamestate.World
import com.game.maths.Maths
import com.game.maths.Vec
import com.game.random.Dice
import com.game.resources.AssetManagerWrapper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

// TODO: short jumps / high jumps
abstract class Playable(world: World, size: Vec, val modifiers: Array<String>, initialPosition: Vec): PhysicsEntity(world, size, 0.2f, initialPosition) {

    protected var walkAcceleration = 0.2f
    protected var walkDeceleration = 0.4f
    protected var wallJumpDeceleration = 0.01f
    protected var brakeDeceleration = 0.6f
    protected var midairBrakeDeceleration = 1.0f
    protected var wallJumpBrakeDeceleration = 0.05f
    protected var walkSpeed = 3f
    protected var jumpSpeed = 4.5f
    protected var wallJumpHSpeed = 2.5f
    protected var wallJumpVSpeed = 5f
    protected var wallSlideGravity = 0.02f
    protected var terminalVelocity = 5f
    protected var wallSlideTerminalVelocity = 1.5f

    protected var touchingWallLeft = false
    protected var touchingWallRight = false
    protected var lastWallTouchDirection = 1
    protected var wallJumping = false
    protected var jumpInputBuffer = 0
    protected var coyoteTime = 0
    protected var wallCoyoteTime = 0
    protected var justJumped = false

    protected var action = "idle"
        private set
    protected var facingRight = true
        private set

    protected var timeAlive = 0f
        private set
    protected var framesAlive = 0
        private set

    var isDead = false
        private set

    init {
        if (modifier(Modifiers.INV_GRAVITY)) {
            gravity = -0.2f
        }

        if (modifier(Modifiers.HYPERSPEED)) {
            walkSpeed = 5f
            wallJumpVSpeed = 4.5f
            walkAcceleration = 0.4f
        }

        if (modifier(Modifiers.ICE_PHYSICS)) {
            walkAcceleration = 0.075f
            walkDeceleration = 0.05f
            brakeDeceleration = 0.075f
            midairBrakeDeceleration = 0.1f
        }
    }

    fun modifier(name: String) =
            name in modifiers

    fun kill() {
        isDead = true
    }

    override fun entityUpdateEarly(delta: Float) {
        super.entityUpdateEarly(delta)
        justJumped = false
    }

    override fun entityUpdateLate(delta: Float) {
        timeAlive += delta
        framesAlive += 1

        val inLeft = if(modifier(Modifiers.ONLY_JUMP_MOVE)) { !onGround } else { true } && if (modifier(Modifiers.INV_CONTROLS)) { Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) } else { Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT) }
        val inRight = if(modifier(Modifiers.ONLY_JUMP_MOVE)) { !onGround } else { true } && if (modifier(Modifiers.INV_CONTROLS)) { Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT) } else { Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) }
        val inJump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)

        checkTouchingWall()

        var wallSliding = !onGround && (touchingWallLeft || touchingWallRight) && velocity.y * sign(gravity) <= 0 && ((inRight && touchingWallRight) || (inLeft && touchingWallLeft))

        if (touchingWallLeft || touchingWallRight) {
            wallCoyoteTime = 8
            lastWallTouchDirection = if (touchingWallLeft) { 1 } else { -1 }
        } else {
            wallCoyoteTime--
        }

        if (onGround) {
            coyoteTime = 6
            wallJumping = false
            wallCoyoteTime = 0
        } else if(coyoteTime > 0) {
            coyoteTime--
        }

        if (inJump) {
            jumpInputBuffer = 20
        } else if (jumpInputBuffer > 0) {
            jumpInputBuffer--
        }

        if (inLeft != inRight) {
            if (onGround) {
                facingRight = inRight
            }
            val targetDirection = if (inLeft) { -1f } else { 1f }
            val acceleration = if (velocity.x == 0f || sign(velocity.x) == targetDirection) {
                walkAcceleration * targetDirection
            } else if (onGround) {
                brakeDeceleration * targetDirection
            } else if (wallJumping) {
                wallJumpBrakeDeceleration
            } else {
                midairBrakeDeceleration
            } * targetDirection
            val maxSpeed = if (wallJumping) {
                wallJumpHSpeed
            } else {
                walkSpeed
            }
            velocity = Vec(Maths.clamp(velocity.x + acceleration, -maxSpeed, maxSpeed), velocity.y)
        } else if (velocity.x != 0f) {
            val movingRight = velocity.x > 0f
            val deceleration = if (wallJumping) { wallJumpDeceleration } else { walkDeceleration }
            velocity = if (movingRight) {
                Vec(max(velocity.x - deceleration, 0f), velocity.y)
            } else {
                Vec(min(velocity.x + deceleration, 0f), velocity.y)
            }
        }

        val jumping = if (modifier(Modifiers.POGO)) {
            onGround
        } else {
            coyoteTime > 0 && jumpInputBuffer > 0
        }

        if (jumping) {
            coyoteTime = 0
            jumpInputBuffer = 0
            onGround = false
            justJumped = true

            AudioManager.playSound(AssetManagerWrapper.INSTANCE.getSound("jump.wav"),
                    SoundCategory.GAMEPLAY, 0.4f, Dice.FAIR.rollF(0.7f..1.3f))

            if (modifier(Modifiers.JUMP_INV_GRAVITY)) {
                gravity = -gravity
            } else {
                velocity = Vec(velocity.x, jumpSpeed * sign(gravity))
            }
        } else if (wallCoyoteTime > 0 && !onGround && jumpInputBuffer > 10) {
            coyoteTime = 0
            wallCoyoteTime = 0
            jumpInputBuffer = 0
            wallJumping = true
            onGround = false

            AudioManager.playSound(AssetManagerWrapper.INSTANCE.getSound("jump.wav"),
                    SoundCategory.GAMEPLAY, 0.4f, Dice.FAIR.rollF(0.7f..1.3f))

            wallSliding = false
            if (modifier(Modifiers.JUMP_INV_GRAVITY)) {
                gravity = -gravity
                velocity = Vec(wallJumpHSpeed * lastWallTouchDirection, velocity.y)
            } else {
                velocity = Vec(wallJumpHSpeed * lastWallTouchDirection, wallJumpVSpeed * sign(gravity))
            }
        } else {
            val acceleration = if (wallSliding) { wallSlideGravity } else { gravity }
            val max = if (wallSliding) { wallSlideTerminalVelocity } else { terminalVelocity }
            velocity = if (gravity < 0) {
                Vec(velocity.x, min(velocity.y + abs(acceleration), max))
            } else {
                Vec(velocity.x, max(velocity.y - acceleration, -max))
            }
        }

        if (!onGround && velocity.x != 0f) {
            facingRight = velocity.x > 0f
        }

        action = if(wallSliding) {
            "slide"
        } else if (!onGround) {
            "jump"
        } else if (onGround && velocity.x != 0f) {
            "walk"
        } else {
            "idle"
        }
    }

    private fun checkTouchingWall() {
        val checkDistance = Vec(0.0625f, 0f)
        touchingWallLeft = world.map.isSolid(world.map.toMapPos(position - extents.xComponent() - checkDistance))
        touchingWallRight = world.map.isSolid(world.map.toMapPos(position + extents.xComponent() + checkDistance))
    }

    fun forceJump(ySpeed: Float) {
        coyoteTime = 0
        wallCoyoteTime = 0
        jumpInputBuffer = 0
        wallJumping = false
        onGround = false
        velocity = Vec(velocity.x, ySpeed)
    }

    fun forceFlipGravity() {
        coyoteTime = 0
        wallCoyoteTime = 0
        jumpInputBuffer = 0
        wallJumping = true
        onGround = false
        gravity = -gravity
    }

//    abstract fun spawnWalkParticles()
//    abstract fun spawnLandParticles()

}