package com.giuseppepaoletti.sei;

/**
 * Created by Giuseppe Paoletti on 04/05/2016.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SeiData
{
    public Integer	mEstrazione[] = new Integer[7];
    public Double	mPremi[] = new Double[6];
    public String	mConcorso = "";
    public String	mRisultato = "";
    public String	mDataFile = "colonne.sei";
    public String	mDataPath = "";
    public String	mConnType = "Manuale";
    public String	mAutoDown = "No";

    private String	mPageUrl1 = "http://www.televideo.rai.it/televideo/pub/solotesto.jsp?pagina=791";
    private String	mPageUrl2 = "http://www.televideo.rai.it/televideo/pub/solotesto.jsp?pagina=791&sottopagina=2";

    public SeiData()
    {
        mDataPath = Environment.getExternalStorageDirectory().toString();
        InizializzaDati();
        Serialize( false, false );
    }

    private void InizializzaDati()
    {
        int	i;

        for( i=0 ; i<mEstrazione.length ; i++ )
        {
            if( mEstrazione[i] == null )
                mEstrazione[i] = new Integer(0);
            mEstrazione[i] = 0;
        }
        for( i=0 ; i<mPremi.length ; i++ )
        {
            if( mPremi[i] == null )
                mPremi[i] = new Double(0.0);
            mPremi[i] = 0.0;
        }
        mConcorso = "";
        mRisultato = "";
    }

    public int ConvertiStringaInNumeri( String riga, Integer[] colonne )
	/*
	 * 	Converte una stringa contenente una serie di numeri separati da uno spazio
	 *	in un vettore di interi
	*/
    {
        Integer		i, ncol;
        String		numero = "";

        for( i=0, ncol=0 ; i<riga.length() ; i++ )
        {
            // se il primo carattere è # la riga è un commento, quindi la salto
            if( i == 0 && riga.charAt(0) == '#' )
                break;
            // se è un numero memorizzo il carattere
            if( riga.charAt(i) >= '0' && riga.charAt(i) <= '9' )
            {
                // lo zero in posizione iniziale lo salto
                if( riga.charAt(i) != '0' || numero.length() > 0 )
                    numero += riga.charAt(i);
            }
            // se c'è uno spazio o è l'ultimo posso convertire il numero
            if( riga.charAt(i) == ' ' || i == riga.length()-1 )
            {
                if( colonne[ncol] == null )
                    colonne[ncol] = new Integer(0);
                colonne[ncol] = 0;
                if( numero.length() > 0 )
                {
                    colonne[ncol] = Integer.decode(numero);
                    ncol++;
                }
                numero = "";
            }
        }

        return ncol;
    }

    public int EstraiDataConcorso( String concorso )
    /*
        Estrae la data concorso in formato numerico (AAMMGG) dalla stringa concorso
    */
    {
        int     i, data = 0;
        String  conc = concorso, val = null, aa = null, mm = null, gg = null;

        for( i=conc.length()-1 ; i>=0 ; i-- )
        {
            if( conc.charAt(i) < '0' || conc.charAt(i) > '9' )
            {
                val = conc.substring( i+1, conc.length() );
                conc = conc.substring( 0, i );
                i = conc.length();
                if( aa == null )
                {
                    aa = val;
                    if( aa.length() == 4 )
                        aa = aa.substring( 2 );
                }
                else if( mm == null )
                    mm = val;
                else if( gg == null )
                {
                    gg = val;
                    break;
                }
            }
        }

        return Integer.decode(aa+mm+gg);
    }

    public void Serialize( Boolean store, Boolean onlyCfg )
	/*
	 * 	Lettura/scrittura parametri programma
	 */
    {
        int					i;
        String				riga = null;
        SharedPreferences	preference = PreferenceManager.getDefaultSharedPreferences(global.activity.getBaseContext());

        if( store == true )
        {
            // scrittura su xml preference
            Editor	editor = preference.edit();
            editor.putString( "pref_directory", mDataPath );
            editor.putString( "pref_connessione", mConnType );
            editor.putString( "pref_auto_down", mAutoDown );
            if( onlyCfg == false )
            {
                // numero e data concorso
                editor.putString( "concorso", mConcorso );
                // numeri estratti
                riga = "";
                for( i=0 ; i<mEstrazione.length ; i++ )
                {
                    riga += String.valueOf( mEstrazione[i] );
                    riga += ' ';
                }
                riga = riga.trim();
                editor.putString( "num_estratti", riga );
                // premi
                for( i=0 ; i<mPremi.length ; i++ )
                    editor.putString( "premio_"+String.valueOf(i), String.valueOf( mPremi[i] ).trim() );
                editor.commit();
            }
        }
        else
        {
            // lettura da xml preference
            mDataPath = preference.getString( "pref_directory", Environment.getExternalStorageDirectory().toString() );
            mConnType = preference.getString( "pref_connessione", "Manuale" );
            mAutoDown = preference.getString( "pref_auto_down", "No" );
            if( onlyCfg == false )
            {
                // numero e data concorso
                mConcorso = preference.getString( "concorso", "" );
                // numeri estratti
                riga = preference.getString( "num_estratti", "" );
                if( riga != null && riga.length() > 0 )
                    ConvertiStringaInNumeri( riga.trim(),  mEstrazione );
                // premi
                for( i=0 ; i<mPremi.length ; i++ )
                {
                    riga = preference.getString( "premio_"+String.valueOf(i), "" );
                    if( riga != null && riga.length() > 0 )
                        mPremi[i] = Double.parseDouble( riga.trim() );
                }
            }
        }
    }

    public void ControllaRisultati()
	/*
	 *	Controlla e visualizza i risultati della vincita
	 */
    {
        Boolean			piu1;
        Integer			i, j, quanti, massimo = 0, colonne[] = new Integer[6],
                punteggio[] = new Integer[6];
        double			vincita = 0.0;
        String			riga = null, commento = "";
        BufferedReader	buf = null;

        mRisultato = "";
        for( i=0 ; i<punteggio.length ; i++ )
            punteggio[i] = new Integer(0);

        try
        {
            buf = new BufferedReader(new FileReader(mDataPath+mDataFile));
            riga = buf.readLine();
            while( riga != null && riga.length() > 0 )
            {
                if( ConvertiStringaInNumeri( riga, colonne ) == colonne.length )
                {
                    // conto quanti numeri estratti sono presenti nelle colonne
                    quanti = 0;
                    piu1 = false;
                    for( i=0, quanti=0 ; i<mEstrazione.length ; i++ )
                    {
                        for( j=0 ; j<colonne.length ; j++ )
                        {
                            if( mEstrazione[i] == colonne[j] )
                            {
                                if( i == mEstrazione.length-1 )
                                    piu1 = true;
                                else
                                    quanti++;
                                break;
                            }
                        }
                    }

                    if( quanti == 2 )			// due
                    {
                        punteggio[0] += 1;
                        vincita += mPremi[0];
                    }
                    else if( quanti == 3 )		// tre
                    {
                        punteggio[1] += 1;
                        vincita += mPremi[1];
                    }
                    else if( quanti == 4 )		// quattro
                    {
                        punteggio[2] += 1;
                        vincita += mPremi[2];
                    }
                    else if( quanti == 5 )		// cinque / cinque + 1
                    {
                        if( piu1 )
                        {
                            punteggio[4] += 1;
                            vincita += mPremi[4];
                        }
                        else
                        {
                            punteggio[3] += 1;
                            vincita += mPremi[3];
                        }
                    }
                    else if( quanti == 6 )
                    {
                        punteggio[5] += 1;
                        vincita += mPremi[5];
                    }
                    // memorizzo il punteggio massimo
                    if( quanti > massimo )
                        massimo = quanti;
                }
                // altrimenti memorizzo la riga di commento (se la lunghezza è sufficiente)
                else
                {
                    if( riga.length() > 2 )
                        commento = riga.substring( 2 );
                }
                riga = buf.readLine();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if( massimo < 2 )
        {
            riga = String.format( "Hai fatto %d\nAndra\' meglio la prossima!", massimo );
            mRisultato += riga;
        }
        else
        {
            if( vincita > 0.0 )
                riga = String.format( "HAI VINTO %.2f EURI!", vincita );
            else
                riga = "HAI VINTO!";
            mRisultato += riga;
            for( i=punteggio.length-1 ; i>=0 ; i-- )
            {
                if( punteggio[i] == 0 )
                    continue;
                mRisultato += '\n';
                mRisultato += Integer.toString(punteggio[i]);
                switch( i )
                {
                    case 0:
                        mRisultato += " due";
                        break;
                    case 1:
                        mRisultato += " tre";
                        break;
                    case 2:
                        mRisultato += "quattro";
                        break;
                    case 3:
                        mRisultato += "cinque";
                        break;
                    case 4:
                        mRisultato += "cinque+1";
                        break;
                    case 5:
                        mRisultato += "sei";
                        break;
                }
            }
            if( !commento.isEmpty() )
            {
                mRisultato += '\n';
                mRisultato += commento;
            }
        }

        Main.WriteLog( mConcorso+mRisultato );
    }

    public Integer ScaricaRisultati( boolean testIfDiff )
	/*
	 *	Scarica e aggiorna i dati dell'estrazione
	 */
    {
        boolean			inPagina = false, qualcheNum = false, isWiFiEnabled = false;
        Integer			i, np, iv = -1, ok = 0, dataConcorso1 = 0, dataConcorso2 = 0, estrazione[] = new Integer[7];
        Double			premi[] = new Double[6];
        URL 			url = null;
        String			line = null, riga = null, tmp = null, concorso1 = null, concorso2 = null;
        BufferedReader	reader = null;

        for( i=0 ; i<estrazione.length ; i++ )
            estrazione[i] = new Integer(0);
        for( i=0 ; i<premi.length ; i++ )
            premi[i] = new Double(0);

        // eventuale attivazione Wi-Fi
        if( mConnType.equals("Wi-Fi") )
        {
            WifiManager wm = (WifiManager)global.activity.getSystemService( Context.WIFI_SERVICE );
            if( wm != null )
            {
                if( wm.isWifiEnabled() )
                    isWiFiEnabled = true;
                else
                    wm.setWifiEnabled( true );
            }
        }
        // se richiesto attendo che venga resa disponibile una connessione
        if( mConnType.equals("Wi-Fi") || mConnType.equals("3G") )
        {
            NetworkInfo			netMobile = null, netWiFi = null;
            ConnectivityManager cm = (ConnectivityManager)global.activity.getSystemService( Context.CONNECTIVITY_SERVICE );
            if( cm != null )
            {
                netMobile = cm.getNetworkInfo( ConnectivityManager.TYPE_MOBILE );
                netWiFi = cm.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
                for( i=0 ; i<20 ; i++ )
                {
                    SystemClock.sleep( 500 );
                    // se devo attivare il Wi-Fi attendo che sia connesso
                    if( mConnType.equals("Wi-Fi") )
                    {
                        if( netWiFi != null && netWiFi.getState() == NetworkInfo.State.CONNECTED )
                            break;
                    }
                    // stessa cosa per la connessione 3G
                    else if( mConnType.equals("3G") )
                    {
                        if( netMobile != null && netMobile.getState() == NetworkInfo.State.CONNECTED )
                            break;
                    }
                }
            }
            // quando esco da qui devo avere una connessione disponibile
            if( ( netMobile == null || netMobile.getState() != NetworkInfo.State.CONNECTED ) &&
                    ( netWiFi == null || netWiFi.getState() != NetworkInfo.State.CONNECTED ) )
            {
                // errore
                Toast.makeText( global.activity.getBaseContext(), R.string.nessuna_connessione, Toast.LENGTH_LONG ).show();
                return 0;
            }
            // se devo usare il Wi-Fi ma ho solo la connessione 3G chiedo il da farsi
            else if( mConnType.equals("Wi-Fi") &&
                    ( netWiFi == null || netWiFi.getState() != NetworkInfo.State.CONNECTED ) )
            {

            }
        }

        for( np=0 ; np<2 ; np++ )
        {
            inPagina = false;

            try
            {
                url = new URL( np==0 ? mPageUrl1:mPageUrl2 );
                URLConnection	conn = url.openConnection();
                conn.setConnectTimeout( 10000 );
                conn.setReadTimeout( 10000 );
                InputStream	is = conn.getInputStream();
                //InputStream	is = new FileInputStream( Environment.getExternalStorageDirectory()+"/Estrazione.html" );
//InputStream	is = url.openStream();
                InputStreamReader	isr = new InputStreamReader( is, "UTF-8" );
                reader = new BufferedReader( isr );
/*HttpGet httpGet = new HttpGet("http://www.televideo.rai.it/televideo/pub/solotesto.jsp?pagina=598");
HttpClient httpClient = new DefaultHttpClient();
HttpResponse response = httpClient.execute(httpGet);
reader = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );*/
                File file = new File(mDataPath, "Pagina.html");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                while( (line = reader.readLine()) != null )
                {
                    riga = line.trim();
                    writer.write(line);
                    writer.write("\r\n");

                    // riga dati concorso, che è la prima cosa che incontro
                    if( ( ( np == 0 && concorso1 == null ) || ( np == 1 && concorso2 == null ) ) && riga.indexOf( "CONCORSO" ) >= 0 )
                    {
                        Main.WriteLog( "WebPage "+riga );
                        for( i=riga.length()-1 ; i>=0 ; i-- )
                        {
                            if( riga.charAt(i) == '/' )
                            {
                                if( np == 0 )
                                    concorso1 = riga.substring( riga.indexOf( "CONCORSO" ), i+5 ).trim();
                                else
                                    concorso2 = riga.substring( riga.indexOf( "CONCORSO" ), i+5 ).trim();
                                inPagina = true;
                                break;
                            }
                        }
                        if( ( np == 0 && !concorso1.isEmpty() ) || ( np == 1 && !concorso2.isEmpty() ) )
                        {
                            if( np == 0 )
                                dataConcorso1 = EstraiDataConcorso( concorso1 );
                            else
                            {
                                dataConcorso2 = EstraiDataConcorso( concorso2 );
                                if( dataConcorso1 >= dataConcorso2 )
                                    break;
                                // nella sottopagina c'è un'estrazione più recente, leggo i dati da lì
                                for( i=0 ; i<estrazione.length ; i++ )
                                    estrazione[i] = 0;
                                for( i=0 ; i<premi.length ; i++ )
                                    premi[i] = 0.0;
                            }
                        }
                    }

                    // se non sono arrivato ancora all'inizio della pagina vado avanti
                    if( inPagina == false )
                        continue;

                    // se contiene la stringa "</pre>" ho terminato
                    if( riga.indexOf( "</pre>") >= 0 )
                        break;

                    // se la riga contiene nelle prime posizioni tutti numeri è la riga dei numeri estratti
                    if( line.length() >= 20 )
                    {
                        qualcheNum = false;
                        for( i=0 ; i<20 && estrazione[0]==0 ; i++ )
                        {
                            if( line.charAt(i) >= '0' && line.charAt(i) <= '9' )
                                qualcheNum = true;
                            else if( line.charAt(i) != ' ' )
                                break;
                        }
                        if( qualcheNum && i == 20 )
                        {
                            Main.WriteLog( "WebPage "+riga );
                            ConvertiStringaInNumeri( line.substring(0,20).trim(), estrazione );
                        }
                    }

                    // numero jolly
                    if( estrazione[6] == 0 && (i=line.indexOf("Numero Jolly")) >= 0 )
                    {
                        Main.WriteLog( "WebPage "+riga );
                        tmp = line.substring( i+12, Math.min(19,line.length()) );
                        tmp = tmp.trim();
                        if( tmp.length() > 0 )
                            estrazione[6] = Integer.decode( tmp );
                    }

                    // da qui in poi ci sono le vincite, che stanno alla fine della riga successiva
                    if( iv >= 0 )
                    {
                        Main.WriteLog( "WebPage vincita "+iv+" "+riga );
                        tmp = "";
                        for( i=Math.max(20,line.length()-14) ; i<line.length() ; i++ )
                        {
                            if( ( line.charAt(i) >= '0' && line.charAt(i) <= '9' ) )
                                tmp += line.charAt(i);
                            else if( line.charAt(i) == ',' )
                                tmp += '.';
                        }
                        if( tmp.length() > 0 )
                            premi[iv] = Double.parseDouble( tmp );
                        iv = -1;
                    }
                    else
                    {
                        iv = -1;
                        if( premi[5] == 0 && riga.indexOf( "\"sei\"" ) >= 0 )
                            iv = 5;
                        else if( premi[4] == 0 && riga.indexOf( "\"cinque+1\"" ) >= 0 )
                            iv = 4;
                        else if( premi[3] == 0 && riga.indexOf( "\"cinque\"" ) >= 0 )
                            iv = 3;
                        else if( premi[2] == 0 && riga.indexOf( "\"quattro\"" ) >= 0 )
                            iv = 2;
                        else if( premi[1] == 0 && riga.indexOf( "\"tre\"" ) >= 0 )
                            iv = 1;
                        else if( premi[0] == 0 && riga.indexOf( "\"due\"" ) >= 0 )
                            iv = 0;
                    }
                }
                writer.close();
            }
            catch (UnsupportedEncodingException e)
            {
                Main.WriteLog( "WebPage UnsupportedEncodingException: "+e.getMessage() );
                Toast.makeText( global.activity.getBaseContext(), e.getMessage(), Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            }
            catch (IOException e)
            {
                Main.WriteLog( "WebPage IOException: "+e.getMessage() );
                Toast.makeText( global.activity.getBaseContext(), e.getMessage(), Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            }
            catch (Exception e)
            {
                Main.WriteLog( "Exception: "+e.getMessage() );
                Toast.makeText( global.activity.getBaseContext(), e.getMessage(), Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            }
            finally
            {
                if( reader != null )
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException logOrIgnore) {}
                }
            }
        }

        // controllo se ho letto correttamente tutti i numeri
        for( i=0 ; i<estrazione.length ; i++ )
        {
            if( estrazione[i] <= 0 || estrazione[i] > 90 )
                break;
        }
        if( i == estrazione.length )
        {
            ok = 1;
            // se richiesto controllo che siano numeri diversi da quelli memorizzati
            if( testIfDiff && estrazione.length == mEstrazione.length )
            {
                for( i=0 ; i<estrazione.length ; i++ )
                {
                    if( mEstrazione[i] != estrazione[i] )
                        break;
                }
                if( i == estrazione.length )
                    ok = 2;
            }
        }
        Main.WriteLog( String.format("WebPage ok %d", ok) );
        if( ok == 1 )
        {
            for( i=0 ; i<estrazione.length ; i++ )
                mEstrazione[i] = estrazione[i];
            for( i=0 ; i<premi.length ; i++ )
                mPremi[i] = premi[i];
            mConcorso = np == 2 ? concorso2 : concorso1;

            Serialize( true, false );
        }

        // eventuale disattivazione Wi-Fi
        if( mConnType.equals("Wi-Fi") && !isWiFiEnabled )
        {
            WifiManager wm = (WifiManager)global.activity.getSystemService( Context.WIFI_SERVICE );
            if( wm.isWifiEnabled() )
                wm.setWifiEnabled( false );
        }

        return ok;
    }

    public Integer GetDownloadMode()
    {
        Integer		mode = 0;
        String		autoDown = global.activity.mData.mAutoDown;
        String		autoMode[] = global.activity.getResources().getStringArray(R.array.auto_down_elements);

        if( autoDown.equalsIgnoreCase(autoMode[1]))
            mode = 1;
        else if( autoDown.equalsIgnoreCase(autoMode[2]))
            mode = 2;
        else if( autoDown.equalsIgnoreCase(autoMode[3]))
            mode = 3;

        return mode;
    }
}
