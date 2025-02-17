package com.example.actividad3m08;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

        //Caso recicleview
    private RecyclerView lista2;
    private ImageView fotito;
        //caso listView
    private ListView lista;

        //Array de canciones
    Class_Cancion [] canciones;
    //ArrayList<Class_Cancion> canciones1 = new ArrayList<>();

        //Array de ficheros
    //File [] ficheros;

        //Variables para permisos
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        fotito = findViewById(R.id.iv_fotito);
            //Caso de recycleview
        lista2 = findViewById(R.id.rv_listaReciclerView);
        lista2.setLayoutManager(new LinearLayoutManager(this));

            //Caso de ListView
        lista = findViewById(R.id.LV_listaListView);

        //Chequear permisos
        //chequearPermisos();

        //Opciones de cargar archivos
        cargarArchivosDesdeAssets();
        //cargarArchivos();
        //cargarArchivosDesdeRaw();
        //cargarArchivos2();

            //En el caso de usar el ListView hago el Onclick aquí
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Obtener la canción seleccionada del array de canciones
                Class_Cancion cancionSeleccionada = canciones[position];

                // Crear un Intent para abrir la nueva actividad
                Intent intent = new Intent(MainActivity.this, Actividad2.class);

                // Pasar el id (u otros datos si es necesario) como extra
                intent.putExtra("cancionId", cancionSeleccionada.getId());
                intent.putExtra("cancionArchivo", cancionSeleccionada.getNombreArchivo());

                // Iniciar la nueva actividad
                startActivity(intent);
            }
        });

    }

    /**
     * Metodo para cargar archivos desde assets
     * Recopilo en un array de String los archivos dentro de la carpeta assets/music y obtengo los
     * metadatos de cada archivo mp3 en el fichero. Creo un objeto de cada canción con dichos
     * metadatos
     */
    private void cargarArchivosDesdeAssets() {
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
                    return;
                }

                // Inicializo el array con la cantidad de archivos .mp3
                canciones = new Class_Cancion[cantidadMp3];

                //cargo la imagen que mostraré si no hay carátula en los metadatos
                Bitmap imgalbum = BitmapFactory.decodeResource(this.getResources(), R.drawable.foto);
                Drawable imgalbum2 = ContextCompat.getDrawable(this, R.drawable.foto);

                int index = 0; // Índice para llenar el array canciones
                for (String nombreArchivo : archivos) {

                    // Solo proceso archivos .mp3
                    if (nombreArchivo.endsWith(".mp3")) {
                        MediaMetadataRetriever m_metaRetriever = new MediaMetadataRetriever();
                        try {
                            // Abro archivo desde assets y cargo los datos
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

                            //Obtengo los distintos metadatos si existen y creo una variable con el nombre sinla extensión
                            String nombreSinExtension = nombreArchivo.substring(0, nombreArchivo.lastIndexOf("."));
                            String cancion = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            String album = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            String autor = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                            //Trato de obener la imagen d ela carátula si existe en los metadatos y si es así la convierto a drawable
                            //En caso de no existir pongo la imagen2 por defecto
                            byte[] artBytes = m_metaRetriever.getEmbeddedPicture();
                            Bitmap bitmap = (artBytes != null) ? BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length) : imgalbum;
                            Drawable drawable = (bitmap != null) ? new BitmapDrawable(getResources(), bitmap) : imgalbum2;

                            // Creo objeto de la canción y lo agrego al array contemplando parametros por defecto si no tiene metadatos
                            canciones[index] = new Class_Cancion(index,
                                    (cancion != null ? cancion : nombreSinExtension),
                                    (autor != null ? autor : "Autor desconocido"),
                                    (album != null ? album : "Álbum desconocido"),
                                    drawable,
                                    nombreArchivo);

                            index++; // Incremento el índice

                            // Elimino el archivo temporal
                            tempFile.delete();

                        } finally {
                            try {
                                m_metaRetriever.release();  //libero memoria
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Configuro el RecyclerView con el método implementado oncliclistener
                AdaptadorRecyclerView adaptador2 = new AdaptadorRecyclerView(canciones, this, new AdaptadorRecyclerView.OnItemClickListener() {
                    @Override
                    public void onItemClick(Class_Cancion cancion) {
                        // Manejo el clic: abrir la nueva actividad y pasar id y nombre del archivo con extensión
                        Intent intent = new Intent(MainActivity.this, Actividad2.class);
                        intent.putExtra("cancionId", cancion.getId());
                        intent.putExtra("cancionArchivo", cancion.getNombreArchivo());
                        startActivity(intent);
                    }
                });

                lista2.setAdapter(adaptador2);

                    // Configuro el ListView (en caso de querer usar listview)
                Adaptador adaptador = new Adaptador(canciones, this);
                // lista.setAdapter(adaptador);

            } else {
                Toast.makeText(this, "La carpeta assets/music está vacía", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar archivos desde assets/music", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "Permisos concedidos en la solicitud");
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
                //cargarArchivosDesdeRaw();
                //cargarArchivos();
                cargarArchivosDesdeAssets();
            } else {
                Log.d("TAG", "Permisos denegados en la solicitud");
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show();

                //comprobar si anteriormente se rechazaron los permisos
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    new AlertDialog.Builder(this).setMessage("Necesitas habilitar los permisos para usar la aplicación")
                            .setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);

                                }
                            })
                            .setNegativeButton("NO GRACIAS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //NO pongo nada
                                    Log.d("TAG", "Salir");
                                }
                            }).show();


                }else {
                    Toast.makeText(this, "Necesitas habilitar los permisos de manera manual", Toast.LENGTH_SHORT).show();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }



    private void chequearPermisos(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            //cargarArchivos();
            //cargarArchivosDesdeRaw();
            //cargarArchivos2();
            cargarArchivosDesdeAssets();
        }else{      //si es mayor a la api 23
            Log.i("TAG",  "API >= 23");
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                Log.i("TAG",  "Permisos granted");
                //cargarArchivos();
                //cargarArchivosDesdeRaw();
                //cargarArchivos2();
                cargarArchivosDesdeAssets();
            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){ //verdadero
                    Log.i("TAG",  "El usuario previamente rechazó permisos");
                }else{      //si es falso
                    Log.i("TAG",  "Vamos a la solicitud de permisos");
                }
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            // Si los permisos ya están concedidos
            //cargarArchivos();
            //cargarArchivosDesdeRaw();
            //cargarArchivos2();
            cargarArchivosDesdeAssets();
        }
    }


    //Los 3 siguientes métodos fueron pruebas que hice con raw y descargas hasta que encontré la solución

    //Cargar archivos desde raw
   /* private void cargarArchivosDesdeRaw() {
        // Obtener todos los campos de la clase R.raw mediante reflexión
        Field[] campos = R.raw.class.getFields();
        canciones = new Class_Cancion[campos.length];
        Bitmap imgalbum = BitmapFactory.decodeResource(this.getResources(), R.drawable.foto);
        Drawable  imgalbum2 = ContextCompat.getDrawable(this, R.drawable.foto);

        for (int i = 0; i < campos.length; i++) {
            try {
                int idRecurso = campos[i].getInt(null);
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + idRecurso);
                MediaMetadataRetriever m_metaRetriever = new MediaMetadataRetriever();

                try {
                    m_metaRetriever.setDataSource(this, uri);
                    //String cancion = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String cancion = campos[i].getName();
                    String album = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    String autor = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);


                    byte[] artBytes = m_metaRetriever.getEmbeddedPicture();
                    Bitmap bitmap = (artBytes != null) ? BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length) : imgalbum;
                    Drawable drawable = (bitmap != null) ? new BitmapDrawable(getResources(), bitmap) : imgalbum2;
                    //Si quiero dejar de nuevo bitmap tengo que cambiar la clase cancion y en el campo de la goto escribir bitmap

                    canciones[i] = new Class_Cancion(i,
                            (cancion != null ? cancion : "Título desconocido"),
                            (autor != null ? autor : "Autor desconocido"),
                            (album != null ? album : "Álbum desconocido"),
                            //bitmap,
                            drawable, campos[i].getName());
                } finally {
                    m_metaRetriever.release();
                }

            } catch (IllegalAccessException | IOException e) {
                e.printStackTrace();
            }


                //Esto sería con Recycleview
                //String nombreArchivo = getResources().getResourceEntryName(idRecurso);

                // Crear una instancia de Class_Cancion
                //canciones[i] = new Class_Cancion(i, nombreArchivo, nombreArchivo, "Album", imgalbum);


        }
        //Caso recycleview
        //AdaptadorRecyclerView adaptador = new AdaptadorRecyclerView(canciones, this);

        //caso ListView
        Adaptador adaptador = new Adaptador(canciones,this);
        lista.setAdapter(adaptador);
    }*/


    // Cargar archivos  desde descargas
    /*private void cargarArchivos2(){
        //File directorioDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File directorioDescargas = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        File[] archivos = directorioDescargas.listFiles();
        canciones = new Class_Cancion[archivos.length];
        if(archivos.length > 0){
            Bitmap imgalbum = BitmapFactory.decodeResource(this.getResources(), R.drawable.foto);
            Drawable  imgalbum2 = ContextCompat.getDrawable(this, R.drawable.foto);

            for (int i = 0; i < archivos.length; i++) {
                File archivo = archivos[i];
                if (archivo.isFile() && archivo.getName().endsWith(".mp3")) { // Puedes ajustar el filtro según tus necesidades
                    Uri uri = Uri.fromFile(archivo);
                    MediaMetadataRetriever m_metaRetriever = new MediaMetadataRetriever();

                    try {
                        m_metaRetriever.setDataSource(archivo.getAbsolutePath());

                        String cancion = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String album = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        String autor = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);

                        byte[] artBytes = m_metaRetriever.getEmbeddedPicture();
                        Bitmap bitmap = (artBytes != null) ? BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length) : imgalbum;
                        Drawable drawable = (bitmap != null) ? new BitmapDrawable(getResources(), bitmap) : imgalbum2;
                        //Si quiero dejar de nuevo bitmap tengo que cambiar la clase cancion y en el campo de la goto escribir bitmap


                        canciones[i] = new Class_Cancion(i,
                                (cancion != null ? cancion : "Título desconocido"),
                                (autor != null ? autor : "Autor desconocido"),
                                (album != null ? album : "Álbum desconocido"),
                                //bitmap,
                                drawable);
                    } finally {
                        try {
                            m_metaRetriever.release();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            Adaptador adaptador = new Adaptador(canciones, this);
            lista.setAdapter(adaptador);
        }else{
            Toast.makeText(this, "la carpeta está vacía", Toast.LENGTH_LONG).show();
        }

    }*/


    // Cargar archivos y configurar el RecyclerView
    /*private void cargarArchivos() {

        //File directorio = new File("/document/raw:/storage/emulated/0/Download");
        //File directorio = new File(Environment.getExternalStorageDirectory(), "Download");
        //File directorio = new File("/Users/eduardolucasmunozdelucas/AndroidStudioProjects/Actividad3M08/app/src/main/res/raw");
        //File directorio = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File directorio  = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        if (ficheros != null) {
            for (File fichero : ficheros) {
                Log.d("MainActivity", "Archivo encontrado: " + fichero.getName());
            }
        }else{
            Log.d("MainActivity", "Directorio no encontrado o no accesible.");
        }
        if (directorio != null && directorio.exists() && directorio.isDirectory()) {
            Log.d("MainActivity", "Directorio encontrado: " + directorio.getAbsolutePath());

            ficheros = directorio.listFiles();
            if (ficheros != null && ficheros.length > 0) {
                Log.d("MainActivity", "Número de archivos encontrados: " + ficheros.length);

                canciones = new Class_Cancion[ficheros.length];
                Bitmap imgalbum = BitmapFactory.decodeResource(this.getResources(), R.drawable.foto);
                Drawable  imgalbum2 = ContextCompat.getDrawable(this, R.drawable.foto);

                for (int i = 0; i < canciones.length; i++) {
                    //canciones[i] = new Class_Cancion(i, ficheros[i].getName(), ficheros[i].getName(), "Album", imgalbum);
                    canciones[i] = new Class_Cancion(i, ficheros[i].getName(), ficheros[i].getName(), "Album", imgalbum2);

                }


                //caso ListView
                Adaptador adaptador = new Adaptador(canciones,this);
                lista.setAdapter(adaptador);

                //AdaptadorRecyclerView adaptador = new AdaptadorRecyclerView(canciones, this);
                //lista.setAdapter(adaptador);
            } else {
                Toast.makeText(this, "CARPETA VACÍA", Toast.LENGTH_LONG).show();
                canciones = new Class_Cancion[0];
            }
        } else {
            Log.d("MainActivity", "Directorio no encontrado o no accesible.");
            Toast.makeText(this, "NO EXISTE EL DIRECTORIO O NO ACCESIBLE", Toast.LENGTH_LONG).show();
            canciones = new Class_Cancion[0];
        }
    }*/

}