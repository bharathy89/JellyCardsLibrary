/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.codemonkey.cardsui;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.animation.AnimatorSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;

import com.codemonkey.cardsui.objects.CardStack;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * A {@link android.view.View.OnTouchListener} that makes any {@link View}
 * dismissable when the user swipes (drags her finger) horizontally across the
 * view.
 * 
 * <p>
 * <em>For {@link android.widget.ListView} list items that don't manage their own touch events
 * (i.e. you're using
 * {@link android.widget.ListView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)}
 * or an equivalent listener on {@link android.app.ListActivity} or
 * {@link android.app.ListFragment}, use {@link SwipeDismissListViewTouchListener} instead.</em>
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * view.setOnTouchListener(new SwipeDismissTouchListener(view, null, // Optional
 * 																	// token/cookie
 * 																	// object
 * 		new SwipeDismissTouchListener.OnDismissCallback() {
 * 			public void onDismiss(View view, Object token) {
 * 				parent.removeView(view);
 * 			}
 * 		}));
 * </pre>
 * 
 * <p>
 * This class Requires API level 12 or later due to use of
 * {@link android.view.ViewPropertyAnimator}.
 * </p>
 * 
 * @see SwipeDismissListViewTouchListener
 */
public class SwipeDismissTouchListener implements View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;

	// Fixed properties
	private View mView;
	private OnDismissCallback mCallback;
	private int mViewHeight = 1; // 1 and not 0 to prevent dividing by zero

	// Transient properties
	private float mDownY;
	private float mDownX;
	private float prevX;
	private boolean mSwiping;
	private Object mToken;
	private VelocityTracker mVelocityTracker;
	private float mTranslationY;
	private CardStack cardStack;
	
	private int viewHeight;

	private View backView;
	
	/**
	 * The callback interface used by {@link SwipeDismissTouchListener} to
	 * inform its client about a successful dismissal of the view for which it
	 * was created.
	 */
	public interface OnDismissCallback {
		/**
		 * Called when the user has indicated they she would like to dismiss the
		 * view.
		 * 
		 * @param view
		 *            The originating {@link View} to be dismissed.
		 * @param token
		 *            The optional token passed to this object's constructor.
		 */
		public void onDismiss(View view, Object token);
	}

	/**
	 * Constructs a new swipe-to-dismiss touch listener for the given view.
	 * 
	 * @param view
	 *            The view to make dismissable.
	 * @param token
	 *            An optional token/cookie object to be passed through to the
	 *            callback.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss this view.
	 */
	public SwipeDismissTouchListener(CardStack cardStack, int height, View view, Object token,
			OnDismissCallback callback) {
		this.cardStack = cardStack;
		ViewConfiguration vc = ViewConfiguration.get(view.getContext());
		mSlop = vc.getScaledTouchSlop() ;
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = view.getContext().getResources()
				.getInteger(android.R.integer.config_shortAnimTime);
		mView = view;
		mToken = token;
		mCallback = callback;
		viewHeight = height;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		// offset because the view is translated during swipe
		motionEvent.offsetLocation(mTranslationY, 0);
		Log.d("swipe - down", mTranslationY + " mTranslationY ");
		
		if (mViewHeight < 2) {
			mViewHeight = mView.getHeight();
		}

		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: {
			// TODO: ensure this is a finger, and set a flag
			mDownY = motionEvent.getRawY();
			mDownX = motionEvent.getRawX();
			prevX = mDownX;
			mVelocityTracker = VelocityTracker.obtain();
			mVelocityTracker.addMovement(motionEvent);
			//view.onTouchEvent(motionEvent);
			Log.d("swipe - down", mDownY + "mDownY ");
			backView = cardStack.getNext();
			if(backView != null) {
			backView.setVisibility(View.VISIBLE);
			backView.setScaleX(0.1f);
			backView.setScaleY(0.1f);
			backView.setAlpha(0);
			}
			
			return false;
		}

		case MotionEvent.ACTION_UP: {
			if (mVelocityTracker == null) {
				break;
			}

			float deltaY = motionEvent.getRawY() - mDownY;
			
			Log.d("swipe - up", view.getY() + " view.getY() "+viewHeight+" viewHeight ");
			mVelocityTracker.addMovement(motionEvent);
			mVelocityTracker.computeCurrentVelocity(1000);
			float velocityX = Math.abs(mVelocityTracker.getXVelocity());
			float velocityY = Math.abs(mVelocityTracker.getYVelocity());
			boolean dismiss = false;
			boolean dismissRight = false;
			if (view.getY() > viewHeight - 250 ) {
				dismiss = true;
				dismissRight = deltaY > 0;
			}/* else if (mMinFlingVelocity <= velocityY
					&& velocityY <= mMaxFlingVelocity && velocityX < velocityY) {
				dismiss = true;
				dismissRight = mVelocityTracker.getYVelocity() > 0;
			}*/
			if (dismiss) {
				// dismiss
				animate(mView)
						.translationY(dismissRight ? viewHeight : -viewHeight)
						.alpha(0).setDuration(mAnimationTime)
						.setListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animator arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator arg0) {
								performDismiss();

							}

							@Override
							public void onAnimationCancel(Animator arg0) {
								// TODO Auto-generated method stub

							}
						});
				if(backView != null) {
					animate(backView).scaleX(1f).scaleY(1f).translationY(0).alpha(1)
						.setDuration(mAnimationTime).setListener(null);
				}
				
			} else {
				// cancel
				animate(mView).translationY(0).alpha(1).rotation(0)
						.setDuration(mAnimationTime).setListener(null);
				if(backView != null) {
				backView.setVisibility(View.GONE);
				}
				
				Log.d("swipe - up", mDownY + " mDownY "+deltaY+" deltaY ");

			}
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mTranslationY = 0;
			mDownY = 0;
			mSwiping = false;
			backView = null;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if (mVelocityTracker == null) {
				break;
			}

			mVelocityTracker.addMovement(motionEvent);
			float deltaY = motionEvent.getRawY() - mDownY;
			float deltaX = motionEvent.getRawX() - mDownX;
			Log.d("swipe - move", "swiping "+mSlop);
			
			Log.d("swipe - move", mDownY + " mDownY "+deltaY+" deltaY ");
			if (Math.abs(deltaY) > 10) {
				mSwiping = true;
				Log.d("swipe - move", "swiping started "+mSlop);
				mView.getParent().requestDisallowInterceptTouchEvent(true);

				// Cancel listview's touch
				MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
				cancelEvent
						.setAction(MotionEvent.ACTION_CANCEL
								| (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				mView.onTouchEvent(cancelEvent);
				cancelEvent.recycle();
			}
			

			if (mSwiping) {
				mTranslationY = deltaY;
				ViewHelper.setTranslationY(mView, deltaY);
				
				ViewHelper.setRotation(mView, mView.getRotation()+30.0f*((motionEvent.getRawX()-prevX)
					/ viewHeight));
				
				Log.d("swipe - move", deltaY+" deltaY ");
				Log.d("swipe - move-angle", (deltaX
						/ viewHeight)+"");
				prevX =  motionEvent.getRawX();
				

//				// TODO: use an ease-out interpolator or such
//				setAlpha(
//						mView,
//						Math.max(
//								0f,
//								Math.min(1f, 1f - (Math.abs(deltaY)
//										/ viewHeight))));
				return true;
			}
			break;
		}
		}
		return false;
	}

	private void performDismiss() {
		// Animate the dismissed view to zero-height and then fire the dismiss
		// callback.
		// This triggers layout on each animation frame; in the future we may
		// want to do something
		// smarter and more performant.

		final ViewGroup.LayoutParams lp = mView.getLayoutParams();
		final int originalHeight = mView.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1)
				.setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCallback.onDismiss(mView, mToken);
				// Reset view presentation
				setAlpha(mView, 1f);
				ViewHelper.setTranslationY(mView, 0);
				// mView.setAlpha(1f);
				// mView.setTranslationX(0);
				lp.height = originalHeight;
				mView.setLayoutParams(lp);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				mView.setLayoutParams(lp);
			}
		});

		animator.start();
	}
}
