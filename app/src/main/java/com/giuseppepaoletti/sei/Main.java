package com.giuseppepaoletti.sei;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import com.appaholics.updatechecker.*;

public class Main extends AppCompatActivity
{
    public static final int NOTIFICATION_ID = 1;
    public static final int RETCODE_SETTINGS = 1, RETCODE_COLUMNS = 2;
    public static final int DOWNLOAD_MODE_INTERACTIVE = 0, DOWNLOAD_MODE_TIMER = 1;

    private String	mVersionUrl = "http://giuseppepaoletti.altervista.org/sei/sei.txt";
    private String	mApkUrl = "http://giuseppepaoletti.altervista.org/sei/Sei.apk";

    private Alarm	mAlarm;
    public 	SeiData	mData;

    public int getVersion()
    {
        int v = 0;
        try
        {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        }
        catch (NameNotFoundException e)
        {
        }
        return v;
    }

    public static void WriteLog( String str )
    {
        File 				file = new File( global.activity.mData.mDataPath, "Sei.log" );
        SimpleDateFormat 	sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss.SSS  " );
        String 				time = sdf.format( Calendar.getInstance().getTime() );
        BufferedWriter		writer;
        try
        {
            writer = new BufferedWriter( new FileWriter(file, true) );
            writer.write( time+str );
            writer.write("\r\n");
            writer.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    private void ImpostaAllarme( boolean setAlarm )
	/*
	 * 	impostazione timer per aggiornamento automatico
	*/
    {
        // impostazione timer per aggiornamento automatico
        mAlarm.CancelAlarm( this.getApplicationContext() );
        if( setAlarm && !mData.mAutoDown.equalsIgnoreCase("No") )
            mAlarm.SetAlarm( this.getApplicationContext(), false, false );
    }

    private void ControllaVisualizzaRisultati()
	/*
	 *	Controlla e visualizza i risultati della vincita
	 */
    {
        String	result = "";

        mData.ControllaRisultati();

        // numeri estratti
        if( mData.mEstrazione[0] != 0 )
            ((EditText)findViewById(R.id.editNumeri)).setText( String.format( "%d %d %d %d %d %d %d", mData.mEstrazione[0], mData.mEstrazione[1], mData.mEstrazione[2],
                    mData.mEstrazione[3], mData.mEstrazione[4], mData.mEstrazione[5], mData.mEstrazione[6] ) );
        // risultato estrazione
        if( mData.mConcorso.length() > 0 )
        {
            result = mData.mConcorso;
            result += '\n';
        }
        if( mData.mRisultato.length() > 0 )
            result += mData.mRisultato;
        ((TextView)findViewById(R.id.textViewRis)).setText( result );
    }

    public void KillMe()
    {
        ImpostaAllarme( false );
        super.onDestroy();
        this.finish();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    public void StartDownloadAsyncTask( Integer mode )
    {
        new DownloadAsyncTask().execute( mode );
    }

    private class DownloadAsyncTask extends AsyncTask<Integer, Integer, Integer>
    {
        Integer mMode = 0;

        @Override
        protected Integer doInBackground(Integer... arg0)
        {
            mMode = arg0[0];
            WriteLog( "OnClickListener Scarica mode " + mMode );
            return mData.ScaricaRisultati( mMode==DOWNLOAD_MODE_TIMER );
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            if( mMode == DOWNLOAD_MODE_TIMER )
            {
                Main mainActivity = global.activity;
                if( mainActivity != null )
                {
                    PowerManager pm = (PowerManager)mainActivity.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
                    wl.acquire();
                    if( result == 1 )
                    {
                        //mainActivity.mData.ControllaRisultati();
                        Intent intent1 = new Intent(mainActivity, Main.class);
                        intent1.putExtra("IntentID", Main.NOTIFICATION_ID);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mainActivity, 0, intent1, Intent.FILL_IN_ACTION);
                        Notification notify = new NotificationCompat.Builder(mainActivity)
                                .setContentTitle(mainActivity.mData.mConcorso)
                                .setContentText(mainActivity.mData.mRisultato)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setDefaults(Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                .setContentIntent(pendingIntent)
                                .setLights(Color.BLUE, 300, 1000)
                                .build();
                        notify.flags = Notification.FLAG_AUTO_CANCEL;
                        NotificationManager notificationManager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(Main.NOTIFICATION_ID, notify);
                    }
                    // next
                    mainActivity.mAlarm.SetAlarm( mainActivity, result==0, true );

                    wl.release();
                }
                else
                    Main.WriteLog( "onReceive mainActivity NULL!" );
            }
            else
            {
                if( result != 0 )
                    ControllaVisualizzaRisultati();
            }
        }
    }
    private class UpdateAsyncTask extends AsyncTask<Context, Integer, Integer>
    {
        UpdateChecker mChecker = null;

        @Override
        protected Integer doInBackground(Context... arg0)
        {
            mChecker = new UpdateChecker( arg0[0], true );
            mChecker.checkForUpdateByVersionCode( mVersionUrl );
            if( mChecker.isUpdateAvailable() )
                return 1;
            else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            if( result == 1 )
                mChecker.downloadAndInstall(mApkUrl);
            else
                Toast.makeText( getApplicationContext(), R.string.programma_aggiornato, Toast.LENGTH_LONG ).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        global.activity = this;
        setContentView(R.layout.activity_main);

        // inizializzazione dati
        mData = new SeiData();

        WriteLog( "OnCreate" );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton scarica = (FloatingActionButton) findViewById(R.id.scarica);
        scarica.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                StartDownloadAsyncTask( DOWNLOAD_MODE_INTERACTIVE );
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        ((EditText)findViewById(R.id.editNumeri)).setText( "" );
        ((TextView)findViewById(R.id.textViewRis)).setText( "" );

        // visualizzazione dati ultima estrazione ed esito
        ControllaVisualizzaRisultati();

        // impostazione timer per aggiornamento automatico
        mAlarm = new Alarm();
        ImpostaAllarme( true );
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        WriteLog( "onStart" );
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        WriteLog( "onRestart" );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        WriteLog( "onResume" );
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        WriteLog( "onPause" );
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        WriteLog( "onStop" );
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        WriteLog( "onDestroy" );
    }

    public void onBackPressed()
    {
        // salvataggio
        mData.Serialize( true, false );
        // chiusura definitiva se non c'è l'auto download, altrimenti solo background
        if( mData.mAutoDown.equalsIgnoreCase( "No" ) )
        {
            WriteLog( "onBackPressed (destroy)" );
            KillMe();
        }
        else
        {
            WriteLog( "onBackPressed (background)" );
            moveTaskToBack(true);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menuOpzioni)
        {
            WriteLog( "menu Opzioni" );
            Intent intentPref = new Intent( Main.this, Preferences.class );
            startActivityForResult( intentPref, RETCODE_SETTINGS );
            return true;
        }
        else if (id == R.id.menuColonne)
        {
            WriteLog( "menu Colonne" );
            Intent intentCol = new Intent( Main.this, Colonne.class );
            intentCol.putExtra( "dataFile", mData.mDataPath+mData.mDataFile );
            startActivityForResult( intentCol, RETCODE_COLUMNS );
            return true;
        }
        else if (id == R.id.menuAggiorna)
        {
            // controllo aggiornamento versione
            new UpdateAsyncTask().execute( this );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        // ritorno dalla configurazione, rileggo le impostazioni
        if( requestCode == RETCODE_SETTINGS )
        {
            WriteLog( "menu Opzioni (exit)" );
            mData.Serialize( false, true );
            // impostazione timer per aggiornamento automatico
            ImpostaAllarme( true );
        }
        else if( requestCode == RETCODE_COLUMNS )
        {
            WriteLog( "menu Colonne (exit)" );
            if( resultCode == RESULT_OK )
                Toast.makeText( getBaseContext(), R.string.salvataggio_eseguito, Toast.LENGTH_LONG ).show();
        }
    }

    protected void onNewIntent( Intent intent )
    {
        int	intExtra = intent.getIntExtra("IntentID", 0);

        WriteLog( String.format("onNewIntent intExtra %d", intExtra) );
        if( intExtra == NOTIFICATION_ID )
            ControllaVisualizzaRisultati();
        //Toast.makeText( getBaseContext(), "Intent ricevuto!!!", Toast.LENGTH_LONG ).show();
    }
}
