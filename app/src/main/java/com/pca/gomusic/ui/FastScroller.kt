package com.pca.gomusic.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.R
import java.util.*

class FastScroller : LinearLayout {
    private val scrollListener = ScrollListener()
    private lateinit var bubble: TextView
    private var handle: View? = null
    private lateinit var recyclerView: RecyclerView
    private var _height = 0
    private var currentAnimator: ObjectAnimator? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialise(context)
    }

    constructor(context: Context) : super(context) {
        initialise(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialise(context)
    }

    private fun initialise(context: Context) {
        orientation = HORIZONTAL
        clipChildren = false
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.recyclerview_fastscroller, this, true)
        bubble = findViewById(R.id.fastscroller_bubble)
        handle = findViewById(R.id.fastscroller_handle)
        bubble.visibility = View.INVISIBLE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _height = h
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < handle!!.x) return false
                if (currentAnimator != null) currentAnimator!!.cancel()
                if (bubble.visibility == View.INVISIBLE) showBubble()
                handle!!.isSelected = true
                val y = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handle!!.isSelected = false
                hideBubble()
                return true
            }
        }
        return super.performClick()
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(scrollListener)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun setRecyclerViewPosition(y: Float) {
        val itemCount = Objects.requireNonNull(recyclerView.adapter)?.itemCount
        val proportion: Float = if (handle!!.y == 0f) 0f else if (handle!!.y + handle!!.height >= height - TRACK_SNAP_RANGE) 1f else y / height.toFloat()
        val targetPos = itemCount?.minus(1)?.let { getValueInRange(it, (proportion * itemCount.toFloat()).toInt()) }
        if (recyclerView.layoutManager is StaggeredGridLayoutManager) {
            targetPos?.let { (recyclerView.layoutManager as StaggeredGridLayoutManager?)!!.scrollToPositionWithOffset(it, 0) }
        } else if (recyclerView.layoutManager is LinearLayoutManager) {
            targetPos?.let { (recyclerView.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(it, 0) }
        }
        //      recyclerView.oPositionWithOffset(targetPos);
        val bubbleText = targetPos?.let { (recyclerView.adapter as BubbleTextGetter?)!!.getTextToShowInBubble(it) }
        bubble.text = bubbleText

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentTheme = sharedPref.getString(Constant.THEME, "default")
        if (currentTheme.equals("mimi")){
            bubble.setTextColor(context.resources.getColor(R.color.white))
        }
    }

    private fun getValueInRange(max: Int, value: Int): Int {
        val minimum = 0.coerceAtLeast(value)
        return minimum.coerceAtMost(max)
    }

    private fun setBubbleAndHandlePosition(y: Float) {
        val bubbleHeight = bubble.height
        val handleHeight = handle!!.height
        handle!!.y = getValueInRange(height - handleHeight, (y - handleHeight / 2).toInt()).toFloat()
        bubble.y = getValueInRange(height - bubbleHeight - handleHeight / 2, (y - bubbleHeight).toInt()).toFloat()
    }

    private fun showBubble() {
        bubble.visibility = View.VISIBLE
        if (currentAnimator != null) currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubble, "$alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.start()
    }

    private fun hideBubble() {
        if (currentAnimator != null) currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubble, "$alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                bubble.visibility = View.INVISIBLE
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                bubble.visibility = View.INVISIBLE
                currentAnimator = null
            }
        })
        currentAnimator!!.start()
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            if (handle!!.isSelected) {
                return
            }
            val firstVisibleView = recyclerView.getChildAt(0)
            val firstVisiblePosition = recyclerView.getChildLayoutPosition(firstVisibleView)
            val visibleRange = recyclerView.childCount
            val lastVisiblePosition = firstVisiblePosition + visibleRange
            val itemCount = Objects.requireNonNull(recyclerView.adapter)!!.itemCount
            val position: Int
            position = if (firstVisiblePosition == 0) 0 else if (lastVisiblePosition == itemCount) itemCount else (firstVisiblePosition.toFloat() / (itemCount.toFloat() - visibleRange.toFloat()) * itemCount.toFloat()).toInt()
            val proportion = position.toFloat() / itemCount.toFloat()
            setBubbleAndHandlePosition(height * proportion)
        }
    }

    companion object {
        private const val BUBBLE_ANIMATION_DURATION = 100
        private const val TRACK_SNAP_RANGE = 5
    }
}