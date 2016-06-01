package com.simplemobiletools.calculator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MyWidgetConfigure extends AppCompatActivity {
    @BindView(R.id.btn_reset) View resetBtn;
    @BindView(R.id.config_bg_color) View bgColorPicker;
    @BindView(R.id.config_bg_seekbar) SeekBar bgSeekBar;
    @BindView(R.id.config_text_color) View textColorPicker;
    @BindView(R.id.config_calc) View background;
    @BindView(R.id.config_save) Button saveBtn;
    @BindView(R.id.result) TextView result;
    @BindView(R.id.formula) TextView formula;
    private int widgetId;

    private int bgColor;
    private int bgColorWithoutTransparency;
    private float bgAlpha;
    private int textColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);
        ButterKnife.bind(this);
        initVariables();

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null)
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish();
    }

    private void initVariables() {
        final SharedPreferences prefs = getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        bgColor = prefs.getInt(Constants.WIDGET_BG_COLOR, 1);
        if (bgColor == 1) {
            bgColor = Color.BLACK;
            bgAlpha = .2f;
        } else {
            bgAlpha = Color.alpha(bgColor) / (float) 255;
        }

        resetBtn.setVisibility(View.VISIBLE);
        bgColorWithoutTransparency = Color.rgb(Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
        bgSeekBar.setOnSeekBarChangeListener(bgSeekbarChangeListener);
        bgSeekBar.setProgress((int) (bgAlpha * 100));
        updateBackgroundColor();

        textColor = prefs.getInt(Constants.WIDGET_TEXT_COLOR, getResources().getColor(R.color.colorPrimary));
        updateTextColor();

        formula.setText("15,937*5");
        result.setText("79,685");
    }

    @OnClick(R.id.config_save)
    public void saveConfig() {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.activity_main);
        views.setInt(R.id.calculator_holder, "setBackgroundColor", bgColor);
        appWidgetManager.updateAppWidget(widgetId, views);

        storeWidgetBackground();
        requestWidgetUpdate();

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private void storeWidgetBackground() {
        final SharedPreferences prefs = getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(Constants.WIDGET_BG_COLOR, bgColor).apply();
        prefs.edit().putInt(Constants.WIDGET_TEXT_COLOR, textColor).apply();
    }

    private void requestWidgetUpdate() {
        final Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        sendBroadcast(intent);
    }

    private void updateBackgroundColor() {
        bgColor = adjustAlpha(bgColorWithoutTransparency, bgAlpha);
        background.setBackgroundColor(bgColor);
        bgColorPicker.setBackgroundColor(bgColor);
        saveBtn.setBackgroundColor(bgColor);
    }

    private void updateTextColor() {
        textColorPicker.setBackgroundColor(textColor);
        saveBtn.setTextColor(textColor);

        int[] viewIds =
                new int[]{R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8,
                        R.id.btn_9, R.id.btn_modulo, R.id.btn_power, R.id.btn_root, R.id.btn_clear, R.id.btn_reset, R.id.btn_divide,
                        R.id.btn_multiply, R.id.btn_minus, R.id.btn_plus, R.id.btn_decimal, R.id.btn_equals};
        result.setTextColor(textColor);
        formula.setTextColor(textColor);

        Button btn;
        for (int i : viewIds) {
            btn = (Button) findViewById(i);
            btn.setTextColor(textColor);
        }
    }

    @OnClick(R.id.config_bg_color)
    public void pickBackgroundColor() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, bgColorWithoutTransparency, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                bgColorWithoutTransparency = color;
                updateBackgroundColor();
            }
        });

        dialog.show();
    }

    @OnClick(R.id.config_text_color)
    public void pickTextColor() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, textColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                textColor = color;
                updateTextColor();
            }
        });

        dialog.show();
    }

    private SeekBar.OnSeekBarChangeListener bgSeekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            bgAlpha = (float) progress / (float) 100;
            updateBackgroundColor();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private int adjustAlpha(int color, float factor) {
        final int alpha = Math.round(Color.alpha(color) * factor);
        final int red = Color.red(color);
        final int green = Color.green(color);
        final int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
