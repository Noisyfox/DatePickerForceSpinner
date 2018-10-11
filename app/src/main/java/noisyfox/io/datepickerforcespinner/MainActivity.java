package noisyfox.io.datepickerforcespinner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private boolean fixOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);
        // Show sample html
        webView.loadUrl("file:///android_asset/sample.html");

        findViewById(R.id.btnDatePicker).setOnClickListener(v -> {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        });
        ((ToggleButton) findViewById(R.id.toggleFix)).setOnCheckedChangeListener((v, c) -> fixOn = c);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }


    // Magic happens!

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View fix = fixOn ? DatePickerFix.fixDatePicker(parent, name, context, attrs) : null;

        return fix == null ? super.onCreateView(parent, name, context, attrs) : fix;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View fix = fixOn ? DatePickerFix.fixDatePicker(null, name, context, attrs) : null;

        return fix == null ? super.onCreateView(name, context, attrs) : fix;
    }
}
