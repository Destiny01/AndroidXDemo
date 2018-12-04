package com.jinnuojiayin.retrofit;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author xyl
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //第二步：The Retrofit class generates an implementation of the GitHubService interface.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                //数据解析器
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        GitHubService service = retrofit.create(GitHubService.class);

        Call<List<Repo>> repos = service.listRepos("Destiny01");
        repos.enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                for (Repo repo : response.body()) {
                    Log.i("repos", repo.toString());
                }
            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable t) {

            }
        });

    }
}
