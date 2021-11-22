/* 
* Jujare Vinayak Inc., Android Application
* Copyright (C)  Jujare Vinayak Inc.
*
* The Jujare Vinayak's Application is the private property of
* Jujare Vinayak Inc. Any distribution of this software
* is unlawful and prohibited.
*/
package com.vinayak.anytaghelperlibrary

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import java.util.ArrayList
import java.util.LinkedHashSet


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
class HashTagHelper private constructor(
    color: Int,
    listener: OnHashTagClickListener,
    additionalHashTagCharacters: CharArray?
) :
    ClickableForegroundColorSpan.OnHashTagClickListener {
    /**
     * If this is not null then  all of the symbols in the List will be considered as valid symbols of hashtag
     * For example :
     * mAdditionalHashTagChars = {'$','_','-'}
     * it means that hashtag: "#this_is_hashtag-with$dollar-sign" will be highlighted.
     *
     * Note: if mAdditionalHashTagChars would be "null" only "#this" would be highlighted
     *
     */
    private val mAdditionalHashTagChars: MutableList<Char>
    private var mTextView: TextView? = null
    private val mHashTagWordColor: Int = color
    private val mOnHashTagClickListener: OnHashTagClickListener?

    object Creator {
        fun create(color: Int, listener: OnHashTagClickListener): HashTagHelper {
            return HashTagHelper(color, listener, null)
        }

        fun create(
            color: Int,
            listener: OnHashTagClickListener,
            vararg additionalHashTagChars: Char
        ): HashTagHelper {
            return HashTagHelper(color, listener, additionalHashTagChars)
        }
    }

    interface OnHashTagClickListener {
        fun onHashTagClicked(hashTag: String?)
    }

    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            if (text.isNotEmpty()) {
                eraseAndColorizeAllText(text)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    fun handle(textView: TextView?) {
        if (mTextView == null) {
            mTextView = textView
            mTextView!!.addTextChangedListener(mTextWatcher)

            // in order to use spannable we have to set buffer type
            mTextView!!.setText(mTextView!!.text, TextView.BufferType.SPANNABLE)
            if (mOnHashTagClickListener != null) {
                // we need to set this in order to get onClick event
                mTextView!!.movementMethod = LinkMovementMethod.getInstance()

                // after onClick clicked text become highlighted
                mTextView!!.highlightColor = Color.TRANSPARENT
            } else {
                // hash tags are not clickable, no need to change these parameters
            }
            setColorsToAllHashTags(mTextView!!.text)
        } else {
            throw RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView")
        }
    }

    private fun eraseAndColorizeAllText(text: CharSequence) {
        val spannable = mTextView!!.text as Spannable
        val spans = spannable.getSpans(
            0, text.length,
            CharacterStyle::class.java
        )
        for (span in spans) {
            spannable.removeSpan(span)
        }
        setColorsToAllHashTags(text)
    }

    private fun setColorsToAllHashTags(text: CharSequence) {
        var startIndexOfNextHashSign: Int
        var index = 0
        while (index < text.length - 1) {
            val sign = text[index]
            var nextNotLetterDigitCharIndex =
                index + 1 // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if (sign == '#' || sign == '@') {
                startIndexOfNextHashSign = index
                nextNotLetterDigitCharIndex =
                    findNextValidHashTagChar(text, startIndexOfNextHashSign)
                setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex)
            }
            index = nextNotLetterDigitCharIndex
        }
    }

    private fun findNextValidHashTagChar(text: CharSequence, start: Int): Int {
        var nonLetterDigitCharIndex = -1 // skip first sign '#"
        for (index in start + 1 until text.length) {
            val sign = text[index]
            val isValidSign =
                Character.isLetterOrDigit(sign) || mAdditionalHashTagChars.contains(sign)
            if (!isValidSign) {
                nonLetterDigitCharIndex = index
                break
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length
        }
        return nonLetterDigitCharIndex
    }

    private fun setColorForHashTagToTheEnd(startIndex: Int, nextNotLetterDigitCharIndex: Int) {
        val s = mTextView!!.text as Spannable
        val span: CharacterStyle = if (mOnHashTagClickListener != null) {
            ClickableForegroundColorSpan(mHashTagWordColor, this)
        } else {
            // no need for clickable span because it is messing with selection when click
            ForegroundColorSpan(mHashTagWordColor)
        }
        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getAllHashTags(withHashes: Boolean): List<String> {
        val text = mTextView!!.text.toString()
        val spannable = mTextView!!.text as Spannable

        // use set to exclude duplicates
        val hashTags: MutableSet<String> = LinkedHashSet()
        for (span in spannable.getSpans(
            0, text.length,
            CharacterStyle::class.java
        )) {
            hashTags.add(
                text.substring(
                    if (!withHashes) spannable.getSpanStart(span) + 1 /*skip "#" sign*/ else spannable.getSpanStart(
                        span
                    ),
                    spannable.getSpanEnd(span)
                )
            )
        }
        return ArrayList(hashTags)
    }

    fun getAllHashTags(): List<String> {
        return getAllHashTags(false)
    }

    override fun onHashTagClicked(hashTag: String?) {
        mOnHashTagClickListener!!.onHashTagClicked(hashTag)
    }

    init {
        mOnHashTagClickListener = listener
        mAdditionalHashTagChars = ArrayList()
        if (additionalHashTagCharacters != null) {
            for (additionalChar in additionalHashTagCharacters) {
                mAdditionalHashTagChars.add(additionalChar)
            }
        }
    }
}