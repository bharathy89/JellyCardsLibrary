package com.codemonkey.cardsui.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codemonkey.cardsui.R;
import com.codemonkey.cardsui.SwipeDismissTouchListener;
import com.codemonkey.cardsui.SwipeDismissTouchListener.OnDismissCallback;
import com.codemonkey.cardsui.Utils;

public class CardStack extends View {
    private static final float _12F = 12f;
    private static final float _45F = 45f;
    private static final String NINE_OLD_TRANSLATION_Y = "translationY";
    private ArrayList<Card> cards;
    private String title, stackTitleColor;

    private int mPosition;
    private Context mContext;
    private CardStack mStack;
    private RelativeLayout container;
    private boolean swipable;
    private int viewHeight;
    private Map<Card, View> cardViewMap;

    public CardStack(Context context, ViewGroup parent) {
    	super(context);
        cards = new ArrayList<Card>();
        mStack = this;
        initView(context, true, parent);
    }

    public CardStack(Context context, String title, ViewGroup parent) {
    	super(context);
        cards = new ArrayList<Card>();
        mStack = this;

        setTitle(title);
        initView(context, true, parent);
    }
    public ArrayList<Card> getCards() {
        return cards;
    }

    public void add(Card newCard) {
        cards.add(0 , newCard);
        refresh();
    }

    public void initView(Context context, boolean swipable, ViewGroup parent) {
    	this.swipable = swipable;
        mContext = context;

        final View view = LayoutInflater.from(context).inflate(
                R.layout.item_stack, parent);

        assert view != null;
        container = (RelativeLayout) view
                .findViewById(R.id.stackContainer);
        final TextView title = (TextView) view.findViewById(R.id.stackTitle);

        if (!TextUtils.isEmpty(this.title)) {
            if (stackTitleColor == null)
                stackTitleColor = context.getResources().getString(R.color.card_title_text);

            title.setTextColor(Color.parseColor(stackTitleColor));
            title.setText(this.title);
            title.setVisibility(View.VISIBLE);
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        viewHeight = display.getHeight();
        cardViewMap = new HashMap<Card, View>();
    }
    
    
    private void refresh() {
    	 	
    	final int cardsArraySize = cards.size();
        final int lastCardPosition = cardsArraySize - 1;

        Card card;
        View cardView;
        container.removeAllViews();
        
    	for (int i = 0; i < cardsArraySize; i++) {
            card = cards.get(i);
            cardView = cardViewMap.get(card);
            
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            

            // handle the view
//            if (i == 0) {
//                cardView = card.getViewFirst(mContext);
//            }
//            else if (i == lastCardPosition) {
//                cardView = card.getViewLast(mContext);
//            }
//            else {
              if( cardView == null) {
            	   cardView = card.getView(mContext);
            	   cardViewMap.put(card, cardView);
              } 
//            }
            

            // handle the listener
            if (i == lastCardPosition) {
            	cardView.setVisibility(View.VISIBLE);
                cardView.setOnClickListener(card.getClickListener());
                cardView.setOnLongClickListener(card.getOnLongClickListener());
            } else {
            	cardView.setVisibility(View.GONE);
                cardView.setOnClickListener(getClickListener(this, container, i));
                cardView.setOnLongClickListener(getOnLongClickListener(this, container, i));
            }

            lp.setMargins(0, 0, 0, 0);

            cardView.setLayoutParams(lp);

            if (swipable) {
                cardView.setOnTouchListener(new SwipeDismissTouchListener(this, viewHeight,
                    cardView, card, new OnDismissCallback() {

                    @Override
                    public void onDismiss(View view, Object token) {
                        Card c = (Card) token;
                        // call onCardSwiped() listener
                        c.OnSwipeCard();
                        CardStack.this.remove(c);

                    }
                }));
            }

            container.addView(cardView);
        }
    }

    /**
     * Attempt to modify the convertView instead of inflating a new View for this CardStack.
     * If convertView isn't compatible, it isn't modified.
     * @param convertView view to try reusing
     * @return true on success, false if the convertView is not compatible
     */
    private boolean convert(View convertView) {
        // only convert singleton stacks
        if (cards.size() != 1) {
            Log.d("CardStack", "Can't convert view: num cards is " + cards.size());
            return false;
        }

        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.stackContainer);
        if (container == null) {
            Log.d("CardStack", "Can't convert view: can't find stackContainer");
            return false;
        }

        if (container.getChildCount() != 1) {
            Log.d("CardStack", "Can't convert view: child count is " + container.getChildCount());
            return false;
        }

        // check to see if they're compatible Card types
        Card card = cards.get(0);
        View convertCardView = container.getChildAt(0);

        if (convertCardView == null || convertCardView.getId() != card.getId()) {
            Log.d("CardStack", String.format("Can't convert view: child Id is 0x%x, card Id is 0x%x", convertCardView != null ? convertCardView.getId() : 0, card.getId()));
            return false;
        }

        if (card.convert(convertCardView))
            return true;

        return false;
    }

    public Card remove(int index) {
        Card card  = cards.remove(index);
        cardViewMap.remove(card);
        refresh();
        return card;
        
    }
    
    public void remove(Card c) {
        cards.remove(c);
        cardViewMap.remove(c);
        refresh();
    }

    public Card get(int i) {
        return cards.get(i);
    }
    
    public View getNext() {
    	if(cards.size() >= 2) {
    		return cardViewMap.get(cards.get(cards.size()-2));
    	}
    	return null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String setColor(String color) {
        return this.stackTitleColor = color;
    }

    private OnClickListener getClickListener(final CardStack cardStack,
                                             final RelativeLayout container, final int index) {
        return new OnClickListener() {

            @Override
            public void onClick(View v) {

                // init views array
                View[] views = new View[container.getChildCount()];

                for (int i = 0; i < views.length; i++) {
                    views[i] = container.getChildAt(i);

                }

                int last = views.length - 1;

                if (index != last) {

                    if (index == 0) {
                        onClickFirstCard(cardStack, container, index, views);
                    } /*else if (index < last) {
                        onClickOtherCard(cardStack, container, index, views,
                                last);
                    }*/

                }

            }

            public void onClickFirstCard(final CardStack cardStack,
                                         final RelativeLayout frameLayout, final int index,
                                         View[] views) {
                // run through all the cards
                for (int i = 0; i < views.length; i++) {
                    ObjectAnimator anim = null;

                    if (i == 0) {
                        // the first goes all the way down
                        float downFactor = 0;
                        if (views.length > 2) {
                            downFactor = convertDpToPixel((_45F)
                                    * (views.length - 1) - 1);
                        } else {
                            downFactor = convertDpToPixel(_45F);
                        }

                        anim = ObjectAnimator.ofFloat(views[i],
                                NINE_OLD_TRANSLATION_Y, 0, downFactor);
                        anim.addListener(getAnimationListener(cardStack,
                                frameLayout, index, views[index]));

                    } else if (i == 1) {
                        // the second goes up just a bit

                        float upFactor = convertDpToPixel(-17f);
                        anim = ObjectAnimator.ofFloat(views[i],
                                NINE_OLD_TRANSLATION_Y, 0, upFactor);

                    } else {
                        // the rest go up by one card
                        float upFactor = convertDpToPixel(-1 * _45F);
                        anim = ObjectAnimator.ofFloat(views[i],
                                NINE_OLD_TRANSLATION_Y, 0, upFactor);
                    }

                    if (anim != null)
                        anim.start();

                }
            }
            /*
            public void onClickOtherCard(final CardStack cardStack,
                                         final RelativeLayout frameLayout, final int index,
                                         View[] views, int last) {
                // if clicked card is in middle
                for (int i = index; i <= last; i++) {
                    // run through the cards from the clicked position
                    // and on until the end
                    ObjectAnimator anim = null;

                    if (i == index) {
                        // the selected card goes all the way down
                        float downFactor = convertDpToPixel(_45F * (last - i)
                                + _12F);
                        anim = ObjectAnimator.ofFloat(views[i],
                                NINE_OLD_TRANSLATION_Y, 0, downFactor);
                        anim.addListener(getAnimationListener(cardStack,
                                frameLayout, index, views[index]));
                    } else {
                        // the rest go up by one
                        float upFactor = convertDpToPixel(_45F * -1);
                        anim = ObjectAnimator.ofFloat(views[i],
                                NINE_OLD_TRANSLATION_Y, 0, upFactor);
                    }

                    if (anim != null)
                        anim.start();
                }
            }*/
        };
    }
    
    private OnLongClickListener getOnLongClickListener(final CardStack cardStack,
    		final RelativeLayout container, final int index) {
    	return new OnLongClickListener() {
    
    		@Override
			public boolean onLongClick(View v) {
    			// init views array
    			View[] views = new View[container.getChildCount()];
    			
    			for (int i = 0; i < views.length; i++) {
    				views[i] = container.getChildAt(i);
    				
    			}
    			
    			int last = views.length - 1;
    			
    			if (index != last) {
    				
    				if (index == 0) {
    					onClickFirstCard(cardStack, container, index, views);
    				} /*else if (index < last) {
    					onClickOtherCard(cardStack, container, index, views,
    							last);
    				}*/
    				
    			}
				return false;
			}
    		
    		public void onClickFirstCard(final CardStack cardStack,
    				final RelativeLayout frameLayout, final int index,
    				View[] views) {
    			// run through all the cards
    			for (int i = 0; i < views.length; i++) {
    				ObjectAnimator anim = null;
    				
    				if (i == 0) {
    					// the first goes all the way down
    					float downFactor = 0;
    					if (views.length > 2) {
    						downFactor = convertDpToPixel((_45F)
    								* (views.length - 1) - 1);
    					} else {
    						downFactor = convertDpToPixel(_45F);
    					}
    					
    					anim = ObjectAnimator.ofFloat(views[i],
    							NINE_OLD_TRANSLATION_Y, 0, downFactor);
    					anim.addListener(getAnimationListener(cardStack,
    							frameLayout, index, views[index]));
    					
    				} else if (i == 1) {
    					// the second goes up just a bit
    					
    					float upFactor = convertDpToPixel(-17f);
    					anim = ObjectAnimator.ofFloat(views[i],
    							NINE_OLD_TRANSLATION_Y, 0, upFactor);
    					
    				} else {
    					// the rest go up by one card
    					float upFactor = convertDpToPixel(-1 * _45F);
    					anim = ObjectAnimator.ofFloat(views[i],
    							NINE_OLD_TRANSLATION_Y, 0, upFactor);
    				}
    				
    				if (anim != null)
    					anim.start();
    				
    			}
    		}
    		/*
    		public void onClickOtherCard(final CardStack cardStack,
    				final RelativeLayout frameLayout, final int index,
    				View[] views, int last) {
    			// if clicked card is in middle
    			for (int i = index; i <= last; i++) {
    				// run through the cards from the clicked position
    				// and on until the end
    				ObjectAnimator anim = null;
    				
    				if (i == index) {
    					// the selected card goes all the way down
    					float downFactor = convertDpToPixel(_45F * (last - i)
    							+ _12F);
    					anim = ObjectAnimator.ofFloat(views[i],
    							NINE_OLD_TRANSLATION_Y, 0, downFactor);
    					anim.addListener(getAnimationListener(cardStack,
    							frameLayout, index, views[index]));
    				} else {
    					// the rest go up by one
    					float upFactor = convertDpToPixel(_45F * -1);
    					anim = ObjectAnimator.ofFloat(views[i],
    							NINE_OLD_TRANSLATION_Y, 0, upFactor);
    				}
    				
    				if (anim != null)
    					anim.start();
    			}
    		}*/
    	};
    }

    protected float convertDpToPixel(float dp) {

        return Utils.convertDpToPixel(mContext, dp);
    }

    private AnimatorListener getAnimationListener(final CardStack cardStack,
                                                  final RelativeLayout frameLayout, final int index,
                                                  final View clickedCard) {
        return new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (index == 0) {

                    View newFirstCard = frameLayout.getChildAt(1);
                    handleFirstCard(newFirstCard);

                    // clickedCard.setBackgroundResource(com.fima.cardsui.R.drawable.);

                    // newFirstCard.setBackgroundResource(com.fima.cardsui.R.drawable.card_background);
                    // FrameLayout.LayoutParams lp = new
                    // FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    // FrameLayout.LayoutParams.WRAP_CONTENT);
                    //
                    // if (index > 0) {
                    // top = convertDpToPixelInt(8) + (convertDpToPixelInt(36f)
                    // * index);
                    // bottom = 24;
                    //
                    // }
                    //
                    // else if (0 == index) {
                    // top = 2 * convertDpToPixelInt(8) +
                    // convertDpToPixelInt(1);
                    // bottom = convertDpToPixelInt(12);
                    // }
                    //
                    // lp.setMargins(0, top, 0, bottom);
                    //
                    // clickedCard.setLayoutParams(lp);
                    // clickedCard.setPadding(0, convertDpToPixelInt(20), 0, 0);

                } else {
                    clickedCard
                            .setBackgroundResource(com.codemonkey.cardsui.R.drawable.card_background);
                }
                frameLayout.removeView(clickedCard);
                frameLayout.addView(clickedCard);

            }

            private void handleFirstCard(View newFirstCard) {
                newFirstCard
                        .setBackgroundResource(com.codemonkey.cardsui.R.drawable.card_background);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                int top = 0;
                int bottom = 0;

                top = 2 * Utils.convertDpToPixelInt(mContext, 8)
                        + Utils.convertDpToPixelInt(mContext, 1);
                bottom = Utils.convertDpToPixelInt(mContext, 12);

                lp.setMargins(0, top, 0, bottom);
                newFirstCard.setLayoutParams(lp);
                newFirstCard.setPadding(0,
                        Utils.convertDpToPixelInt(mContext, 8), 0, 0);

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                Card card = cardStack.remove(index);
                cardStack.add(card);
                Log.v("CardsUI", "Notify Adapter");

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        };
    }

   

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

}