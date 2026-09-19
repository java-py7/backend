package com.vendorcoinpay.api;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.vendorcoinpay.AppConfig;
import com.vendorcoinpay.storage.SessionManager;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService apiService;
    private static Retrofit retrofit;

    public static synchronized ApiService getService(Context context) {
        if (apiService == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json");

                        String token = sessionManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(requestBuilder.build());
                    }
                })
                .addInterceptor(logging)
                .build();

            Gson gson = new GsonBuilder()
                .setLenient()
                .create();

            retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static String getErrorMessage(retrofit2.Response<?> response) {
        try {
            if (response != null && response.errorBody() != null) {
                String errorStr = response.errorBody().string();
                Gson gson = new Gson();
                JsonObject obj = gson.fromJson(errorStr, JsonObject.class);
                if (obj != null && obj.has("detail")) {
                    return obj.get("detail").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return "An error occurred. Please try again.";
    }
}
