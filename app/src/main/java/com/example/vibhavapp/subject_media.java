package com.example.vibhavapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vibhavapp.data.MyDbHandler;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class subject_media extends AppCompatActivity {

    public static final int IMAGE_CAPTURE_REQUEST_CODE = 4;
    MyDbHandler db;
    String[] images;
    TabLayout tabLayout;
    ViewPager tabViewPager;
    ImageView mediaIv;
    String name;
    TextView mediaTv;
    Button showFile;
    FloatingActionsMenu fabImages;
    com.getbase.floatingactionbutton.FloatingActionButton fabCamera;
    com.getbase.floatingactionbutton.FloatingActionButton fabGallery;
    String currentPhotoPath;
    ConstraintLayout subMediaParentLayout;

//    PdfViewActivity pdfViewActivity;
//    Uri selectedFileUri;
    public static final int IMAGE_PICK_REQUEST_CODE = 3;
//    private Intent intent1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_media);
//        getSupportActionBar().hide();

        db = new MyDbHandler(subject_media.this);
        tabLayout = findViewById(R.id.tabLayout);
        tabViewPager = findViewById(R.id.tabViewPager);

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("subjectName");
        setTitle(name + "  -  Drawer");

//        images = db.getMedia(name);
//        Toast.makeText(this, "uri: "+images[0], Toast.LENGTH_LONG).show();

        setupTabViewPager(tabViewPager);
        tabLayout.setupWithViewPager(tabViewPager);

        fabImages = findViewById(R.id.fabImages);
        fabCamera = findViewById(R.id.fabCamera);
        fabGallery = findViewById(R.id.fabGallery);

        tabViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                fabImages.collapse();
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    fabImages.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    fabImages.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                fabImages.collapse();
            }

        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(v.getContext()).withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                dispatchTakePictureIntent();
                            }
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                if ( permissionDeniedResponse.isPermanentlyDenied()) {


                                    Snackbar sb = Snackbar.make(findViewById(R.id.subMediaParentLayout),"Requires Camera Permission to continue.",Snackbar.LENGTH_LONG)
                                            .setAction("SETTINGS", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                                    intent.setData(uri);
                                                    startActivity(intent);
                                                }
                                            });
                                    sb.show();
                                }
                            }
                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });


        fabGallery.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
//                dispatchChoosePictureIntent();
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(gallery, IMAGE_PICK_REQUEST_CODE);
            }
        });


//        fabMedia.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.Q)
//            @Override
//            public void onClick(final View v) {
//                Intent intent_fe = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                Intent intent_fe = new Intent(Intent.ACTION_PICK, Uri.parse(MediaStore.Files.FileColumns.DATA));
//                Intent intent_fe = new Intent(Intent.ACTION_PICK);
//                Intent intent_fe = new Intent(Intent.ACTION_GET_CONTENT);
//                intent_fe.setType("*/*");
//                startActivityForResult(intent_fe, REQUEST_CODE_SELECT_FILE);

//                    Toast.makeText(v.getContext(), "Select a ", Toast.LENGTH_LONG).show();
//                    Intent browseStorage = new Intent(Intent.ACTION_PICK);
////                    browseStorage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    browseStorage.setType("image/*");
////                    browseStorage.addCategory(Intent.CATEGORY_OPENABLE);
//                    startActivityForResult(Intent.createChooser(browseStorage, "Img"), REQUEST_CODE_SELECT_FILE);

//                Intent intent1 = new Intent(Intent.ACTION_PICK);
//                intent1.setType("image/*");
//                startActivityForResult(intent1, REQUEST_CODE_SELECT_FILE);

//                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
//                intent1.setAction(Intent.ACTION_GET_CONTENT);
//                intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent1.addCategory(Intent.CATEGORY_OPENABLE);
//                intent1.setType("*/*");
//                startActivityForResult(intent1, REQUEST_CODE_SELECT_FILE);
//                startActivityForResult(Intent.createChooser(intent1, "File"), REQUEST_CODE_SELECT_FILE);


//                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
//                intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent1.setType("application/pdf");
//                intent1.addCategory(Intent.CATEGORY_OPENABLE);
////                intent1.setType("*/*");
//                startActivityForResult(intent1, REQUEST_CODE_SELECT_FILE);


//                startActivityForResult(Intent.createChooser(intent1, "PDFs"), REQUEST_CODE_SELECT_FILE);

//                Dexter.withContext(v.getContext())
//                        .withPermission(Manifest.permission.INTERNET)
//                        .withListener(new PermissionListener() {
//                            @Override
//                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                                Toast.makeText(v.getContext(), "Internet Permission Granted", Toast.LENGTH_SHORT).show();
//                                Intent intent1 = new Intent(v.getContext(), ViewPdf.class);
//                                intent1.putExtra("subjectName", name);
//                                startActivity(intent1);
//                            }
//
//                            @Override
//                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
//                                Toast.makeText(v.getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//                                permissionToken.continuePermissionRequest();
//                            }
//                        }).check();


//                Intent intent1 = new Intent(Intent.ACTION_VIEW);




//            }
//        });

//        showFile.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onClick(View v) {
//                String uriText = db.getMedia(name);
//
//                if (uriText.equals(""))
//                    Toast.makeText(v.getContext(), "No File Added", Toast.LENGTH_SHORT).show();
//                else {
//                    showPdfFromUri(uriText);
//                    pdfViewActivity.showPdfFromUri(Uri.parse(uriText));
//                    mediaTv.setText(uriText);
//                    Intent intent1 = new Intent(v.getContext(), ViewPdf.class);
//                    intent1.putExtra("showFile", true);
//                    intent1.putExtra("uri", uriText);
//                    startActivity(intent1);
//                }
//            }
//        });

    }


    public void setupTabViewPager (ViewPager viewPager){
        tabViewPagerAdapter tabViewPagerAdapter = new tabViewPagerAdapter(getSupportFragmentManager());
        tabViewPagerAdapter.addFragment(new ImageFragment(name), "IMAGES");
        tabViewPagerAdapter.addFragment(new DocFragment(), "DOCUMENTS");
        viewPager.setAdapter(tabViewPagerAdapter);
    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    private void showPdfFromUri(Uri uri) {
    private void showPdfFromUri(String uri) {

//        pdfView.fromUri(uri).load();

//        PdfViewActivity pdf = findViewById(R.id.pdfView);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
//        File file = new File("file:///sdcard/Download/world.pdf");
        intent.setData(Uri.parse(uri));
//        intent.setDataAndType(Uri.parse(uri), "application/pdf");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setDataAndType(Uri.parse(uri), "application/pdf");
//        intent.setDataAndType(uri, "image/*");
//        startActivity(intent);
        startActivity(Intent.createChooser(intent, "Open File with.."));

//        mediaTv.setText(uri);

//        File file = new File(uri.getPath());

//        Intent intent = new Intent();
//        File file = new File(Environment.getDataDirectory().getAbsoluteFile(), "world.pdf");
//        intent.setAction(Intent.ACTION_VIEW);
//        File file = new File(uri);
//        intent.setData(Uri.fromFile(file));
//        intent.setDataAndType(uri, "application/pdf");
//        startActivity(intent);
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDF";
//        File file = new File(path, "world.pdf");

//        intent.setDataAndType(Uri.fromFile(file), );
//        startActivity(intent);



//        intent.setAction(Intent.ACTION_VIEW);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setDataAndType(uri, "application/pdf");
//        startActivity(intent);

//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//        intent.setDataAndType(Uri.parse("file:///" + uri), "application/pdf");
//        startActivity(intent);

//        File pdfFile = new File(Environment.getExternalStorageDirectory(),"\"/world.pdf\"");//File path
////        File pdfFile = new File(uri);//File path
//        if (pdfFile.exists()) //Checking if the file exists or not
//        {
//            Uri path = Uri.fromFile(pdfFile);
//            Intent objIntent = new Intent(Intent.ACTION_VIEW);
//            objIntent.setDataAndType(path, "application/pdf");
//            objIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(objIntent);//Starting the pdf viewer
//        } else {
//            Toast.makeText(subject_media.this, "File DNE", Toast.LENGTH_SHORT).show();
//        }

//        try {
//            File file = new File(Environment.getExternalStorageDirectory()
//                    + "/download/" + "world.pdf");
//            if (!file.isDirectory())
//                file.mkdir();
////            Intent pdfIntent = new Intent("com.adobe.reader");
//            Intent pdfIntent = new Intent();
//            pdfIntent.setType("application/pdf");
//            pdfIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            pdfIntent.setAction(Intent.ACTION_VIEW);
//            Uri uri1 = Uri.fromFile(file);
////            Uri uri1 = Uri.parse(file.getCanonicalPath());
//
//
//            pdfIntent.setDataAndType(uri1, "application/pdf");
//            startActivity(pdfIntent);
//        } catch (Exception e) {
////            e.printStackTrace();
//            Toast.makeText(subject_media.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
    }


//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
//            if (data != null) {
//            File file = new File(currentPhotoPath);
//            Uri uri = Uri.fromFile(file);
//            db.addMedia(name, uri.toString());

//            Uri uri = data.getData();
//            db.addMedia(name, uri.toString());
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i=0; i<clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    String path = getPath(this, uri);
                    File file = new File(path);
                    Uri uri1 = Uri.fromFile(file);
                    db.addMedia(name, uri1.toString());
                    Log.d("attman", "Uri: " + uri1.toString() + " prev: " + uri.toString());
                }
            } else {
                Uri uri = data.getData();
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(uri);
                String path = getPath(this, uri);
                String fileName = getFileName(uri);
                File file = new File(path);
                Uri uri1 = Uri.fromFile(file);
                db.addMedia(name, uri1.toString());

                Log.d("attman", "Path: " + path + " Name: " + fileName);
                Log.d("attman", "Uri: " + uri1.toString() + " prev: " + uri.toString());
            }
//            try {
//                saveFileInternal(path, fileName);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }





//                Bundle bundle = data.getExtras();
//                Uri uri = (Uri) bundle.get("data");
//                db.addMedia(name, uri.toString());

//                ClipData clipData = data.getClipData();
//                if (clipData != null) {
//                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                        Uri uri = clipData.getItemAt(i).getUri();
//                        db.addMedia(name, uri.toString());
//                    }
//                    if (Intent.ACTION_SEND_MULTIPLE.equals(data.getAction()) && intent1.hasExtra(Intent.EXTRA_STREAM)) {
//                        // retrieve a collection of selected images
//                        ArrayList<Parcelable> list = intent1.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//                        // iterate over these images
//                        if (list != null) {
//                            for (Parcelable parcel : list) {
//                                Uri uri = (Uri) parcel;
//                                db.addMedia(name, uri.toString());
//                            }
//                            Toast.makeText(this, "Files Added Successfully.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Uri selectedFileUri = data.getData();
//                        db.addMedia(name, selectedFileUri.toString());
//                        Toast.makeText(this, "File Added Successfully.", Toast.LENGTH_SHORT).show();
//                    }
//            }
        }
        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK ) {
            File file = new File(currentPhotoPath);
            Uri uri = Uri.fromFile(file);
            db.addMedia(name, uri.toString());

//            while (true) {
//                dispatchTakePictureIntent();
//            }
        }
        if (requestCode == 101 && resultCode == RESULT_OK ) {
            Toast.makeText(this, "Woohoo!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFileInternal(String path, String fileName) throws IOException {
        FileOutputStream fileOutputStream = openFileOutput(name, MODE_APPEND);
        File file = new File(path);
        byte[] bytes = getBytesFromFile(file);

        fileOutputStream.write(bytes);
        fileOutputStream.close();

        Toast.makeText(this, "SAVED FILE: "+getFilesDir()+"/"+fileName, Toast.LENGTH_SHORT).show();
        Log.d("attman", "SAVED FILE: "+getFilesDir()+"/"+fileName);
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(file);
        return data;
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileExt(Uri uri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(c.getType(uri));
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,        /* prefix */
                ".jpg",         /* suffix */
                storageDir            /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
            }
        }
    }

//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_VIEW);
//                            intent.setDataAndType(selectedFileUri, "application/pdf");
//                            startActivity(intent);

//                            showPdfFromUri(selectedFileUri);
//                            data.getData();

//                            String path = data.getData().getLastPathSegment();
//                            mediaTv.setText(path);

//                            Uri selectedFile = data.getData();
//                            String[] filePathColumn = { MediaStore.Files.FileColumns.DATA };
//                            Cursor cursor = getContentResolver().query(selectedFile,filePathColumn, null, null, null);
//                            cursor.moveToFirst();
//                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                            String filePath = cursor.getString(columnIndex);
//                            cursor.close();
//
//                            mediaTv.setText(filePath);

//                            db.addMedia(name, selectedFileUri.toString());
//                            assert selectedFileUri != null;
//                            db.addMedia(name, selectedFileUri.toString());
//                            Toast.makeText(subject_media.this, "File Added Successfully", Toast.LENGTH_SHORT).show();
//                } else
//                    Toast.makeText(this, "No File Selected.", Toast.LENGTH_SHORT).show();
//            }
//
//
////                            InputStream inputStream = null;
////                            try {
////                                inputStream = getContentResolver().openInputStream(selectedFileUri);
////                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
////                                mediaIv.setImageBitmap(bitmap);
////                            } catch (Exception e) {
////                                Toast.makeText( this, e.getMessage(), Toast.LENGTH_SHORT).show();
////                            }
////                            String s = selectedFileUri.getPath()
////                                File selectedFile = new File(UriToPath(selectedFileUri));
////                            mediaTv.setText(selectedFileUri.toString());
//
//        }

    public String getPath(Context context, Uri uri) {
//        System.out.println("IM IN getPath");
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

}
