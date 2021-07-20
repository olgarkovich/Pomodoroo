package com.example.pomodoro

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {}