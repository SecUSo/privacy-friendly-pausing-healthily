package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    static String time = "";
    static RemoteViews views = null;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        CharSequence widgetText = prefs.getString("name_text", "Help");
        int workTime = prefs.getInt("work_value", 0);
        String clockTime = "";

        if(workTime<10)
            clockTime = "0" + workTime + ":00";
        else
            clockTime = workTime + ":00";

        // Construct the RemoteViews object

        int minWidth = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        getRemoteViews(context, minWidth, minHeight);


        views.setTextViewText(R.id.appwidget_text, widgetText);
        if (time.equals(""))
            views.setTextViewText(R.id.time, clockTime);
        else
            views.setTextViewText(R.id.time, time);


        //Open App if clicked
        Intent openApp = new Intent(context, BreakReminder.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        views.setOnClickPendingIntent(R.id.time, pendingIntent);

        // Instruct the widget_preview manager to update the widget_preview
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget_preview is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget_preview is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String data = "";
        Bundle getPrevData = intent.getExtras();
        if (getPrevData != null) {
            data = getPrevData.getString("time");
            if (data != null)
                time = data;
        }
        super.onReceive(context, intent);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {


        System.out.println("Minimal width: " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) + " minimal height: " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));

        // Get min width and height.
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);


        getRemoteViews(context, minWidth, minHeight);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        CharSequence widgetText = prefs.getString("name_text", "Help");
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewText(R.id.time, time);

        //Open App if clicked
        Intent openApp = new Intent(context, BreakReminder.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_NO_CREATE);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        views.setOnClickPendingIntent(R.id.time, pendingIntent);

        // Obtain appropriate widget_preview and update it.
        appWidgetManager.updateAppWidget(appWidgetId, views);

        updateAppWidget(context,appWidgetManager,appWidgetId);

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private static RemoteViews getRemoteViews(Context context, int minWidth, int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);


        int minValue = 0;
        if (columns < rows) {
            minValue = columns;
        } else {
            minValue = rows;
        }
        // Now you changing layout base on you column count
        // In this code from 1 column to 4
        // you can make code for more columns on your own.
        switch (minValue) {
            case 1:
                views = new RemoteViews(context.getPackageName(), R.layout.app_widget2x1);
                return views;
            case 2:
                views = new RemoteViews(context.getPackageName(), R.layout.app_widget2x2);
                return views;
            case 3:
                views = new RemoteViews(context.getPackageName(), R.layout.app_widget3x3);
                return views;
            case 4:
                views = new RemoteViews(context.getPackageName(), R.layout.app_widget4x4);
                return views;
            default:
                views = new RemoteViews(context.getPackageName(), R.layout.app_widget4x4);
                return views;
        }


    }

    /**
     * Returns number of cells needed for given size of the widget_preview.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }
}

