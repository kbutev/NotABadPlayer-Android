package com.media.notabadplayer.Utilities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
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
        
        Drawable background = view.getBackground();
        
        if (!(background instanceof ColorDrawable)) 
        {
            return;
        }
        
        int colorFrom = context.getResources().getColor(R.color.animationSelectionEffect);
        int colorTo = ((ColorDrawable) background).getColor();
        
        ValueAnimator a = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        
        a.setDuration(400);
        
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
}
