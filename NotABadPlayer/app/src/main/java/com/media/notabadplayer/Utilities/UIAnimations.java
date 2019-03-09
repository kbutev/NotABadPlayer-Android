package com.media.notabadplayer.Utilities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.Button;

import com.media.notabadplayer.R;

import javax.annotation.Nullable;

public class UIAnimations {
    
    public static void animateButtonTAP(@Nullable Context context, @Nullable final Button view)
    {
        if (context == null || view == null)
        {
            return;
        }
        
        int colorFrom = context.getResources().getColor(R.color.animationButtonTapStart);
        int colorTo = context.getResources().getColor(R.color.animationButtonTapEnd);
        
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
    
    public static void animateAlbumItemTAP(@Nullable Context context, @Nullable final View view)
    {
        if (context == null || view == null)
        {
            return;
        }
        
        int colorFrom = context.getResources().getColor(R.color.animationAlbumItemTapStart);
        int colorTo = context.getResources().getColor(R.color.animationAlbumItemTapEnd);
        
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
