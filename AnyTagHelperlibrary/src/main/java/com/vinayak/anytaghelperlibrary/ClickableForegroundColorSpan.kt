/* 
* Jujare Vinayak Inc., Android Application
* Copyright (C)  Jujare Vinayak Inc.
*
* The Jujare Vinayak's Application is the private property of
* Jujare Vinayak Inc. Any distribution of this software
* is unlawful and prohibited.
*/
package com.vinayak.anytaghelperlibrary

import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt


/**
 * Created by Jujare Vinayak on 08/07/2021.
 *
 *
 *
 * Copyright (C) 2021 Jujare Vinayak Inc.
 *
 * This application is the private property of Jujare Vinayak Inc.
 * Any distribution of this software is unlawful and prohibited.
 */
class ClickableForegroundColorSpan(@ColorInt color: Int, listener: OnHashTagClickListener?) :
    ClickableSpan() {
    private val mOnHashTagClickListener: OnHashTagClickListener? = listener

    interface OnHashTagClickListener {
        fun onHashTagClicked(hashTag: String?)
    }

    private val mColor: Int = color
    override fun updateDrawState(ds: TextPaint) {
        ds.color = mColor
    }

    override fun onClick(widget: View) {
        val text = (widget as TextView).text
        val s = text as Spanned
        val start = s.getSpanStart(this)
        val end = s.getSpanEnd(this)
        mOnHashTagClickListener!!.onHashTagClicked(
            text.subSequence(
                start  /*use start+ 1 to skip "#(any tag)" sign*/,
                end
            ).toString()
        )
    }

    init {
        if (mOnHashTagClickListener == null) {
            throw NullPointerException("Constructor click listener not specified.")
        }
    }
}
