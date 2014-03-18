package com.ucasoft.controls.dialogs;

import android.annotation.TargetApi;
import android.app.*;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import com.ucasoft.controls.R;

import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Created by UCASoft.
 * User: Antonov Sergey
 * Date: 15.03.14
 * Time: 16:07
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WeekdaysPicker extends DialogFragment {

    public interface OnWeekdaysListener {
        void OnWeekdaysSelected(int[] weekdays);
    }

    private final String TAG = "WeekdaysPicker";
    private OnWeekdaysListener listener;
    private TreeSet<Integer> selectedDays = new TreeSet<Integer>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMultiChoiceItems(getWeekDays(), getSelectedDays(), new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        int firstDay = Calendar.getInstance().getFirstDayOfWeek();
                        int dayOfWeek = i + firstDay;
                        if (dayOfWeek > 7)
                            dayOfWeek = dayOfWeek - 7;
                        if (selectedDays.contains(dayOfWeek))
                            selectedDays.remove(dayOfWeek);
                        else
                            selectedDays.add(dayOfWeek);
                    }
                })
                .setTitle(R.string.weekdays_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null) {
                            listener.OnWeekdaysSelected(getIntResult());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    public void setSelectedDays(int[] weekdays) {
        if (weekdays != null) {
            for (int weekday : weekdays) {
                selectedDays.add(weekday);
            }
        }
    }

    private int[] getIntResult() {
        int[] result = new int[selectedDays.size()];
        int i = 0;
        for(Integer day : selectedDays) {
            result[i++] = day;
        }
        return result;
    }

    private boolean[] getSelectedDays(){
        boolean[] result = new boolean[7];
        int firstDay = Calendar.getInstance().getFirstDayOfWeek();
        for (int day : selectedDays) {
            int i = day - firstDay;
            if (i < 0) {
                i += 7;
            }
            result[i] = true;
        }
        return result;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    public void setOnWeekdaysListener(OnWeekdaysListener listener) {
        this.listener = listener;
    }

    private String[] getWeekDays() {
        String[] result = new String[7];
        String[] weekdays = DateFormatSymbols.getInstance().getWeekdays();
        int firstDay = Calendar.getInstance().getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            result[i] = weekdays[firstDay];
            if (firstDay == 7)
                firstDay = 1;
            else
                firstDay++;
        }
        return result;
    }
}
