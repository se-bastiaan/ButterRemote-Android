package eu.se_bastiaan.popcorntimeremote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;

import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.models.ScanModel;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PairingScannerActivity extends ActionBarActivity implements ZXingScannerView.ResultHandler {

    public static Integer SCAN = 1440, SUCCESS = 1441;

    ZXingScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_logo);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String json = rawResult.getText();
        Gson gson = new Gson();
        ScanModel model = gson.fromJson(json, ScanModel.class);

        Intent intent = new Intent();
        intent.putExtra("result", model);
        setResult(SUCCESS, intent);
        finish();
    }
}