package com.giuseppepaoletti.sei;

/**
 * Created by Giuseppe Paoletti on 27/05/2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Main.WriteLog( String.format("onReceive RECEIVE_BOOT_COMPLETED") );
        //SeiData data = new SeiData();
        //if( data.GetDownloadMode() != 0 )
        {
            Intent i = new Intent( context, Main.class );
            i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            i.putExtra( "startOnBoot", true );
            context.startActivity( i );
        }
    }
}