package com.asix.pixeldailieswidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.tweetui.TweetUtils;

import io.fabric.sdk.android.Fabric;

public class WidgetProvider extends AppWidgetProvider {

    private static final String LOG = "PDWidget";

    private static final String TWITTER_KEY = "bzkIcWvFGvworcB9g6KBxR67B";
    private static final String TWITTER_SECRET = "RpVY5vZL4CGDF2v5RGQUVi9wKJfL6MsHbPeEYL04MOgZTNOuWk";

    private String pdTheme = "";

    ArrayList<Tweet> tweets = new ArrayList<>();

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

    }

    @Override
    public void onUpdate(final Context context,final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.w(LOG, "onUpdate called");
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(context, new Twitter(authConfig));

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        intent.putExtra("theme", pdTheme);

        // Update the widgets via the service
        context.startService(intent);

        TwitterCore.getInstance().getApiClient(session).getStatusesService()
                .userTimeline(null, "pixel_dailies", 10, null, null, null, null, null, null,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        for (Tweet t : result.data) {
                            tweets.add(t);
                            android.util.Log.d("twittercommunity", "tweet is " + t.text);
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        android.util.Log.d("twittercommunity", "exception " + exception);
                    }
                });

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
}
