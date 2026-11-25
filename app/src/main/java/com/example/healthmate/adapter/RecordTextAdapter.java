package com.example.healthmate.adapter;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import com.example.healthmate.model.HealthRecord;

/** Small UI adapter that turns a health record into the summary text used by the screens. */
public class RecordTextAdapter {
    public static TextView createSummary(Context context, HealthRecord record, int color) {
        TextView summary = new TextView(context);
        summary.setText(record.steps + " steps  •  " + record.calories + " kcal  •  " + record.water + " ml water");
        summary.setTextSize(11);
        summary.setTextColor(color);
        summary.setGravity(Gravity.CENTER_VERTICAL);
        return summary;
    }
}
