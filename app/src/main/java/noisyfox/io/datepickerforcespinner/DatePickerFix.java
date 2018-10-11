package noisyfox.io.datepickerforcespinner;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class DatePickerFix {
    private final static String TAG = DatePickerFix.class.getSimpleName();

    /**
     * Double check the DatePicker style using Reflection.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("PrivateApi")
    private static void checkDatePicker(DatePicker p) throws Exception {

        // First use a more safer way
        if (p.getSpinnersShown()) {
            // Pretty straightforward
            return;
        }
        try {
            // This method throws UnsupportedOperationException if called when the picker is
            // displayed in calendar mode
            p.getCalendarView();

            // That means if we reach here, it's spinner mode! Yeah!
            return;
        } catch (UnsupportedOperationException ignored) {
        }

        // Try some dangerous things. I mean reflection.

        // Get the android.widget.DatePicker#mDelegate field so we could know which implementation is used.
        Class<?> delegateClass = Class.forName("android.widget.DatePicker$DatePickerDelegate");
        Field delegateField = findField(DatePicker.class, delegateClass, "mDelegate");
        Object delegate = delegateField.get(p);

        // This is the expected implementation class with 3 spinners instead of a calendar.
        Class<?> spinnerDelegateClass;
        try {
            // For android [5.0, 6.0], the DatePickerSpinnerDelegate is an inner class.
            spinnerDelegateClass = Class.forName("android.widget.DatePicker$DatePickerSpinnerDelegate");
        } catch (ClassNotFoundException ignored) {
            // For android [7.0,), the DatePickerSpinnerDelegate is moved out to a normal class.
            spinnerDelegateClass = Class.forName("android.widget.DatePickerSpinnerDelegate");
        }

        // Check if it's the correct implementation
        if (delegate.getClass() != spinnerDelegateClass) {
            // Oops, still not spinner style! Need to fix this!
            Log.w(TAG, "DatePicker still has none-spinner style! Try to fix it with reflection.");

            // Save the default parameters
            int year = p.getYear();
            int month = p.getMonth();
            int dayOfMonth = p.getDayOfMonth();

            delegateField.set(p, null); // throw out the DatePickerCalendarDelegate!
            p.removeAllViews(); // remove the DatePickerCalendarDelegate views

            // Instantiate a DatePickerSpinnerDelegate
            Constructor spinnerDelegateConstructor = spinnerDelegateClass
                    .getDeclaredConstructor(DatePicker.class, Context.class, AttributeSet.class, int.class, int.class);
            spinnerDelegateConstructor.setAccessible(true);
            delegate = spinnerDelegateConstructor.newInstance(p, p.getContext(), null, android.R.attr.datePickerStyle, 0);

            // set the DatePicker.mDelegate to the spinner delegate
            delegateField.set(p, delegate);

            // Set up the DatePicker again, with the DatePickerSpinnerDelegate
            p.updateDate(year, month, dayOfMonth);
            p.setCalendarViewShown(false);
            p.setSpinnersShown(true);
        }
    }

    private static Field findField(Class objectClass, Class fieldClass, String expectedName) {
        try {
            Field field = objectClass.getDeclaredField(expectedName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
        }
        // search for it if it wasn't found under the expected ivar name
        for (Field searchField : objectClass.getDeclaredFields()) {
            if (searchField.getType() == fieldClass) {
                searchField.setAccessible(true);
                return searchField;
            }
        }
        return null;
    }

    private static boolean inCreateDatePicker = false;

    private static DatePicker createDatePicker(View parent, Context context, AttributeSet attrs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            // This issue is fixed in API 25 (N_MR1)
            return null;
        }

        if (inCreateDatePicker) {
            // Make sure we don't get trapped in an infinite loop.
            return null;
        }
        inCreateDatePicker = true;
        try {
            // Get the original view ID so when we replace the view with ours the caller can find the correct view with that id.
            int[] attrsArray = new int[]{android.R.attr.id};
            TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
            int id = ta.getResourceId(0, View.NO_ID);
            ta.recycle();

            if (id == View.NO_ID) {
                // Something goes wrong, fall-back to default impl.
                return null;
            }

            // Inflate our own DatePicker
            View v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.date_picker_dialog, (ViewGroup) parent, false);
            DatePicker p = v.findViewById(R.id.datePicker);

            // Make sure the date picker has the correct style.
            checkDatePicker(p);

            // Override the View ID.
            p.setId(id);

            return p;
        } catch (Throwable ignored) {
            // Fail-safe
            return null;
        } finally {
            inCreateDatePicker = false;
        }
    }

    /**
     * Fix DatePicker style on Android between [5.0, 7.0].
     *
     * @return the fixed {@link DatePicker}, or null if we should use the default system impl.
     */
    public static View fixDatePicker(View parent, String name, Context context, AttributeSet attrs) {
        if ("DatePicker".equals(name)) {
            return createDatePicker(parent, context, attrs);
        }

        return null;
    }
}
