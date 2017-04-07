package com.rollncode.basement.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.rollncode.basement.utility.CropCircleTransformation;

public abstract class BaseAvatarImageView extends AppCompatImageView {

    private static final String NOT_CHARACTER_AVATAR = "NOT_CHARACTER_AVATAR";

    private final CropCircleTransformation mTransformation;
    private final RequestManager mGlide;

    private final Paint mPaintBackground;
    private final Paint mPaintText;
    private float mPaintTextCent;

    @DrawableRes
    private int mPlaceholder;
    private String mTextPlaceholder;

    private boolean mCircleMode;

    public BaseAvatarImageView(@NonNull Context context) {
        this(context, null);
    }

    public BaseAvatarImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseAvatarImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode()) {
            mTransformation = null;
            mGlide = null;

        } else {
            mTransformation = new CropCircleTransformation(context);
            mGlide = Glide.with(context.getApplicationContext());
        }
        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaintBackground.setColor(getBackgroundColor());
        mPaintText.setTextSize(getSymbolAvatarSize());
        mPaintText.setTextAlign(Align.CENTER);
        mPaintText.setColor(Color.WHITE);
        mPaintTextCent = (mPaintText.descent() + mPaintText.ascent()) / 2F;

        mPlaceholder = getDefaultPlaceholder();
        setCircleMode(true);
    }

    @NonNull
    protected final RequestManager getGlide() {
        return mGlide;
    }

    protected final boolean isCircleMode() {
        return mCircleMode;
    }

    @CallSuper
    public void setCircleMode(boolean circleMode) {
        mCircleMode = circleMode;
        super.invalidate();
    }

    @NonNull
    protected final Paint getPaintText() {
        return mPaintText;
    }

    @ColorInt
    protected int getBackgroundColor() {
        return Color.TRANSPARENT;
    }

    protected float getSymbolAvatarSize() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
    }

    @DrawableRes
    protected abstract int getDefaultPlaceholder();

    public void setPlaceholder(@DrawableRes int placeholder) {
        mPlaceholder = placeholder;
    }

    public final void load(@Nullable String string) {
        mTextPlaceholder = NOT_CHARACTER_AVATAR;

        final DrawableTypeRequest<String> request = mGlide.load(string);
        if (mCircleMode) {
            request.bitmapTransform(mTransformation);
        }
        request.placeholder(mPlaceholder).into(this);
        super.invalidate();
    }

    public final void load(@Nullable String uri, @Nullable String tag) {
        mTextPlaceholder = getSymbol(tag);

        final DrawableTypeRequest<String> request = mGlide.load(uri);
        if (mCircleMode) {
            request.bitmapTransform(mTransformation);
        }
        request.placeholder(mPlaceholder).into(this);
        super.invalidate();
    }

    public final void load(@Nullable Uri uri, @Nullable String tag) {
        mTextPlaceholder = getSymbol(tag);

        final DrawableTypeRequest<Uri> request = mGlide.load(uri);
        if (mCircleMode) {
            request.bitmapTransform(mTransformation);
        }
        request.placeholder(mPlaceholder).into(this);
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //noinspection StringEquality
        if (NOT_CHARACTER_AVATAR == mTextPlaceholder || getDrawable() != null) {
            return;
        }
        if (mTextPlaceholder == null) {
            super.setImageResource(mPlaceholder);

        } else {
            final float cX = super.getWidth() / 2F;
            final float cY = super.getHeight() / 2F;
            final float radius = Math.min(cX - (super.getPaddingLeft() + super.getPaddingRight()) / 2F, cY - (super.getPaddingTop() + super.getPaddingBottom()) / 2F);

            if (mCircleMode) {
                canvas.drawCircle(cX, cY, radius, mPaintBackground);
            }
            final float textSize = Math.min(cX, cY);
            if (textSize != mPaintText.getTextSize()) {
                mPaintText.setTextSize(textSize);
                mPaintTextCent = (mPaintText.descent() + mPaintText.ascent()) / 2F;
            }
            canvas.drawText(mTextPlaceholder, cX, cY - mPaintTextCent, mPaintText);
        }
    }

    @Nullable
    protected abstract String getSymbol(@Nullable String tag);
}