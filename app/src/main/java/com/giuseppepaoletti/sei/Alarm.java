package com.giuseppepaoletti.sei;

/**
 * Created by Giuseppe Paoletti on 04/05/2016.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.giuseppepaoletti.sei.R;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

public class Alarm extends BroadcastReceiver
{
    //public static Integer	mMode = -1;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Main.WriteLog( "onReceive intent: "+intent.getAction() );
        if( intent.getAction().equalsIgnoreCase("Sei.START_ALARM") )
        {
            Main	mainActivity = global.activity;
            if( mainActivity != null )
            {
                mainActivity.StartDownloadAsyncTask( Main.DOWNLOAD_MODE_TIMER );
                /*PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
                wl.acquire();

                // Put here YOUR code.
                Integer retCode = mainActivity.mData.ScaricaRisultati( true );
                if( retCode == 1 )
                {
                    mainActivity.mData.ControllaRisultati();
                    Intent intent1 = new Intent( mainActivity, Main.class );
                    intent1.putExtra( "IntentID", Main.NOTIFICATION_ID );
                    PendingIntent pendingIntent = PendingIntent.getActivity( mainActivity, 0, intent1, Intent.FILL_IN_ACTION );
                    Notification notify = new NotificationCompat.Builder( mainActivity )
                            .setContentTitle( mainActivity.mData.mConcorso )
                            .setContentText( mainActivity.mData.mRisultato )
                            .setSmallIcon( R.mipmap.ic_launcher )
                            .setDefaults( Notification.FLAG_SHOW_LIGHTS|Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE )
                            .setContentIntent( pendingIntent )
                            .setLights( Color.BLUE, 300, 1000 )
                            .build();
                    notify.flags = Notification.FLAG_AUTO_CANCEL;
                    NotificationManager notificationManager = (NotificationManager)mainActivity.getSystemService( Context.NOTIFICATION_SERVICE );
                    notificationManager.notify( Main.NOTIFICATION_ID, notify );
                }
                // next
                SetAlarm(context, retCode==0 ? true:false, true);

                wl.release();*/
            }
            else
                Main.WriteLog( "onReceive mainActivity NULL!" );
        }
    }

    public void SetAlarm(Context context, boolean failed, boolean nextDay)
    {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("Sei.START_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.set(AlarmManager.RTC_WAKEUP, GetNextAlarm(failed, nextDay).getTimeInMillis(), pi);
    }

    public void CancelAlarm(Context context)
    {
        Main.WriteLog( "CancelAlarm" );
        Intent intent = new Intent("Sei.START_ALARM");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public Calendar GetNextAlarm(boolean failed, boolean nextDay)
    {
        Integer	hour, mode = global.activity.mData.GetDownloadMode();
        Calendar	calNow = Calendar.getInstance(), calNext = Calendar.getInstance();

        //	se ho fallito, imposto l'allarme successivo in base all'ora corrente
        if( failed )
        {
            hour = calNow.get( Calendar.HOUR_OF_DAY );
            if( hour >= 8 && hour < 14 )
                mode = 2;
            else if( hour >= 14 && hour < 21 )
                mode = 3;
            else if( hour >= 21 || hour < 8 )
                mode = 1;
        }
        if( mode == 1 )
        {
            calNext.set(Calendar.HOUR_OF_DAY, 8);
            calNext.set(Calendar.MINUTE, 0);
        }
        else if( mode== 2 )
        {
            calNext.set(Calendar.HOUR_OF_DAY, 14);
            calNext.set(Calendar.MINUTE, 30);
        }
        else if( mode == 3)
        {
            calNext.set(Calendar.HOUR_OF_DAY, 21);
            calNext.set(Calendar.MINUTE, 0);
        }
        calNext.set(Calendar.SECOND, 0);
        calNext.set(Calendar.MILLISECOND, 0);
        // se richiesto, va al giorno successivo se inferiore
        if( nextDay && calNext.getTimeInMillis() < calNow.getTimeInMillis() )
            calNext.add(Calendar.DAY_OF_MONTH, 1);
//calNext = calNow;
//calNext.add(Calendar.MINUTE, 2);
        SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss.SSS" );
        Main.WriteLog( String.format("GetNextAlarm: failed %d, nextDay %d, at %s", failed?1:0, nextDay?1:0, sdf.format(calNext.getTime())) );

        return calNext;
    }
}