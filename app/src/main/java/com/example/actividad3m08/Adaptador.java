package com.example.actividad3m08;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
public class Adaptador extends BaseAdapter {

    Class_Cancion [] canciones ;
    Context c;

    public Adaptador (Class_Cancion[] canciones, Context c){
        this.c = c;
        this.canciones = canciones;
    }

    @Override
    public int getCount() {
        return canciones.length;
    }

    @Override
    public Object getItem(int i) {
        return canciones[i];
    }

    @Override
    public long getItemId(int i) {
        return canciones[i].getId();
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflador= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vistadeevento = inflador.inflate(R.layout.elemento,viewGroup,false);

        ImageView icono = (ImageView) vistadeevento.findViewById(R.id.iv_fotoalbum);
        TextView cancion = (TextView) vistadeevento.findViewById(R.id.tv_cancion);
        TextView autor = (TextView) vistadeevento.findViewById(R.id.tv_autor);
        TextView album = (TextView) vistadeevento.findViewById(R.id.tv_album);

        if (i%2==0){
            vistadeevento.setBackgroundColor(Color.DKGRAY); }
        else {
            vistadeevento.setBackgroundColor(Color.LTGRAY); }

        //setImageBitmap(canciones[i].getFoto());
        icono.setImageDrawable(canciones[i].getFoto());
        cancion.setText(canciones[i].getNombre());
        autor.setText(canciones[i].getAutor());
        album.setText(canciones[i].getAlbum());

        return vistadeevento;

    }
}