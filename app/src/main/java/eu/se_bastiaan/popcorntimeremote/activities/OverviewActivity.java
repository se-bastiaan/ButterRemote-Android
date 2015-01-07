package eu.se_bastiaan.popcorntimeremote.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.crashlytics.android.Crashlytics;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.fragments.InstanceListFragment;
import eu.se_bastiaan.popcorntimeremote.utils.PixelUtils;
import io.fabric.sdk.android.Fabric;

public class OverviewActivity extends ActionBarActivity {

    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        /*if (Constants.LOG_ENABLED)
            Ion.getDefault(this).configure().setLogging("IonLogs", Log.DEBUG);*/

        setContentView(R.layout.activity_framelayout);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + PixelUtils.getStatusBarHeight(this)));
        } else {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material)));
        }

        progressBar.setVisibility(View.GONE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frameLayout, new InstanceListFragment());
        fragmentTransaction.commit();
    }

}
