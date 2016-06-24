/**
 * Copyright (C) 2013 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apptik.comm.jus.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import io.apptik.comm.jus.R;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.ui.ImageLoader.ImageContainer;
import io.apptik.comm.jus.ui.ImageLoader.ImageListener;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class NetworkImageView extends ImageView {
    /**
     * The URL of the network image to load
     */
    private String url;

    /**
     * The tag of the network image request so you can perform operaions on the actual request
     */
    private Object tag;

    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int defaultImageId;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int errorImageId;

    /**
     * Local copy of the ImageLoader.
     */
    private ImageLoader imageLoader;

    /**
     * Current ImageContainer. (either in-flight or finished)
     */
    private ImageContainer imageContainer;

    public NetworkImageView(Context context) {
        this(context, null);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.jus, defStyle,
                styleRes);
        tag = a.getString(R.styleable.jus_requestTag);
        a.recycle();
    }

    public Object getRequestTag() {
        return tag;
    }

    public NetworkImageView setRequestTag(Object tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
     * <p/>
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)} and
     * {@link NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url         The URL that should be loaded into this ImageView.
     * @param imageLoader ImageLoader that will be used to make the request.
     */
    public void setImageUrl(String url, ImageLoader imageLoader) {
        this.url = url;
        this.imageLoader = imageLoader;
        if (this.imageLoader.getDefaultImageId() != 0) {
            defaultImageId = this.imageLoader.getDefaultImageId();
        }
        if (this.imageLoader.getErrorImageId() != 0) {
            errorImageId = this.imageLoader.getErrorImageId();
        }
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    public void resetImage() {
        url = null;
        if (imageContainer != null) {
            imageContainer.cancelRequest();
            imageContainer = null;
        }
        setDefaultImageOrNull();
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage) {
        defaultImageId = defaultImage;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        errorImageId = errorImage;
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     *
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    synchronized void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = getWidth();
        int height = getHeight();

        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(url)) {
            if (imageContainer != null) {
                imageContainer.cancelRequest();
                imageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (imageContainer != null && imageContainer.getRequestUrl() != null) {
            if (imageContainer.getRequestUrl().equals(url)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                imageContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        // Calculate the max image width / height to use while ignoring WRAP_CONTENT dimens.
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageContainer newContainer = imageLoader.get(url,
                new ImageListener() {
                    @Override
                    public void onError(JusError error) {
                        if (errorImageId != 0) {
                            setImageResource(errorImageId);
                        }
                    }

                    @Override
                    public void onResponse(final ImageContainer response, boolean isImmediate) {
                        //verify if we expect the same url
                        if (NetworkImageView.this.url == null ||
                                !NetworkImageView.this.url.equals(response.getRequestUrl())) {
                            Log.w("NetworkImageView", "received: " + response.getRequestUrl()
                                    + ", expected: " + NetworkImageView.this.url);
                            return;
                        }

                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main threadId.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmap() != null && isOk2Draw(response.getBitmap())) {
                            setImageBitmap(response.getBitmap());
                        } else if (defaultImageId != 0) {
                            Log.w("NetworkImageView", "received null for: " + response
                                    .getRequestUrl());
                            setImageResource(defaultImageId);
                        }
                    }
                }, maxWidth, maxHeight, tag);

        // update the ImageContainer to be the new bitmap container.
        imageContainer = newContainer;
    }

    /**
     * Checks if the bitmap is able to be drawn by a {@link Canvas}. If bit map is null it will
     * return true as this is fine and will be ignored anyway, while recycled bitmaps will cause an
     * Exception thrown
     */
    protected static boolean isOk2Draw(Bitmap bitmap) {
        //ignore this if bitmap is null.
        if (bitmap == null) return true;

        if (bitmap.isRecycled()) {
            return false;
        }
        if (Build.VERSION.SDK_INT > 16) {
            if (!bitmap.isPremultiplied() && bitmap.getConfig() == Bitmap.Config.ARGB_8888 &&
                    bitmap.hasAlpha()) {
                return false;
            }
        }

        return true;
    }

    protected void setDefaultImageOrNull() {
        if (defaultImageId != 0) {
            setImageResource(defaultImageId);
        } else {
            setImageBitmap(null);
        }
        ((BitmapDrawable) this.getDrawable()).getBitmap();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() != null
                && BitmapDrawable.class.isAssignableFrom(getDrawable().getClass())) {
            if (!isOk2Draw(((BitmapDrawable) getDrawable()).getBitmap())) {
                //we have a problem and we retry
                if (imageContainer != null) {
                    imageContainer.cancelRequest();
                    imageContainer = null;
                }
                setDefaultImageOrNull();
                loadImageIfNecessary(true);
                return;
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (imageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            imageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            imageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
