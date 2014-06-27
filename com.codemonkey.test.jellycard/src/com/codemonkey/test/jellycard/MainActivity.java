package com.codemonkey.test.jellycard;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.codemonkey.cardsui.views.CardUI;

public class MainActivity extends Activity {

	private CardUI mCardView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// init CardView
		mCardView = (CardUI) findViewById(R.id.cardsview);
		

	//	CardStack stackPlay = new CardStack();
	//	stackPlay.setTitle("GOOGLE PLAY CARDS");
	//	mCardView.addStack(stackPlay);

		
		mCardView.addCard(new MyCard(this,"Google Play"));

		mCardView
				.addCard(new MyCard(this,
						"Menu Overflow"));

		// add one card
		mCardView
				.addCard(new MyCard(this,
						"Different Colors for Title & Stripe"));

		mCardView
				.addCard(new MyCard(this,
						"Set Clickable or Not"));
		
		mCardView.addCard(new MyCard(this,"Google Play"));

		mCardView
				.addCard(new MyCard(this,
						"Menu Overflow"));

		// add one card
		mCardView
				.addCard(new MyCard(this, 
						"Different Colors for Title & Stripe"));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		return true;
	}
}
