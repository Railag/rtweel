package com.rtweel.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.fragments.BaseFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by firrael on 7.5.15.
 */
public class DetailImageFragment extends BaseFragment {
    private Animator mCurrentAnimator;

    private ImageView image;
    private Rect startRect;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail_image, null, false);
        image = (ImageView) v.findViewById(R.id.detail_image);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            ArrayList<Integer> points = args.getIntegerArrayList(Const.IMAGE_RECT);
            startRect = new Rect(points.get(0), points.get(1), points.get(2), points.get(3));
            String url = args.getString(Const.MEDIA_URL);
            zoomImage(url);
        }
    }

    private void zoomImage(String url) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        getActivity().findViewById(R.id.main_frame)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startRect.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startRect.width() / startRect.height()) {
            // Extend start bounds horizontally
            startScale = (float) startRect.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startRect.width()) / 2;
            startRect.left -= deltaWidth;
            startRect.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startRect.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startRect.height()) / 2;
            startRect.top -= deltaHeight;
            startRect.bottom += deltaHeight;
        }

        image.setPivotX(0f);
        image.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(image, "x",
                        startRect.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(image, "y",
                        startRect.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(image, "scaleX",
                        startScale, 1f)).with(ObjectAnimator.ofFloat(image,
                "scaleY", startScale, 1f));
        set.setDuration(1000);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        Picasso.with(getActivity()).load(url).into(image);

    }
}
