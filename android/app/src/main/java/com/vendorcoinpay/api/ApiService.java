package com.vendorcoinpay.api;

import com.vendorcoinpay.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/v1/auth/register")
    Call<TokenResponse> register(@Body UserRegisterRequest request);

    @POST("api/v1/auth/login")
    Call<TokenResponse> login(@Body UserLoginRequest request);

    @GET("api/v1/users/me")
    Call<User> getCurrentUser();

    @PUT("api/v1/users/me")
    Call<User> updateProfile(@Body UserProfileUpdateRequest request);

    @GET("api/v1/users/resolve/{identifier}")
    Call<User> resolveUser(@Path("identifier") String identifier);

    @GET("api/v1/wallet/balance")
    Call<Wallet> getWalletBalance();

    @POST("api/v1/payments/send")
    Call<PaymentResponse> sendCoins(@Body PaymentRequest request);

    @GET("api/v1/transactions")
    Call<List<Transaction>> getTransactions(
        @Query("filter_type") String filterType,
        @Query("limit") int limit,
        @Query("offset") int offset
    );

    @GET("api/v1/transactions/{transaction_id}")
    Call<Transaction> getTransactionDetail(@Path("transaction_id") int transactionId);

    @GET("api/v1/qr/my/payload")
    Call<QRResolveResponse> getMyQrPayload();

    @GET("api/v1/qr/resolve/{identifier}")
    Call<QRResolveResponse> resolveQrCode(@Path("identifier") String identifier);
}
