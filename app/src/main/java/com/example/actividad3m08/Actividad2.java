package com.example.actividad3m08;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class Actividad2 extends AppCompatActivity {

    //Objetos de texto e imagen
    TextView tvDetalle, tvautordetalle, tvalbumdetalle;
    ImageView caratula;

    //Botones
    ImageButton play, stop, next, prev, tenmore, tenless, regreso;
    ProgressBar barraProgreso;

    //Tratamiento audio
    MediaPlayer audio;
    Thread progressThread;
    //Identificador Id
    int cancionId;
    //Array de objetos Class_Cancion
    Class_Cancion [] canciones;

    @SuppressLint("MissingInflatedId")  //Contemplo que no tenga en cuenta elementos que no aparezcan en los layaut horizontal y vertical
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actividad2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Relaciono las variables con sus elementos del layout
        play = findViewById(R.id.ib_play);
        stop = findViewById(R.id.ib_stop);
        next = findViewById(R.id.ib_next);
        prev = findViewById(R.id.ib_prev);
        regreso = findViewById(R.id.ib_regreso);
        tenmore = findViewById(R.id.ib_tenmore);
        tenless = findViewById(R.id.ib_tenless);
        barraProgreso = (ProgressBar) findViewById(R.id.pb_barraProgreso);

        //Inicializo un nuevo mediaobserver para tener en cuenta la barra de progreso
        MediaObserver mediaObserver = new MediaObserver();

        // Obtengo los datos de la actividad anterior
        cancionId = getIntent().getIntExtra("cancionId", -1);
        String cancionArchivo = getIntent().getStringExtra("cancionArchivo");

        //Cargo el Array de claseCanciones
        canciones = cargarArchivosDesdeAssets();

        // Muestro los detalles de la cancion que se está escuchando
        tvDetalle = findViewById(R.id.tv_prueba);
        tvautordetalle = findViewById(R.id.tv_autordetalle);
        caratula = findViewById(R.id.iv_carátula);
        tvalbumdetalle = findViewById(R.id.tv_albumdetalle);

        //Cargo el recurso de audio según su nombre
        cargarRecursoAudioDesdeAssets(cancionArchivo);
        //cargarRecursoAudio(cancionNombre);

        //Inicio el mediaplayer y el hilo de la barra de progreso
        audio.start();
        progressThread = new Thread(mediaObserver);
        if (!progressThread.isAlive()) {
            progressThread.start();  // Iniciar el hilo de actualización
        }


        //Conjunto de OnclickListeners de los botones
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreArchivo;
                    //Si el audio está sonando detengo la cancion y la regreso al inicio y detengo la barra de sonido
                if (audio.isPlaying()) {
                    detenerActualizacionProgreso();
                    audio.stop();
                    audio.reset();
                }

                    // Si el el siguiente id al de esta canción no supera la longitud del listado de canciones le sumo uno, si no lo igualo a 0
                if (cancionId + 1 < canciones.length) {
                    cancionId++;
                } else {
                    cancionId = 0; // Reiniciar al principio si es la última canción
                }

                nombreArchivo = canciones[cancionId].getNombreArchivo();    //Obtendo el nombre con el nuevo id

                // Cargo el nuevo archivo en el mediaplayer, reseteo la barra de progreso e inicio cancion y barra de progreso
                cargarRecursoAudioDesdeAssets(nombreArchivo);
                barraProgreso.setProgress(0); // Reinicio barra de progreso
                audio.start();
                iniciarActualizacionProgreso(); // Inicio actualización de progreso
                play.setImageResource(R.drawable.play); //
            }

        });

        //Comportamiento similar al anterior
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreArchivo;

                if (audio.isPlaying()) {
                    detenerActualizacionProgreso(); // Detenemos la actualización
                    audio.stop();
                    audio.reset();
                }

                if (cancionId - 1 >= 0) {
                    cancionId--;
                } else {
                    cancionId = canciones.length - 1; // Ir a la última canción si estamos en la primera
                }
                nombreArchivo = canciones[cancionId].getNombreArchivo();

                cargarRecursoAudioDesdeAssets(nombreArchivo);
                barraProgreso.setProgress(0); // Reiniciar barra de progreso
                audio.start();
                iniciarActualizacionProgreso(); // Iniciar actualización de progreso
                play.setImageResource(R.drawable.play);
            }

        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audio.isPlaying()) {
                    // Si el audio está reproduciéndose, pausarlo
                    audio.pause();
                    play.setImageResource(R.drawable.pause); // Cambiar icono a pausa
                    detenerActualizacionProgreso(); // Detener la actualización de la barra
                } else {
                    // Si no está reproduciendo
                    if (barraProgreso.getProgress() > 0) {
                        // Si está pausado (la barra no está en 0)
                        audio.start(); // Reanudo reproducción
                    } else {
                        // Si no ha iniciado aún (la barra está en 0)
                        cargarRecursoAudioDesdeAssets(canciones[cancionId].getNombreArchivo());
                        audio.start(); // Comienzo reproducción desde el principio
                        barraProgreso.setProgress(0); // Aseguro que la barra está en 0
                    }
                    iniciarActualizacionProgreso(); // Inicio la actualización de progreso
                    play.setImageResource(R.drawable.play); // Cambio icono a play
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (audio.isPlaying()) {
                    detenerActualizacionProgreso(); // Detengo la actualización
                    audio.stop();
                    audio.reset();
                }

                barraProgreso.setProgress(0); // Reinicio barra de progreso
                cargarRecursoAudioDesdeAssets(canciones[cancionId].getNombreArchivo()); // Recargo el audio
                play.setImageResource(R.drawable.play); // Cambio a icono de reproducción
            }

        });

        tenmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = audio.getCurrentPosition();
                int duration = audio.getDuration();
                int newPosition = currentPosition + 10000; // Avanzo 10 segundos

                if (newPosition > duration) {
                    newPosition = duration; // No permito que se pase del final
                }

                audio.seekTo(newPosition); // Adelantola canción
            }
        });

        //Misma lógica que método anterior
        tenless.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = audio.getCurrentPosition();
                int newPosition = currentPosition - 10000;

                if (newPosition < 0) {
                    newPosition = 0;
                }

                audio.seekTo(newPosition);
            }
        });

        regreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                liberarRecursos();

                Intent intento = new Intent(Actividad2.this, MainActivity.class);
                startActivity(intento);
            }
        });

    }

    /**
     * Metodo que retorna un array de objetos cancion con los distintos campos rellenos
     * @return
     */
    private Class_Cancion[] cargarArchivosDesdeAssets() {
        AssetManager assetManager = getAssets();
        try {
            // Listo archivos en la carpeta "music"
            String[] archivos = assetManager.list("music");
            //Si la carpeta no está vacía compruebo los archivos, si no muestro un toast avisando al respecto
            if (archivos != null && archivos.length > 0) {
                // Cuento archivos que terminen en .mp3
                int cantidadMp3 = 0;
                for (String archivo : archivos) {
                    if (archivo.endsWith(".mp3")) {
                        cantidadMp3++;
                    }
                }

                //Aviso si no se encontraron archivos .mp3
                if (cantidadMp3 == 0) {
                    Toast.makeText(this, "No se encontraron archivos .mp3 en la carpeta assets/music", Toast.LENGTH_LONG).show();
                    return null;
                }

                // Inicializo el array con la cantidad de archivos .mp3
                canciones = new Class_Cancion[cantidadMp3];

                //cargo la imagen que mostraré si no hay carátula en los metadatos
                Bitmap imgalbum = BitmapFactory.decodeResource(this.getResources(), R.drawable.foto);
                Drawable imgalbum2 = ContextCompat.getDrawable(this, R.drawable.foto);

                for (int i = 0; i < archivos.length; i++) {
                    String nombreArchivo = archivos[i];
                    if (nombreArchivo.endsWith((".mp3"))) { // Filtrar solo archivos .mp3
                        MediaMetadataRetriever m_metaRetriever = new MediaMetadataRetriever();
                        try {
                            // Abro archivo desde assets y cargar los datos
                            InputStream inputStream = assetManager.open("music/" + nombreArchivo);
                            File tempFile = File.createTempFile("temp_audio", ".mp3", getCacheDir());
                            FileOutputStream fos = new FileOutputStream(tempFile);

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, length);
                            }

                            inputStream.close();
                            fos.close();

                            // Uso el archivo temporal para obtener metadatos
                            m_metaRetriever.setDataSource(tempFile.getAbsolutePath());

                            String nombreSinExtension = nombreArchivo.substring(0, nombreArchivo.lastIndexOf("."));
                            String cancion = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            String album = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            String autor = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                            byte[] artBytes = m_metaRetriever.getEmbeddedPicture();
                            Bitmap bitmap = (artBytes != null) ? BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length) : imgalbum;
                            Drawable drawable = (bitmap != null) ? new BitmapDrawable(getResources(), bitmap) : imgalbum2;

                            // Creo objeto de la canción
                            canciones[i] = new Class_Cancion(i,
                                    (cancion != null ? cancion : nombreSinExtension),
                                    (autor != null ? autor : "Autor desconocido"),
                                    (album != null ? album : "Álbum desconocido"),
                                    drawable, nombreArchivo);
                            tempFile.delete();

                        } finally {
                            try {
                                m_metaRetriever.release();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(this, "La carpeta assets/music está vacía", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar archivos desde assets/music", Toast.LENGTH_LONG).show();
        }
        return canciones;   //Retorno el array de objetos canciones
    }

    /**
     * Metodo que utilizando el nombre del archivo correspondiente utiliza la clase media player
     * preparando el audio para que empiece a sonar una vez que se invoque al .start
     * @param nombreArchivo
     */
    public void cargarRecursoAudioDesdeAssets(String nombreArchivo) {
        try {
            //Compruebo si el nombre termina en .mp3 si no se lo añado
            if (!nombreArchivo.endsWith(".mp3")) {
                nombreArchivo += ".mp3";
            }

            AssetFileDescriptor afd = getAssets().openFd("music/" + nombreArchivo);

            if (audio != null) {
                audio.release();
            }
            //Creo el nuevo mediaplayer y lo preparo
            audio = new MediaPlayer();
            audio.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            audio.prepare();
            afd.close();

            //Cambio los datos que se mostraran en la pantalla relacionados con el título, autor y carátula
            tvDetalle.setText(canciones[cancionId].getNombre());
            tvautordetalle.setText(canciones[cancionId].getAutor());
            caratula.setImageDrawable(canciones[cancionId].getFoto());
            tvalbumdetalle.setText(canciones[cancionId].getAlbum());

            // Configuro listener para que al final de la canción pase a la siguiente
            audio.setOnCompletionListener(mp -> {
                if (cancionId + 1 < canciones.length) {
                    cancionId++;
                } else {
                    cancionId = 0;
                }

                //Obtengo el nobmre de dicho archivo y lo cargo desde assets y lo lanzo
                String siguienteArchivo = canciones[cancionId].getNombreArchivo();
                cargarRecursoAudioDesdeAssets(siguienteArchivo);
                barraProgreso.setProgress(0);
                audio.start();
                iniciarActualizacionProgreso();
                play.setImageResource(R.drawable.play);
            });

        } catch (Exception e) {
            //Si no se encuentra el archivo
            //Toast.makeText(this, "Recurso no encontrado: " + nombreArchivo, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //

    /**
     * Clase interna que implementa Runnable para poder llevar el proceso de la barra mediante un aparte
     */
    private class MediaObserver implements Runnable {
        private boolean isRunning = true;

        public void stop() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning && audio != null) {
                if (audio.isPlaying()) {
                    // Actualio la barra de progreso en el hilo principal
                    runOnUiThread(() -> {
                        if (audio != null) {
                            barraProgreso.setProgress((int) ((double) audio.getCurrentPosition() / (double) audio.getDuration() * 100));

                            // Si la canción termina
                            if (audio.getCurrentPosition() >= audio.getDuration()) {
                                barraProgreso.setProgress(0); // Resetear barra de progreso
                                audio.stop(); // Detener el audio
                                audio.reset(); // Reiniciar el estado del reproductor
                                play.setImageResource(R.drawable.play); // Cambiar el ícono a "play"
                                detenerActualizacionProgreso(); // Detener el hilo
                            }
                        }
                    });
                }

                try {
                    Thread.sleep(200); // Pausar el hilo 200ms antes de la próxima actualización
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isRunning = false; // Detener el hilo en caso de interrupción
                }
            }
        }

    }

    /**
     * Metodo que actualiza y reinicia la barra de progreso
     */
    private void iniciarActualizacionProgreso() {
        detenerActualizacionProgreso(); // Detengo cualquier hilo previo
        progressThread = new Thread(new MediaObserver());
        progressThread.start();
    }

    /**
     * Método que detiene la actualización del progreso
     */
    private void detenerActualizacionProgreso() {
        if (progressThread != null && progressThread.isAlive()) {
            /*if (progressThread instanceof MediaObserver) {
                ((MediaObserver) progressThread).stop();
            }*/
            progressThread.interrupt(); // Interrumpo el hilo
            progressThread = null;
        }
    }

    /**
     * Método que libera los recursos del mediaplayer y del mediaobserver
     */
    private void liberarRecursos() {
        // Libero MediaPlayer
        if (audio != null) {
            if (audio.isPlaying()) {
                audio.stop();
            }
            audio.release();
            audio = null;
        }

        // Detengo el hilo de progreso
        if (progressThread != null && progressThread.isAlive()) {
            progressThread.interrupt();
            progressThread = null;
        }

        // Reinicio barra de progreso
        if (barraProgreso != null) {
            barraProgreso.setProgress(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        liberarRecursos();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Guardar el estado del MediaPlayer
        if (audio != null) {
            outState.putInt("posicionActual", audio.getCurrentPosition());
            outState.putInt("cancionId", cancionId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restaurar el estado del MediaPlayer
        if (savedInstanceState != null) {
            boolean isPlaying = savedInstanceState.getBoolean("isPlaying");
            int currentPosition = savedInstanceState.getInt("currentPosition");
            cancionId = savedInstanceState.getInt("cancionId");

            cargarRecursoAudioDesdeAssets(canciones[cancionId].getNombreArchivo());
            audio.seekTo(currentPosition);
            if (isPlaying) {
                audio.start();
            }
        }
    }


}