package com.example.actividad3m08;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdaptadorRecyclerView extends RecyclerView.Adapter<AdaptadorRecyclerView.CancionViewHolder> {

    private Class_Cancion[] canciones;
    private Context c;
    private OnItemClickListener listener;

    public AdaptadorRecyclerView(Class_Cancion[] canciones, Context c, OnItemClickListener onItemClickListener) {
        this.c = c;
        this.canciones = canciones;
        this.listener = onItemClickListener; // Asigna el listener
    }

    // Interfaz para manejar los clics
    public interface OnItemClickListener {
        void onItemClick(Class_Cancion cancion);
    }
    @NonNull
    @Override
    public CancionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item view using the context
        View vistadeevento = LayoutInflater.from(c).inflate(R.layout.elemento, parent, false);
        return new CancionViewHolder(vistadeevento);


    }

    @Override
    public void onBindViewHolder(@NonNull CancionViewHolder holder, int position) {
        Class_Cancion cancionActual = canciones[position];

        // Configurar los datos del elemento
        holder.icono.setImageDrawable(cancionActual.getFoto());
        holder.cancion.setText(cancionActual.getNombre());
        holder.autor.setText(cancionActual.getAutor());
        holder.album.setText(cancionActual.getAlbum());


        // Cambiar el color de fondo según si la posición es par o impar
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.DKGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        // Configurar el clic en el elemento actual
        holder.bind(cancionActual, listener); // Usa el listener inicializado
    }

    @Override
    public int getItemCount() {
        return canciones.length;
    }

    // ViewHolder class for the RecyclerView items
    public static class CancionViewHolder extends RecyclerView.ViewHolder {
        ImageView icono;
        TextView cancion, autor, album;

        public CancionViewHolder(@NonNull View itemView) {
            super(itemView);
            icono = itemView.findViewById(R.id.iv_fotoalbum);
            cancion = itemView.findViewById(R.id.tv_cancion);
            autor = itemView.findViewById(R.id.tv_autor);
            album = itemView.findViewById(R.id.tv_album);

        }
        // Método para vincular los datos y configurar el clic
        public void bind(final Class_Cancion cancion, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(cancion);
                }
            });
        }
    }
}
