package com.asix.pixeldailieswidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class UpdateWidgetService extends Service {
    private static final String LOG = "PDService";
    private static String PREFS_NAME = "PD_PREFS";
    private String CACHE_FILE = "dailies_list";
    private static int pdIndex = 0;
    SharedPreferences _prefs;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        //region Index Stuff
        _prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if(_prefs.contains("index")){
            pdIndex = _prefs.getInt("index", 0);
        }else{
            _prefs.edit().putInt("index", pdIndex).apply();
        }


        ArrayList<PDItem> dailiesList = new ArrayList<>();
        if(intent.hasExtra("list")){
            dailiesList = (ArrayList<PDItem>) intent.getSerializableExtra("list");
            try{
                cacheDailiesList(getApplicationContext(), dailiesList);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try{
                dailiesList = loadDailiesfromCache(getApplicationContext());
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        if(intent.hasExtra("navigateBack")){
            if(intent.getBooleanExtra("navigateBack", true)){
                Log.w(LOG, "Previous Pixel Dailies");
                if(pdIndex != dailiesList.size()-1){
                    pdIndex++;
                }
            }else{
                if(pdIndex != 0){
                    Log.w(LOG, "Next Pixel Dailies");
                    pdIndex--;
                }
            }
        }else{
            pdIndex = 0;
        }
        _prefs.edit().putInt("index", pdIndex).apply();
        Log.w(LOG, "Index " + pdIndex + " out of " + (dailiesList.size()-1));
        //endregion

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        PDItem item = dailiesList.get(pdIndex);
        String pdDate = item.getPdDate();
        String pdTheme = item.getPdTheme();

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);

            // Set the theme text
            remoteViews.setTextViewText(R.id.pdTheme, pdTheme);

            //Set the theme date
            remoteViews.setTextViewText(R.id.pdDate, "PD Theme for " + pdDate);

            Log.w(LOG, pdDate + ": " + pdTheme);

            //region Register an onClickListener for widget
            Intent clickIntent = new Intent(this.getApplicationContext(), WidgetProvider.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntentMain = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.pdRefresh, pendingIntentMain);
            //endregion

            //region Register an onClickListener for previous button
            Intent prevIntent = new Intent(this.getApplicationContext(), WidgetProvider.class);

            prevIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            prevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
            prevIntent.putExtra("navigateBack", true);

            PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(getApplicationContext(), 1, prevIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.prevPD, pendingIntentPrev);
            //endregion

            //region Register an onClickListener for next button
            Intent nextIntent = new Intent(this.getApplicationContext(), WidgetProvider.class);

            nextIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
            nextIntent.putExtra("navigateBack", false);

            PendingIntent pendingIntentNext = PendingIntent.getBroadcast(getApplicationContext(), 2, nextIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.nextPD, pendingIntentNext);
            //endregion

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void cacheDailiesList(Context context, ArrayList<PDItem> list) throws IOException{
        FileOutputStream fos = context.openFileOutput (CACHE_FILE, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject (list);
        oos.close ();
        fos.close ();
        Log.w(LOG, "List has been cached");
    }

    private ArrayList<PDItem> loadDailiesfromCache(Context context) throws IOException, ClassNotFoundException{
        FileInputStream fis = context.openFileInput (CACHE_FILE);
        ObjectInputStream ois = new ObjectInputStream (fis);
        ArrayList<PDItem> list = (ArrayList<PDItem>) ois.readObject ();
        Log.w(LOG, "Returning cached list");
        return list;
    }
}
