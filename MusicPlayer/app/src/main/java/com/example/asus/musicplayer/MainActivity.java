package com.example.asus.musicplayer;

import android.Manifest;
import com.example.asus.musicplayer.ImageListAdapter;
import com.example.asus.musicplayer.ImageListArray;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity {




    ListView myListViewForSongs;
    String[] items;
    private List<ImageListArray> onePieceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myListViewForSongs = (ListView)findViewById(R.id.mySongListView);
        onePieceList = new ArrayList<>();

        runtimePermission();
    }

    public void runtimePermission(){

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        display();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        token.continuePermissionRequest();
                    }
                }).check();

    }

    public ArrayList<File> findSong(File file){

        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        for(File singleFile: files){

            if(singleFile.isDirectory() && !singleFile.isHidden()){

                arrayList.addAll(findSong(singleFile));
            }
            else {
                if(singleFile.getName().endsWith(".mp3") ||
                singleFile.getName().endsWith(".wav")) {

                    arrayList.add(singleFile);
                }
            }
        }

        return arrayList;
    }

    void display(){

        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());


        items = new String[mySongs.size()];

        for (int i=0;i<mySongs.size();i++){

            items[i] = mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");

            // 导入数据
            ImageListArray ace =new ImageListArray(items[i], R.drawable.icon_music);
            onePieceList.add(ace);

        }

//        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
//        myListViewForSongs.setAdapter(myAdapter);


        //创建适配器，在适配器中导入数据 1.当前类 2.list_view一行的布局 3.数据集合
        ImageListAdapter imageListAdapter = new ImageListAdapter(this, R.layout.items,onePieceList);
        myListViewForSongs.setAdapter(imageListAdapter);


        myListViewForSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//                String songName = myListViewForSongs.getItemAtPosition(i).toString();

                String songName = mySongs.get(i).getName().toString();
                startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                .putExtra("songs",mySongs).putExtra("songname",songName)
                .putExtra("pos",i));
            }
        });


    }

}
