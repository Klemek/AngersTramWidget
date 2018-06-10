package fr.klemek.angerstramwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;

import fr.klemek.angerstramwidget.utils.Constants;
import fr.klemek.angerstramwidget.utils.Data;

/**
 * The configuration screen for the {@link TramWidget TramWidget} AppWidget.
 */
public abstract class TramWidgetConfigureActivity extends Activity {

    private boolean small;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean showNext = false;

    private Spinner mAppStopSpinner;
    private Spinner mAppDestSpinner;
    private ArrayAdapter<String> adapter;
    private Button mReloadButton;
    private ScrollView mScrollView;

    public TramWidgetConfigureActivity(boolean small) {
        this.small = small;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.tram_widget_configure);
        mAppStopSpinner = findViewById(R.id.stop_spinner);
        mAppDestSpinner = findViewById(R.id.dest_spinner);
        ListView mListView = findViewById(R.id.listview);
        mScrollView = findViewById(R.id.scrollview);
        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Context context = TramWidgetConfigureActivity.this;

                // When the button is clicked, store the string locally

                String selected;
                if (mAppStopSpinner.getSelectedItemPosition() >= 0)
                    selected = Data.getStops(TramWidgetConfigureActivity.this).get(mAppStopSpinner.getSelectedItemPosition());
                else
                    selected = Data.getStops(TramWidgetConfigureActivity.this).get(0);

                Data.deletePref(TramWidgetConfigureActivity.this, mAppWidgetId);

                Data.saveStopPref(TramWidgetConfigureActivity.this, mAppWidgetId, Data.getStopCode(TramWidgetConfigureActivity.this, selected));
                Data.saveDestPref(TramWidgetConfigureActivity.this, mAppWidgetId, mAppDestSpinner.getSelectedItemPosition() <= 0);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                TramWidget.updateAppWidget(context.getApplicationContext(), appWidgetManager, mAppWidgetId, small);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        mReloadButton = findViewById(R.id.reload_button);
        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReloadButton.setEnabled(false);
                new AsyncLoad(getApplicationContext(), mAppWidgetId, small, new AsyncLoad.AsyncLoadListener() {
                    @Override
                    public void onFinished(boolean result, TimeList tl) {
                        mReloadButton.setEnabled(true);
                        if (result) {
                            adapter.clear();
                            adapter.addAll(tl.toStringList());
                            adapter.notifyDataSetChanged();
                            mScrollView.fullScroll(ScrollView.FOCUS_UP);
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                            TramWidget.updateAppWidget(getApplicationContext(), appWidgetManager, mAppWidgetId, small);
                        }
                    }
                }).execute();
            }
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            showNext = extras.getBoolean(Constants.WIDGET_CLICK_REQUEST, false);

            Log.d(Constants.LOGGER_TAG, "appwidgetid:" + mAppWidgetId);

        } else {
            Log.d(Constants.LOGGER_TAG, "no extras : appwidgetid:" + mAppWidgetId);
        }


        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        ArrayAdapter<String> stopsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Data.getStops(this).toArray(new String[0]));
        String[] destsItems = new String[]{Data.getDestination(this, true), Data.getDestination(this, false)};
        ArrayAdapter<String> destsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, destsItems);

        mAppStopSpinner.setAdapter(stopsAdapter);
        mAppDestSpinner.setAdapter(destsAdapter);

        String sselected = Data.loadStopPref(this, mAppWidgetId);
        int iselected = Data.getStops(this).indexOf(Data.getStopName(this, sselected));
        mAppStopSpinner.setSelection(iselected);

        mAppDestSpinner.setSelection(Data.loadDestPref(this, mAppWidgetId) ? 0 : 1);

        findViewById(R.id.next_part).setVisibility(showNext ? View.VISIBLE : View.GONE);

        if (showNext) {
            setResult(RESULT_OK);

            TimeList tl = Data.getSavedDataPref(this, mAppWidgetId);
            if(tl == null)
                tl = new TimeList();
            tl.sort();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, tl.toStringList());
            mListView.setAdapter(adapter);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setResult(showNext ? RESULT_OK : RESULT_CANCELED);
    }
}

