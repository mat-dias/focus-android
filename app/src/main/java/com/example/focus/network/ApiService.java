package com.example.focus.network;

import com.example.focus.responses.LoginResponse;
import com.example.focus.responses.BasicResponse;
import com.example.focus.responses.StatsResponse;
import com.example.focus.responses.TaskResponse;
import com.example.focus.responses.UpdateProfileResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @FormUrlEncoded @POST("login.php")
    Call<LoginResponse> login(@Field("email") String email, @Field("senha") String senha);

    @FormUrlEncoded @POST("register.php")
    Call<BasicResponse> register(@Field("nome") String nome, @Field("email") String email, @Field("senha") String senha);

    @FormUrlEncoded @POST("add_task.php")
    Call<BasicResponse> addTask(@Field("profile_id") int profileId, @Field("title") String title, @Field("priority") String priority, @Field("tag") String tag);

    @FormUrlEncoded @POST("get_tasks.php")
    Call<TaskResponse> getTasks(@Field("profile_id") int profileId);

    @FormUrlEncoded @POST("update_task_done.php")
    Call<BasicResponse> updateTaskDone(@Field("task_id") int taskId, @Field("profile_id") int profileId, @Field("done") int done, @Field("scheduling_id") int schedulingId, @Field("schedule_id") int scheduleId);

    @FormUrlEncoded @POST("edit_task.php")
    Call<BasicResponse> editTask(@Field("task_id") int taskId, @Field("profile_id") int profileId, @Field("title") String title, @Field("priority") String priority, @Field("tag") String tag);

    @FormUrlEncoded @POST("delete_task.php")
    Call<BasicResponse> deleteTask(@Field("task_id") int taskId, @Field("profile_id") int profileId);

    @FormUrlEncoded @POST("get_stats.php")
    Call<StatsResponse> getStats(@Field("profile_id") int profileId, @Field("periodo") String periodo);

    @Multipart @POST("update_profile.php")
    Call<UpdateProfileResponse> updateProfile(@Part("user_id") RequestBody userId, @Part("profile_id") RequestBody profileId, @Part("nome") RequestBody nome, @Part("email") RequestBody email, @Part("senha") RequestBody senha, @Part MultipartBody.Part foto);
}