package com.codemonkey.cardsui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.codemonkey.cardsui.R;
import com.codemonkey.cardsui.objects.Card;
import com.codemonkey.cardsui.objects.CardStack;



public class CardUI extends FrameLayout {

    private CardStack mStack;
   
    private OnRenderedListener onRenderedListener;

    /**
     * Constructor
     */
    public CardUI(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Constructor
     */
    public CardUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    
    private void init(Context context) {
    	LayoutInflater inflater = LayoutInflater.from(context);
    	FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.cards_view, this);
    	mStack = new CardStack(context, layout);
    	layout.addView(mStack);
    	layout.requestLayout();
    }

    /**
     * Constructor
     */
    public CardUI(Context context) {
        super(context);
        init(context);
    }

    public void addCard(Card card) {
        mStack.add(card);
    }

    public OnRenderedListener getOnRenderedListener() {
        return onRenderedListener;
    }

    public void setOnRenderedListener(OnRenderedListener onRenderedListener) {
        this.onRenderedListener = onRenderedListener;
    }

    public interface OnRenderedListener {
        public void onRendered();
    }

}
