package com.codemonkey.test.jellycard;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codemonkey.cardsui.objects.RecyclableCard;

public class MyImageCard extends RecyclableCard {

	public MyImageCard(Context context, String title, int image){
		super(context, title, image);
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_picture;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(title);
		((ImageView) convertView.findViewById(R.id.imageView1)).setImageResource(image);
	}

}
