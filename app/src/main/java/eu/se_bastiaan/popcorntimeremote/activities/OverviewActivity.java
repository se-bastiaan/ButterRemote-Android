package eu.se_bastiaan.popcorntimeremote.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.crashlytics.android.Crashlytics;

import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.database.InstanceDbHelper;
import eu.se_bastiaan.popcorntimeremote.fragments.InstanceListFragment;
import eu.se_bastiaan.popcorntimeremote.utils.AutoUpdateApk;

public class OverviewActivity extends ActionBarActivity {

    private SimpleCursorAdapter mAdapter;

    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        AutoUpdateApk autoUpdateApk = new AutoUpdateApk(getApplicationContext(), Constants.UNANAP_KEY);

        setContentView(R.layout.activity_framelayout);
        ButterKnife.inject(this);

        progressBar.setVisibility(View.GONE);

        getSupportActionBar().setLogo(R.drawable.ic_logo);

        InstanceDbHelper dbHelper = new InstanceDbHelper(this);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frameLayout, new InstanceListFragment());
        fragmentTransaction.commit();
    }

}
