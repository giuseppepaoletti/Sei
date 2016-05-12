package com.giuseppepaoletti.sei;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.giuseppepaoletti.sei.R;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class Colonne extends AppCompatActivity
{
    private String	mColonne = "";
    private String	mDataFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colonne);

        Serialize( false );
        ((EditText)findViewById(R.id.editColonne)).setText( mColonne );

        FloatingActionButton salva = (FloatingActionButton) findViewById(R.id.salva);
        salva.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mColonne = ((EditText)findViewById(R.id.editColonne)).getText().toString();
                Serialize( true );
                setResult( RESULT_OK );
                finish();
            }
        });
    }

    public void Serialize( boolean store )
    /*
     *	Lettura/scrittura dati colonne
     */
    {
        mDataFile = getIntent().getStringExtra( "dataFile" );
        if( store )
        {
            BufferedWriter	buf = null;
            try
            {
                buf = new BufferedWriter(new FileWriter(mDataFile));
                buf.write( mColonne );
                buf.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            String			riga = null;
            BufferedReader	buf = null;

            try
            {
                mColonne = "";
                buf = new BufferedReader(new FileReader(mDataFile));
                while( (riga=buf.readLine()) != null )
                {
                    if( mColonne.length() > 0 )
                        mColonne += '\n';
                    mColonne += riga;
                }
                buf.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
