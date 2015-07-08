package eu.se_bastiaan.popcorntimeremote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;

import eu.se_bastiaan.popcorntimeremote.intro.SlideFive;
import eu.se_bastiaan.popcorntimeremote.intro.SlideFour;
import eu.se_bastiaan.popcorntimeremote.intro.SlideOne;
import eu.se_bastiaan.popcorntimeremote.intro.SlideSix;
import eu.se_bastiaan.popcorntimeremote.intro.SlideThree;
import eu.se_bastiaan.popcorntimeremote.intro.SlideTwo;
import eu.se_bastiaan.popcorntimeremote.utils.PrefUtils;

public class IntroActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addSlide(new SlideOne(), getApplicationContext());
        addSlide(new SlideTwo(), getApplicationContext());
        addSlide(new SlideThree(), getApplicationContext());
        addSlide(new SlideFour(), getApplicationContext());
        addSlide(new SlideFive(), getApplicationContext());
        addSlide(new SlideSix(), getApplicationContext());
    }

    @Override
    public void onDonePressed() {
        PrefUtils.save(this, "intro", true);
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }

}