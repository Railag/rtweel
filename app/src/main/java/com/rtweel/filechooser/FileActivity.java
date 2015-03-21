package com.rtweel.filechooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.rtweel.R;
import com.rtweel.cache.App;
import com.rtweel.constant.Extras;

public class FileActivity extends ListActivity {

    private File mCurrentPath;

    private FileAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentPath = Environment.getExternalStorageDirectory();
        initialize(mCurrentPath);
    }

    private void initialize(File startPath) {
        File[] paths = startPath.listFiles();
        this.setTitle("Current Dir: " + startPath.getName());
        List<Line> pathList = new ArrayList<Line>();
        List<Line> fileList = new ArrayList<Line>();
        try {
            for (File path : paths) {
                if (path.isDirectory())
                    pathList.add(new Line(path.getName(), "Folder", path
                            .getAbsolutePath()));
                else {
                    String type = path.getAbsolutePath().substring(
                            path.getAbsolutePath().length() - 4);
                    Log.i("DEBUG", type);
                    if (".jpg".equals(type) || ".bmp".equals(type)
                            || ".gif".equals(type)) {
                        fileList.add(new Line(path.getName(), "File Size: "
                                + path.length(), path.getAbsolutePath()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(pathList);
        Collections.sort(fileList);
        pathList.addAll(fileList);
        if (!startPath.getName().contains("sdcard") && startPath.getParent() != null) {
            pathList.add(0,
                    new Line("..", "Parent Directory", startPath.getParent()));
        }
        mAdapter = new FileAdapter(FileActivity.this, R.layout.file, pathList);
        this.setListAdapter(mAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Line line = mAdapter.getItem(position);
        if (line.getData().equalsIgnoreCase("folder")
                || line.getData().equalsIgnoreCase("parent directory")) {
            mCurrentPath = new File(line.getPath());
            initialize(mCurrentPath);
        } else {
            onFileClick(line);
        }

    }

    private void onFileClick(Line line) {
        final String path = line.getPath();
        new Thread(new Runnable() {

            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory()
                        + App.PHOTO_PATH + ".jpg");
                FileOutputStream output = null;
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileInputStream input = null;
                try {
                    input = new FileInputStream(new File(path));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] buf = new byte[1024];
                int lenght;
                try {
                    while ((lenght = input.read(buf)) > 0) {
                        output.write(buf, 0, lenght);
                    }
                    input.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Intent data = new Intent();
        data.putExtra(Extras.FILE_URI, line.getPath());
        setResult(RESULT_OK, data);
        finish();
    }
}
