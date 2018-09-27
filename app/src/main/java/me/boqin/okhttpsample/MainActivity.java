package me.boqin.okhttpsample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
/**
 * 验证一下两点
 * cache路径以及文件结构
 * response在第一次读取后才会更新cache
 * @description: Created by BoQin on 2018/9/27 4:14 PM
 */
public class MainActivity extends AppCompatActivity {
    final int cacheSize = 10 * 1024 * 1024;
    private OkHttpClient mClient;
    private File mHttpCacheDir;
    private ExecutorService mExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mExecutor = Executors.newSingleThreadExecutor();
        //storage/emulated/0/Android/data/me.boqin.okhttpsample/cache
        mHttpCacheDir = new File(getExternalCacheDir(), "response");
        mClient = new OkHttpClient.Builder()
                .cache(new Cache(mHttpCacheDir, cacheSize))
                .build();

//        final Request request = new Request.Builder().cacheControl(CacheControl.FORCE_CACHE).get().url("https://publicobject.com/helloworld.txt").build();
        final Request request = new Request.Builder().get().url("https://publicobject.com/helloworld.txt").build();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("BQ", e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response!=null) {
//                            Log.d("BQ", response.toString());
                            Log.d("BQ", response.body().string());
                        }else {
                            Log.d("BQ", "null");
                        }
                    }
                });

            }
        });

//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                BufferedSink bufferedSink = null;
//                try {
//                    bufferedSink = Okio.buffer(Okio.sink(mHttpCacheDir));
//                    bufferedSink.writeString("Hello", Charset.defaultCharset());
//                    bufferedSink.flush();
////                    bufferedSink.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };

//        mExecutor.execute(runnable);


    }
}
