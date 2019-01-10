package com.example.asus.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {
    //使用formatTime方法对时间格式化：

    private String formatTime(int length){
        Date date = new Date(length);
        //时间格式化工具
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String totalTime = sdf.format(date);
        return totalTime;
    }

    private Button btn_next, btn_previous, btn_pause, btn_loop;
    private TextView songTextLabel;
    private SeekBar songSeekBar;
    private TextView currentTime;
    private TextView totalTime;
    private int flag;

    private Handler mHandler;

    static MediaPlayer myMediaPlayer;
    int position;

    ArrayList<File> mySongs;
    Thread updateseekBar;

    String sname;

    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = myMediaPlayer.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btn_next = (Button)findViewById(R.id.next);
        btn_previous = (Button)findViewById(R.id.previous);
        btn_pause = (Button)findViewById(R.id.pause);
        songTextLabel = (TextView)findViewById(R.id.songLabel);
        songSeekBar = (SeekBar)findViewById(R.id.seekBar);
        currentTime = (TextView)findViewById(R.id.currentTime);
        totalTime = (TextView)findViewById(R.id.totalTime);
        btn_loop = (Button)findViewById(R.id.loopall);
        flag = 0;//顺序播放


        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                // 展示给进度条和当前时间
                int progress = myMediaPlayer.getCurrentPosition();
                songSeekBar.setProgress(progress);
                currentTime.setText(formatTime(progress));
                // 继续定时发送数据
                updateProgress();
                return true;
            }
        });

        updateseekBar = new Thread(){

            @Override
            public void run() {


                int totalDuration = myMediaPlayer.getDuration();
                int currentPosition = 0;
                updateProgress();

                while (currentPosition < totalDuration){
                    try{
                        sleep(1000);
                        currentPosition = myMediaPlayer.getCurrentPosition();
                        songSeekBar.setProgress(currentPosition);

                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }
        };

        if(myMediaPlayer != null){
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }

        Intent i =getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");

        sname = mySongs.get(position).getName().toString();

        String songName = i.getStringExtra("songname");

        songTextLabel.setText(songName);
        songTextLabel.setSelected(true);

        position = bundle.getInt("pos",0);

        Uri u = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);

        myMediaPlayer.start();
        songSeekBar.setMax(myMediaPlayer.getDuration());
        totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长
        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                myMediaPlayer.stop();
                myMediaPlayer.release();

                if(flag==2){//如果是随机播放
                    Random rand = new Random();
                    int size = rand.nextInt(mySongs.size()) + 1;
                    position = (position+1)%size;
                }else {
                    position = ((position+1)%mySongs.size());
                }

                Uri u = Uri.parse(mySongs.get(position).toString());

                myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                songSeekBar.setMax(myMediaPlayer.getDuration());

                sname = mySongs.get(position).getName().toString();
                songTextLabel.setText(sname);

                myMediaPlayer.start();
                myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        myMediaPlayer.stop();
                        myMediaPlayer.release();

                        if(flag==2){//如果是随机播放
                            Random rand = new Random();
                            int size = rand.nextInt(mySongs.size()) + 1;
                            position = (position+1)%size;
                        }else {
                            position = ((position+1)%mySongs.size());
                        }

                        Uri u = Uri.parse(mySongs.get(position).toString());

                        myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                        totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                        songSeekBar.setMax(myMediaPlayer.getDuration());

                        sname = mySongs.get(position).getName().toString();
                        songTextLabel.setText(sname);

                        myMediaPlayer.start();
                    }
                });
            }
        });



        updateseekBar.start();

        songSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        songSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary),PorterDuff.Mode.SRC_IN);
        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    myMediaPlayer.seekTo(i);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                myMediaPlayer.seekTo(seekBar.getProgress());

            }
        });

        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                songSeekBar.setMax(myMediaPlayer.getDuration());

                if(myMediaPlayer.isPlaying()){
                    btn_pause.setBackgroundResource(R.drawable.icon_play);
                    myMediaPlayer.pause();
                }else {
                    btn_pause.setBackgroundResource(R.drawable.icon_pause);
                    myMediaPlayer.start();
                }
            }
        });


        btn_next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                myMediaPlayer.stop();
                myMediaPlayer.release();
                if(flag==2){//如果是随机播放
                    Random rand = new Random();
                    int size = rand.nextInt(mySongs.size()) + 1;
                    position = (position+1)%size;
                }else {
                    position = ((position+1)%mySongs.size());
                }

                Uri u = Uri.parse(mySongs.get(position).toString());

                myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                songSeekBar.setMax(myMediaPlayer.getDuration());

                sname = mySongs.get(position).getName().toString();
                songTextLabel.setText(sname);

                myMediaPlayer.start();
                myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        myMediaPlayer.stop();
                        myMediaPlayer.release();

                        if(flag==2){//如果是随机播放
                            Random rand = new Random();
                            int size = rand.nextInt(mySongs.size()) + 1;
                            position = (position+1)%size;
                        }else {
                            position = ((position+1)%mySongs.size());
                        }

                        Uri u = Uri.parse(mySongs.get(position).toString());

                        myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                        totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                        songSeekBar.setMax(myMediaPlayer.getDuration());

                        sname = mySongs.get(position).getName().toString();
                        songTextLabel.setText(sname);

                        myMediaPlayer.start();
                        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                myMediaPlayer.stop();
                                myMediaPlayer.release();

                                if(flag==2){//如果是随机播放
                                    Random rand = new Random();
                                    int size = rand.nextInt(mySongs.size()) + 1;
                                    position = (position+1)%size;
                                }else {
                                    position = ((position+1)%mySongs.size());
                                }

                                Uri u = Uri.parse(mySongs.get(position).toString());

                                myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                                totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                                songSeekBar.setMax(myMediaPlayer.getDuration());

                                sname = mySongs.get(position).getName().toString();
                                songTextLabel.setText(sname);

                                myMediaPlayer.start();
                            }
                        });
                    }
                });
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMediaPlayer.stop();
                myMediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u = Uri.parse(mySongs.get(position).toString());

                myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                songSeekBar.setMax(myMediaPlayer.getDuration());

                sname = mySongs.get(position).getName().toString();
                songTextLabel.setText(sname);

                myMediaPlayer.start();
                myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        myMediaPlayer.stop();
                        myMediaPlayer.release();

                        if(flag==2){//如果是随机播放
                            Random rand = new Random();
                            int size = rand.nextInt(mySongs.size()) + 1;
                            position = (position+1)%size;
                        }else {
                            position = ((position+1)%mySongs.size());
                        }

                        Uri u = Uri.parse(mySongs.get(position).toString());

                        myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                        totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                        songSeekBar.setMax(myMediaPlayer.getDuration());

                        sname = mySongs.get(position).getName().toString();
                        songTextLabel.setText(sname);

                        myMediaPlayer.start();
                        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                myMediaPlayer.stop();
                                myMediaPlayer.release();

                                if(flag==2){//如果是随机播放
                                    Random rand = new Random();
                                    int size = rand.nextInt(mySongs.size()) + 1;
                                    position = (position+1)%size;
                                }else {
                                    position = ((position+1)%mySongs.size());
                                }

                                Uri u = Uri.parse(mySongs.get(position).toString());

                                myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                                totalTime.setText(formatTime(myMediaPlayer.getDuration()));//显示歌曲总时长

                                songSeekBar.setMax(myMediaPlayer.getDuration());

                                sname = mySongs.get(position).getName().toString();
                                songTextLabel.setText(sname);

                                myMediaPlayer.start();
                            }
                        });
                    }
                });
            }
        });

        btn_loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(flag == 0){//如果顺序播放
                    //改为单曲循环
                    btn_loop.setBackgroundResource(R.drawable.icon_loopone);
                    flag = 1;
                    myMediaPlayer.setLooping(true);

                    Toast.makeText(getApplicationContext(),"单曲循环",Toast.LENGTH_SHORT).show();

                }else if (flag == 1){//如果单曲循环
                    //改为随机播放
                    btn_loop.setBackgroundResource(R.drawable.icon_random);
                    flag = 2;
                    myMediaPlayer.setLooping(false);

                    Toast.makeText(getApplicationContext(),"随机播放",Toast.LENGTH_SHORT).show();

                }
                else if (flag == 2){//如果随机播放
                    //改为顺序播放
                    btn_loop.setBackgroundResource(R.drawable.icon_loopall);
                    flag = 0;

                    Toast.makeText(getApplicationContext(),"顺序播放",Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId() == android.R.id.home){
            onBackPressed();

        }
        return super.onOptionsItemSelected(item);
    }






}
