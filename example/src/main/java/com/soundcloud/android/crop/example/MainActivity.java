package com.soundcloud.android.crop.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    public static final int REQUEST_PICK_CAMERA = 91620;

    private File imageFile;

    private ImageView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (ImageView) findViewById(R.id.result_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select) {
            resultView.setImageDrawable(null);
            Crop.pickImage(this);
            return true;
        } else if (item.getItemId() == R.id.action_select2) {
            resultView.setImageDrawable(null);
            requestPermission();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_CAMERA) {
                beginCrop(Uri.fromFile(imageFile));
            } else if (requestCode == Crop.REQUEST_PICK) {
                beginCrop(result.getData());
            } else if (requestCode == Crop.REQUEST_CROP) {
                handleCrop(resultCode, result);
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PICK_CAMERA);
        } else {
            cropFromCamera();
        }
    }

    private void cropFromCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = timeStamp + "_";
//        File fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File fileDir = getExternalFilesDir(this, Environment.DIRECTORY_PICTURES);
        try {
            imageFile = File.createTempFile(fileName, ".jpg", fileDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Uri uri = Uri.fromFile(imageFile);
        Uri uri = FileProvider.getUriForFile(this,
                FileProviderContract.AUTHORITY,
                imageFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_PICK_CAMERA);
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));

        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            resultView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static File getExternalFilesDir(Context context, String type) {
        // Use it instead of Context.getExternalFilesDir()
        // There is a bug on Huawei-Devices, which select the wrong external path
        // https://stackoverflow.com/questions/39895579/fileprovider-error-onhuawei-devices
        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, type);
        if (externalFilesDirs != null && externalFilesDirs.length > 0) {
            return externalFilesDirs[0];
        } else {
            return context.getExternalFilesDir(type);
        }
    }
}
