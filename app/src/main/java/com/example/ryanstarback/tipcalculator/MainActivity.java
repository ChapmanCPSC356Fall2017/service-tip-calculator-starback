package com.example.ryanstarback.tipcalculator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxRatingBar;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.subtotal_price_edit_text) EditText subtotalEditText;
    @BindView(R.id.score_text_view) TextView scoreTextView;
    @BindView(R.id.score_rating_bar) RatingBar scoreRatingBar;
    @BindView(R.id.total_price_text_view) TextView totalPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        totalPriceTextView.setText(String.format(getString(R.string.total_price), "$0.00"));

        Observable<Double> subtotalObserver = RxTextView.textChanges(subtotalEditText)
                .filter(s -> s.length() > 0)
                .map(s -> s.toString())
                .map(s -> Double.parseDouble(s));

        Observable<Integer> scoreObserver = RxRatingBar.ratingChanges(scoreRatingBar).map(r -> {
            if (r.floatValue() < 1) {
                return 1;
            }

            return (int) (r.floatValue()*2);
        }).doOnNext(score -> {
            scoreRatingBar.setRating((float) (score/2.0));
            scoreTextView.setText(String.format(getString(R.string.score), score));
        });

        Observable<Double> tipObserver = scoreObserver.map(score -> {
            if (score == 10) {
                return 0.25;
            } else if (score == 8 || score == 9) {
                return 0.20;
            } else if (score == 6 || score == 7) {
                return 0.15;
            } else if (score == 4 || score == 5) {
                return 0.13;
            } else if (score == 1 || score == 2 || score == 3) {
                return 0.10;
            } else {
                return 0.00;
            }
        });

        Observable.combineLatest(subtotalObserver, tipObserver, (subtotal, tip) -> {
           return subtotal + subtotal*tip;
        }).subscribe(total -> {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
            format.setCurrency(Currency.getInstance(Locale.getDefault()));
            totalPriceTextView.setText(String.format(getString(R.string.total_price), format.format(total)));
        });
    }
}
