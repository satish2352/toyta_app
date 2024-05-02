package com.autocop.legroomlamps;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class CustomFontTextView extends androidx.appcompat.widget.AppCompatTextView {
    private final Typeface FONT_NAME;

    public CustomFontTextView(Context context) {
        super(context);
        this.FONT_NAME = Typeface.createFromAsset(context.getAssets(), "fonts/serif.otf");
        if (this.FONT_NAME != null) {
            setTypeface(this.FONT_NAME);
        }
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.FONT_NAME = Typeface.createFromAsset(context.getAssets(), "fonts/serif.otf");
        if (this.FONT_NAME != null) {
            setTypeface(this.FONT_NAME);
        }
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.FONT_NAME = Typeface.createFromAsset(context.getAssets(), "fonts/serif.otf");
        if (this.FONT_NAME != null) {
            setTypeface(this.FONT_NAME);
        }
    }
}
