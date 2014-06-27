package com.codemonkey.test.jellycard;



import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.codemonkey.cardsui.objects.RecyclableCard;

public class MyCard extends RecyclableCard {

	public MyCard(Context context, String title){
		super(context, title);
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_ex;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(title);
	}
}
