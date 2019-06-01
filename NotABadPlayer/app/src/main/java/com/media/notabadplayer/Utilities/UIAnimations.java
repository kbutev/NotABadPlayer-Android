package com.media.notabadplayer.Utilities;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.media.notabadplayer.R;

public class UIAnimations {
    private static UIAnimations singleton;

    public final ButtonAnimations buttonAnimations;
    public final ImageAnimations imageAnimations;
    public final ListItemAnimations listItemAnimations;

    private UIAnimations()
    {
        buttonAnimations = new ButtonAnimations();
        imageAnimations = new ImageAnimations();
        listItemAnimations = new ListItemAnimations();
    }

    public static synchronized UIAnimations getShared()
    {
        if (singleton == null)
        {
            singleton = new UIAnimations();
        }

        return singleton;
    }

    public void stopAnimations(final @Nullable View view)
    {
        view.clearAnimation();
        
        if (view.animate() != null)
        {
            view.animate().cancel();
        }
    }

    public void animateFadeIn(@Nullable Context context, final @Nullable View view)
    {
        if (context == null || view == null)
        {
            return;
        }

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(300);

        final AnimatorSet animationSet = new AnimatorSet();

        animationSet.play(fadeIn);

        animationSet.start();
    }

    public void animateFadeOut(@Nullable Context context, final @Nullable View view)
    {
        if (context == null || view == null)
        {
            return;
        }

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha",  1.0f, 0.0f);
        fadeOut.setDuration(500);

        final AnimatorSet animationSet = new AnimatorSet();

        animationSet.play(fadeOut);

        animationSet.start();
    }

    public void animateQuickScaleDown(@Nullable Context context, final @Nullable View view, float startScaleValue)
    {
        if (context == null || view == null)
        {
            return;
        }
        
        Animation animation = new ScaleAnimation(startScaleValue, 1.0f, startScaleValue, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(150);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public void animateQuickScaleUp(@Nullable Context context, final @Nullable View view, float startScaleValue)
    {
        if (context == null || view == null)
        {
            return;
        }

        Animation animation = new ScaleAnimation(startScaleValue, 1.0f, startScaleValue, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(150);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public class ImageAnimations {
        ValueAnimator animator = null;

        public void animateTap(@Nullable Context context, final @Nullable ImageView view)
        {
            if (context == null || view == null)
            {
                return;
            }

            endAll();

            int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);

            ColorStateList tintList = ImageViewCompat.getImageTintList(view);

            if (tintList == null)
            {
                return;
            }

            int colorTo = tintList.getDefaultColor();

            animator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

            animator.setDuration(500);

            view.setColorFilter(colorFrom, PorterDuff.Mode.SRC_ATOP);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    view.setColorFilter((int)animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                }
            });

            animator.start();
        }

        public void interruptAll()
        {
            if (animator != null)
            {
                animator.cancel();
                animator = null;
            }
        }

        public void endAll()
        {
            if (animator != null)
            {
                animator.end();
                animator = null;
            }
        }
    }

    public class ButtonAnimations {
        ValueAnimator animator = null;

        public void animateTap(@Nullable Context context, final @Nullable Button view)
        {
            if (context == null || view == null)
            {
                return;
            }

            endAll();

            int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);
            int colorTo = ViewCompat.getBackgroundTintList(view).getDefaultColor();

            animator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

            animator.setDuration(500);

            view.getBackground().setColorFilter(colorFrom, PorterDuff.Mode.SRC_ATOP);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    view.getBackground().setColorFilter((int)animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                }
            });

            animator.start();
        }

        public void interruptAll()
        {
            if (animator != null)
            {
                animator.cancel();
                animator = null;
            }
        }

        public void endAll()
        {
            if (animator != null)
            {
                animator.end();
                animator = null;
            }
        }
    }

    public class ListItemAnimations {
        ValueAnimator animator = null;

        public void animateTap(@Nullable Context context, final @Nullable View view)
        {
            if (context == null || view == null)
            {
                return;
            }

            final Drawable background = view.getBackground();

            if (!(background instanceof ColorDrawable))
            {
                return;
            }

            endAll();

            int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);
            int colorTo = context.getResources().getColor(R.color.currentlyPlayingTrack);

            animator = new ValueAnimator();
            animator.setIntValues(colorFrom, colorTo);
            animator.setEvaluator(new ArgbEvaluator());

            animator.setDuration(300);

            view.setBackgroundColor(colorFrom);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    view.setBackgroundColor((int)animator.getAnimatedValue());
                }
            });

            animator.start();
        }

        public void interruptAll()
        {
            if (animator != null)
            {
                animator.cancel();
                animator = null;
            }
        }

        public void endAll()
        {
            if (animator != null)
            {
                animator.end();
                animator = null;
            }
        }
    }
}
