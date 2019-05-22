package com.media.notabadplayer.Utilities;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.media.notabadplayer.R;

public class UIAnimations {

    public static void animateImageTAP(Context context, final ImageView view)
    {
        if (context == null || view == null)
        {
            return;
        }
        
        int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);
        
        if (ImageViewCompat.getImageTintList(view) == null)
        {
            return;
        }
        
        int colorTo = ImageViewCompat.getImageTintList(view).getDefaultColor();

        ValueAnimator a = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        a.setDuration(500);

        view.setColorFilter(colorFrom, PorterDuff.Mode.SRC_ATOP);

        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setColorFilter((int)animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
            }
        });

        a.start();
    }
    
    public static void animateButtonTAP(Context context, final Button view)
    {
        if (context == null || view == null)
        {
            return;
        }
        
        int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);
        int colorTo = ViewCompat.getBackgroundTintList(view).getDefaultColor();
        
        ValueAnimator a = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        
        a.setDuration(500);

        view.getBackground().setColorFilter(colorFrom, PorterDuff.Mode.SRC_ATOP);
        
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.getBackground().setColorFilter((int)animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
            }
        });
        
        a.start();
    }
    
    public static void animateAlbumItemTAP(Context context, final View view)
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
        
        int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);
        int colorTo = ((ColorDrawable) background).getColor();
        
        ValueAnimator a = new ValueAnimator();
        a.setIntValues(colorFrom, colorTo);
        a.setEvaluator(new ArgbEvaluator());
        
        a.setDuration(300);
        
        view.setBackgroundColor(colorFrom);

        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((int)animator.getAnimatedValue());
            }
        });

        a.start();
    }

    public static void animateViewFadeIn(Context context, final View view)
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

    public static void animateViewFadeOut(Context context, final View view)
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
}
