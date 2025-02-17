package com.example.actividad3m08;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Class_Cancion {
    private int id;
    private String nombre;
    private String autor;
    private String album;
    private Drawable foto;
    private String nombreArchivo ;
    //private Bitmap foto;

    public Class_Cancion(int id, String nombre, String autor, String album, Drawable foto, String nombrearchivo) {
        this.id = id;
        this.nombre = nombre;
        this.autor = autor;
        this.album = album;
        this.foto = foto;
        this.nombreArchivo = nombrearchivo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAutor() {
        return autor;
    }

    public String getAlbum() {
        return album;
    }

    public Drawable getFoto() {
        return foto;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

}
