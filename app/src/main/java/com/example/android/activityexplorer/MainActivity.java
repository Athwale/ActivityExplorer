package com.example.android.activityexplorer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private final Adapter adapter = new Adapter();
    private static final int REQUEST_WRITE_STORAGE = 365;
    private final File report_file = new File(Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS), "activity_report.txt");
    private final File useful_file = new File(Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS), "useful_activities.txt");
    private final File tried_file = new File(Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS), "used_activities.txt");

    private List<ActivityInfo> getAllActivities(Context context) {
        List<ActivityInfo> result = new ArrayList<>();

        List<ApplicationInfo> packages = context.getPackageManager()
                .getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            Log.d("APPINFO", "getAllRunningActivities: " + applicationInfo.packageName);
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(
                        applicationInfo.packageName, PackageManager.GET_ACTIVITIES);
                Collections.addAll(result, pi.activities);
            } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                Log.d("APPINFO", e.toString());
            }
        }
        return result;
    }

    private void markItem(Model model) {
        model.setUseful(true);
        adapter.notifyDataSetChanged();
        String record = model.packageName + ":" + model.label + "\n";
        try (FileOutputStream stream = new FileOutputStream(this.useful_file, true)) {
            stream.write(record.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(this, model.packageName + " marked", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
        try {
            this.markItem(adapter.getItem(index));
        } catch (Exception e) {
            Toast.makeText(this, adapter.getItem(index).packageName, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
        try {
            Model model = adapter.getItem(index);
            String name = model.packageName;
            String activity = model.label;
            model.setViewed(true);
            adapter.notifyDataSetChanged();

            Intent intent = new Intent();
            intent.setClassName(name, activity);

            String record = name + ":" + activity + "\n";
            try (FileOutputStream stream = new FileOutputStream(this.tried_file, true)) {
                stream.write(record.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.d("APPINFO", e.toString());
        }
    }

    private void populate() {
        // todo load from file if exists.
        List<ActivityInfo> result = this.getAllActivities(this);
        ArrayList<Model> models = new ArrayList<>();
        long id = 0;

        for (ActivityInfo a : result) {
            models.add(new Model(++id, a.name, a.packageName));
            String record = a.packageName + ":" + a.name + "\n";
            try (FileOutputStream stream = new FileOutputStream(this.report_file)) {
                stream.write(record.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Log.d("APPINFO", a.toString());
        }
        adapter.update(models);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
            this.populate();
        } else {
            Toast.makeText(this, "File access permission missing",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected void onStart() {
        super.onStart();
        boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    },
                    REQUEST_WRITE_STORAGE);
        } else {
            this.populate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}