package com.jinnuojiayin.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * 第一步：Retrofit turns your HTTP API into a Java interface.
 * Created by xyl on 2018/12/3.
 */
public interface GitHubService {
    /**
     * 请某个用户下的Github仓库列表
     *
     * @param user
     * @return
     */
    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);
}
