package com.pmdm.contentprovideremail;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListView.OnItemLongClickListener{

    ListView l;
    private final String tag="SMS:";


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        TextView t=(TextView)view;
        String nombreContacto=t.getText().toString();

        String proyeccion[]={ContactsContract.Contacts._ID};
        String filtro=ContactsContract.Contacts.DISPLAY_NAME + " = ?";
        String args_filtro[]={nombreContacto};

        List<String> lista_contactos=new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                proyeccion, filtro, args_filtro, null);
        if (cur.getCount() > 0) {
            int i = 0;
            String[] identificadores = new String[cur.getCount()];
            while (cur.moveToNext()) {
                String identificador = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                identificadores[i++] = identificador;
            }
            enviarEmail(identificadores);
        }
        cur.close();
        return true;
    }

    //envia un Email a los contactos
    private void enviarEmail(String[] identificadores){
        ContentResolver cr = getContentResolver();
        String mensaje=((EditText)findViewById(R.id.txtSMS)).getText().toString();
        String [] para = new String[identificadores.length];
        for(int i = 0; i<identificadores.length; i++){
            Cursor cursorTelefono = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID +" = ?",
                    new String[]{identificadores[i]}, null);
            while (cursorTelefono.moveToNext()) {
                String email = cursorTelefono.getString(
                        cursorTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                para[i] = email;
            }
            cursorTelefono.close();
        }
        //envío de email mediante intent implícito
        Intent i=new Intent();
        Intent chooser=null;
        i.setAction(Intent.ACTION_SEND);
        i.setData(Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_EMAIL,para);
        i.putExtra(Intent.EXTRA_SUBJECT,"Email de prueba");
        i.putExtra(Intent.EXTRA_TEXT,mensaje);
        i.setType("message/rfc822");
        chooser=i.createChooser(i,"Enviar email");
        startActivity(chooser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        l=(ListView)findViewById(R.id.lstContactos);
        l.setOnItemLongClickListener(this);
    }

    public void buscar(View v){
        EditText txtNombre=(EditText)findViewById(R.id.txtContacto);

        String proyeccion[]={ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.DATA};
        String filtro=ContactsContract.Contacts.DISPLAY_NAME + " like ?";
        String args_filtro[]={"%"+txtNombre.getText().toString()+"%"};

        List<String> lista_contactos=new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query( ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                proyeccion, filtro, args_filtro, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                   lista_contactos.add(name);
            }
        }
        cur.close();

        ListView l=(ListView)findViewById(R.id.lstContactos);
        l.setAdapter(new ArrayAdapter<String>(this,R.layout.fila_lista,lista_contactos));
    }
}
