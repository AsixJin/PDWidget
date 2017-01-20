package com.asix.pixeldailieswidget;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

    @Override
    public void onUpdate(final Context context,final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.w(LOG, "onUpdate called");
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(context, new Twitter(authConfig));

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        final int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        TwitterApiClient guestApiClient = TwitterCore.getInstance().getGuestApiClient();
        guestApiClient.getStatusesService().userTimeline(null, "pixel_dailies", 20, null, null, null, true, null, false).enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                //Get the tweets
                List<Tweet> pdTimeline = result.data;

                //Get Theme
                boolean done = false;
                for (Tweet t:pdTimeline) {
                    done = getPDTweet(t);
                    if(done){
                        break;
                    }
                }

                // Build the intent to call the service
                Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
                intent.putExtra("theme", pdTheme);
                intent.putExtra("date", pdDate);

                // Update the widgets via the service
                context.startService(intent);
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

    //Determines if the tweet is a valid pd tweet
    //and sets the theme/date and returns true.
    //Otherwise it will return false if the tweet
    //isn't a pd tweet.
    private boolean getPDTweet(Tweet tweet){
        boolean isThemeTweet = false;
        if(tweet.retweetedStatus == null){
            isThemeTweet = true;
            TweetEntities entities = tweet.entities;
            for (HashtagEntity he :entities.hashtags) {
                if(!he.text.equalsIgnoreCase("pixel_dailies")){
                    pdTheme = he.text;
                    pdDate = tweet.createdAt.substring(4, 10);
                }
            }

        }
        return isThemeTweet;
    }
}
