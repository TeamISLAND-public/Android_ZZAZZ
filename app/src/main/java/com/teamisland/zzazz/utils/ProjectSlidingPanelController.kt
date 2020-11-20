package com.teamisland.zzazz.utils

import android.view.MotionEvent
import kotlin.math.abs

internal class ProjectSlidingPanelController {
    private enum class TouchState {
        NONE,
        CONTACT,
        SLIDING
    }

    private var posXAnchor = 0f
    private var mode = TouchState.NONE
    private var newDist = 0f
    private var oldDist = 0f

    var onMove: (Float) -> Unit = { _ -> }
    var onMultitouch: (Float, Float) -> Unit = { _, _ -> }
    var onTouchStart: () -> Unit = { -> }

    fun handleEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleTouchStart(event.x)
            MotionEvent.ACTION_UP -> mode = TouchState.NONE
            MotionEvent.ACTION_POINTER_UP -> mode = TouchState.CONTACT
            MotionEvent.ACTION_POINTER_DOWN -> handleSlidingStart(distance(event))
            MotionEvent.ACTION_MOVE -> handleMovement(event)
        }
        return true
    }

    private fun distance(event: MotionEvent): Float = abs(event.getX(0) - event.getX(1))

    private fun handleMovement(event: MotionEvent) {
        when (mode) {
            TouchState.CONTACT -> handleMove(event)
            TouchState.SLIDING -> handleMultitouch(event)
            TouchState.NONE -> {
                /* no-op */
            }
        }
    }

    private fun handleMultitouch(event: MotionEvent) {
        newDist = distance(event)
        onMultitouch(newDist, oldDist)
        oldDist = newDist
    }

    private fun handleMove(event: MotionEvent) {
        val deltaPos = event.x - posXAnchor
        onMove(deltaPos)
    }

    private fun handleSlidingStart(distance: Float) {
        newDist = distance
        oldDist = distance
        mode = TouchState.SLIDING
    }

    private fun handleTouchStart(xPos: Float) {
        mode = TouchState.CONTACT
        posXAnchor = xPos
        onTouchStart()
    }
}