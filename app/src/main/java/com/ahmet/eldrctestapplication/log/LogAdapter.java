package com.ahmet.eldrctestapplication.log;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ahmet.eldrctestapplication.R;

import java.util.List;

/**
 * Adapter for displaying log entries in a ListView.
 * @author Ahmet TOPAK
 * @version 1.2
 * @since 7/23/2024
 */

public class LogAdapter extends ArrayAdapter<LogEntry> {

    private final List<LogEntry> logList;
    private final Handler mainHandler;
    private final LayoutInflater inflater;
    private final ListView listView; // Add this line to hold a reference to the ListView

    public LogAdapter(@NonNull Context context, List<LogEntry> logList, ListView listView) {
        super(context, R.layout.simple_list_item, logList);
        this.logList = logList;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.inflater = LayoutInflater.from(context);
        this.listView = listView;  // Initialize the ListView reference
    }

    public void log(final LogEntry.LogType type, final String message) {
        mainHandler.post(() -> {
            logList.add(new LogEntry(type, message));
            notifyDataSetChanged();

            // Scroll to the bottom of the ListView
            if (listView != null) {
                listView.postDelayed(() -> listView.setSelection(logList.size() - 1), 100);
            }
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.simple_list_item, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LogEntry logEntry = logList.get(position);
        holder.textView.setText(logEntry.getMessage());
        holder.textView.setTextColor(getLogColor(logEntry.getType()));

        return convertView;
    }

    private int getLogColor(LogEntry.LogType type) {
        switch (type) {
            case ERROR:
                return getContext().getResources().getColor(android.R.color.holo_red_light);
            case DEBUG:
                return getContext().getResources().getColor(android.R.color.holo_blue_light);
            case INFO:
            default:
                return getContext().getResources().getColor(android.R.color.darker_gray);
        }
    }

    private static class ViewHolder {
        TextView textView;
    }
}
