package com.asix.pixeldailieswidget;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;


import io.fabric.sdk.android.Fabric;

public class WidgetProvider extends AppWidgetProvider {

    private static final String LOG = "PDProvider";

    private static final String TWITTER_KEY = "bzkIcWvFGvworcB9g6KBxR67B";
    private static final String TWITTER_SECRET = "RpVY5vZL4CGDF2v5RGQUVi9wKJfL6MsHbPeEYL04MOgZTNOuWk";

    private String pdTheme = "ERROR (No Theme)";
    private String pdDate = "ERROR (No Date)";

    private ArrayList<PDItem> dailiesList = new ArrayList<>();

    private boolean navigating = false;
    private boolean navigateBack = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra("navigateBack")){
            navigating = true;
            navigateBack = intent.getBooleanExtra("navigateBack", false);
        }else{
            navigating = false;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.w(LOG, "onUpdate called");
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(context, new Twitter(authConfig));

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        final int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        //Setup and call the twitter API
        if(navigating){
            Log.w(LOG, "Navigating...Not loading list");
            startService(context, allWidgetIds);
        }else{
            Log.w(LOG, "Refreshing list");
            TwitterApiClient guestApiClient = TwitterCore.getInstance().getGuestApiClient();
            guestApiClient.getStatusesService().userTimeline(null, "pixel_dailies", 210, null, null, null, true, null, false).enqueue(new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                    //Get the tweets
                    List<Tweet> pdTimeline = result.data;

                    //Get Theme List
                    for (Tweet t:pdTimeline) {
                        getPDTweet(t);
                    }

                    startService(context, allWidgetIds);
                }

                @Override
                public void failure(TwitterException exception) {
                    pdTheme = "Failure";

                    //Print Error to the log
                    Log.e(LOG, exception.getMessage());

                    // Build the intent to call the service
                    Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
                    intent.putExtra("theme", pdTheme);
                    intent.putExtra("date", pdDate);

                    // Update the widgets via the service
                    context.startService(intent);
                }
            });
        }

    }

    private void startService(Context context, int[] allWidgetIds){
        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        if(navigating){
            intent.putExtra("navigateBack", navigateBack);
        }else{
            intent.putExtra("list", dailiesList);
        }

        // Update the widgets via the service
        context.startService(intent);
    }


    //Determines if the tweet is a valid pd tweet
    //and sets the theme/date and returns true.
    //Otherwise it will return false if the tweet
    //isn't a pd tweet.
    private boolean getPDTweet(Tweet tweet){
        boolean isThemeTweet = false;
        if(tweet.text.contains("theme") || tweet.text.contains("task")){
            isThemeTweet = true;
            TweetEntities entities = tweet.entities;
            for (HashtagEntity he :entities.hashtags) {
                if(!he.text.equalsIgnoreCase("pixel_dailies") && !he.text.equalsIgnoreCase(pdTheme) && !he.text.equalsIgnoreCase("set")){
                    pdTheme = he.text;
                    pdDate = tweet.createdAt.substring(4, 10);
                    PDItem item = new PDItem(pdTheme, pdDate);
                    dailiesList.add(item);
                    break;
                }
            }

        }
        return isThemeTweet;
    }


}
