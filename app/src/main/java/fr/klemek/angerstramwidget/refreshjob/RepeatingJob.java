package fr.klemek.angerstramwidget.refreshjob;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import fr.klemek.angerstramwidget.utils.Constants;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RepeatingJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(Constants.LOGGER_TAG, "onStartJob");
        Intent intent = new Intent(Constants.JOB_TICK);
        sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}