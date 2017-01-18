package com.asix.pixeldailieswidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;

import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.tweetui.TweetUtils;

import io.fabric.sdk.android.Fabric;

public class UpdateWidgetService extends Service {
    private static final String LOG = "de.vogella.android.widget.example";
    private static final String TWITTER_KEY = "bzkIcWvFGvworcB9g6KBxR67B";
    private static final String TWITTER_SECRET = "RpVY5vZL4CGDF2v5RGQUVi9wKJfL6MsHbPeEYL04MOgZTNOuWk";

    Callback<List<Tweet>> callback;

    ArrayList<Tweet> listOfTweets = new ArrayList<>();
    Tweet pdTweet = null;

    private String pdDate_Format = "";

    private String pdDate = "";
    private String pdTheme = "";

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
//        callback = new Callback<List<Tweet>>() {
//            @Override
//            public void success(Result<List<Tweet>> result) {
//                listOfTweets.addAll(result.data);
//
//                boolean isThemeTweet = false;
//
//                for (Tweet t:listOfTweets) {
//                    isThemeTweet = getPDTweet(t);
//                    if(isThemeTweet)
//                        break;
//                }
//
//                if(isThemeTweet){
//                    setupWidget(intent, startId);
//                }
//
//            }
//
//            @Override
//            public void failure(TwitterException exception) {
//                pdTheme = "XXXERRORXXXX";
//                Log.e("PDWidget", exception.getMessage());
//                setupWidget(intent, startId);
//            }
//        };
//
//        final List<Long> tweetIds = Arrays.asList(2586535099L);
//
//        TweetUtils.loadTweets(tweetIds, callback);

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean getPDTweet(Tweet tweet){
        boolean isThemeTweet = false;
        if(tweet.retweetedStatus == null){
            isThemeTweet = true;
            TweetEntities entities = tweet.entities;
            for (HashtagEntity he :entities.hashtags) {
                if(he.text != "pixel_dailies"){
                    pdTheme = he.text;
                }
            }

        }
        return isThemeTweet;
    }

    private void setupWidget(Intent intent, int startId){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for (int widgetId : allWidgetIds) {
            // create some random data
            int number = (new Random().nextInt(100));

            RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);
            Log.w("WidgetExample", String.valueOf(number));

            // Set the theme text
            remoteViews.setTextViewText(R.id.pdTheme, pdTheme);

            // Set the text
            remoteViews.setTextViewText(R.id.update, "Random: " + String.valueOf(number));

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(), WidgetProvider.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

}
