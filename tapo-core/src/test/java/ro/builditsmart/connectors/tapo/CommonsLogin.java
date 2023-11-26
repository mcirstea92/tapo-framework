package ro.builditsmart.connectors.tapo;

import ro.builditsmart.models.tapo.TapoDeviceKey;

import java.util.concurrent.CompletableFuture;

public interface CommonsLogin {

    CompletableFuture<TapoDeviceKey> loginByIpAsync(String ipAddress, String email, String password);

}
