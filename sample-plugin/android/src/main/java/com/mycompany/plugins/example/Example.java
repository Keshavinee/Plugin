package com.mycompany.plugins.example;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Example extends Worker {

    private static Context context;
    private int notificationCounter = ExamplePlugin.getCounterValue();
    private int id = notificationCounter * 100;
    private String channelId = "channel " + id;

    private int filecount = 0;

    public Example(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
        this.context = context;
    }

    @Override
    public Result doWork() {
        try {
            CompletableFuture<Void> downloadFuture = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                downloadFuture = new CompletableFuture<>();
                downloadFile(downloadFuture);
                downloadFuture.get(); // Wait for the download to complete
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    public void downloadFile(CompletableFuture<Void> downloadFuture) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://my-firebase-project-57a96.appspot.com/");

        // Create a reference to "file"
        StorageReference mountainsRef = storageRef.child("newfile.zip");
        File localFile;
        try {
            localFile = File.createTempFile("work", "zip");

            mountainsRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                // File downloaded successfully
                showNotification("File downloaded from Firebase");

                // Creating database object
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Extracting zip file
                try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(localFile))) {
                    ZipEntry zipEntry = zipInputStream.getNextEntry();

                    while (zipEntry != null) {
                        // Process the contents of the zip entry
                        String fileExtension = zipEntry.getName().substring(zipEntry.getName().lastIndexOf('.') + 1);
                        String contentType;

                        switch (fileExtension) {
                            case "txt":
                                contentType = "text/plain";
                                break;
                            case "html":
                                contentType = "text/html";
                                break;
                            case "jpg":
                            case "jpeg":
                                contentType = "image/jpeg";
                                break;
                            case "png":
                                contentType = "image/png";
                                break;
                            case "zip":
                                contentType = "application/zip";
                                break;
                            case "pptx":
                                contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                                break;
                            case "pdf":
                                contentType = "application/pdf";
                                break;
                            default:
                                contentType = "application/octet-stream";
                                break;
                        }

                        // Create a temporary file to store the contents of the entry
                        File tempFile = File.createTempFile("temp", "." + fileExtension);
                        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

                        // Read the contents of the entry and write it to the temporary file
                        byte[] buffer = new byte[(int) zipEntry.getSize()];
                        int count = zipInputStream.read(buffer);
                        while (count != -1) {
                            fileOutputStream.write(buffer, 0, count);
                            count = zipInputStream.read(buffer);
                        }

                        // Close the output stream and do something with the temporary file
                        fileOutputStream.close();
                        ContentValues values = new ContentValues();
                        values.put("file_name", tempFile.getName());
                        values.put("file_path", tempFile.getPath());
                        long rowId = db.insert("files_table", null, values);
                        if (rowId != -1L) {
                            // File inserted successfully
                            filecount++;
                        }
                        zipEntry = zipInputStream.getNextEntry();
                    }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    downloadFuture.complete(null); // Complete the future when download is done
                }
                // Get the file count from the database
                int insertedFileCount = getInsertedFileCount();

                Log.d("Example", "Number of files inserted: " + insertedFileCount);
                showNotification("File inserted into database");
            }).addOnFailureListener(exception -> {
                // Handle failure
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    downloadFuture.completeExceptionally(exception); // Complete the future with an exception
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                downloadFuture.completeExceptionally(e); // Complete the future with an exception
            }
        }
    }

    public static int getInsertedFileCount() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        return dbHelper.getFileCount();
    }

    private void showNotification(String string) {
        Intent intent = new Intent(getApplicationContext(), Example.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Workmanager : File " + notificationCounter)
                .setContentText(string)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Workmanager";
            String channelDescription = "Background processing using WorkManager";
            int channelImportance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(notificationCounter, notificationBuilder.build());
        id++;
    }
}

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_FILES_TABLE = "CREATE TABLE files_table (id INTEGER PRIMARY KEY AUTOINCREMENT, file_name TEXT, file_path TEXT)";
    private static final String SQL_DROP_FILES_TABLE = "DROP TABLE IF EXISTS files_table";

    private static DatabaseHelper instance;

    static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(SQL_CREATE_FILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade tables
        db.execSQL(SQL_DROP_FILES_TABLE);
        onCreate(db);
    }

    public int getFileCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM files_table", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }
}