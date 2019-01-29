package com.pmdm.contentprovideremail;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements ListView.OnItemLongClickListener {

    private final String tag = "EMAIL:";
    ListView l;

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        TextView t = (TextView) view;
        String nombreContacto = t.getText().toString();

        String proyeccion[] = {ContactsContract.Contacts._ID};
        String filtro = ContactsContract.Contacts.DISPLAY_NAME + " = ?";
        String args_filtro[] = {nombreContacto};

        List<String> lista_contactos = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                proyeccion, filtro, args_filtro, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String identificador = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                enviarSMS(identificador);
            }
        }
        cur.close();
        return true;
    }

    //envia un SMS a los teléfonos de un contacto
    private void enviarSMS(String identificador) {
        ContentResolver cr = getContentResolver();
        SmsManager smsManager = SmsManager.getDefault();
        String mensaje = ((EditText) findViewById(R.id.txtSMS)).getText().toString();
        Cursor cursorTelefono = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,//Email
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{identificador}, null);
        while (cursorTelefono.moveToNext()) {
            String telefono = cursorTelefono.getString(
                    cursorTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            try {
                String email = telefono;/* Your email address here */
                String subject = "subject";/* Your subject here */
                String body = "cuerpo";/* Your body here */
                String chooserTitle = "chooserTitle";/* Your chooser title here */
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                //emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); //If you are using HTML in your body text

                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
                /*
                smsManager.sendTextMessage(telefono, null, mensaje, null, null);
                Log.d(tag, "SMS enviado.");
                */
            } catch (Exception e) {
                Log.d(tag, "No se pudo enviar el SMS.");
                e.printStackTrace();
            }

        }
        cursorTelefono.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        l = (ListView) findViewById(R.id.lstContactos);
        l.setOnItemLongClickListener(this);
    }

    public void buscar(View v) {
        EditText txtNombre = (EditText) findViewById(R.id.txtContacto);

        String proyeccion[] = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME};
        String filtro = ContactsContract.Contacts.DISPLAY_NAME + " like ?";
        String args_filtro[] = {"%" + txtNombre.getText().toString() + "%"};

        List<String> lista_contactos = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Email.CONTENT_URI,
                proyeccion, filtro, args_filtro, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    lista_contactos.add(name);
                }
            }
        }
        cur.close();

        ListView l = (ListView) findViewById(R.id.lstContactos);
        l.setAdapter(new ArrayAdapter<String>(this, R.layout.fila_lista, lista_contactos));
    }
}
