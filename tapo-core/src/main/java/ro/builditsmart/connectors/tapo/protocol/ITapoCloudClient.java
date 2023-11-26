package ro.builditsmart.connectors.tapo.protocol;

import ro.builditsmart.models.tapo.cloud.CloudListDeviceResponse;
import ro.builditsmart.models.tapo.cloud.CloudLoginResponse;
import ro.builditsmart.models.tapo.cloud.CloudRefreshLoginResponse;
import ro.builditsmart.models.tapo.video.TapoVideoList;

import java.util.concurrent.CompletableFuture;

public interface ITapoCloudClient {

    CompletableFuture<CloudLoginResponse.CloudLoginResult> loginAsync(String email, String password, boolean refreshTokenNeeded);

    CompletableFuture<CloudRefreshLoginResponse.CloudRefreshLoginResult> refreshLoginAsync(String refreshToken);

    CompletableFuture<CloudListDeviceResponse.CloudListDeviceResult> listDevicesAsync(String cloudToken);

    CompletableFuture<TapoVideoList> listVideosAsync(String cloudToken);

}