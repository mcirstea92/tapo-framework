package ro.builditsmart.rest.tapo.services;

import org.springframework.stereotype.Service;
import ro.builditsmart.connectors.tapo.protocol.ITapoDeviceClient;
import ro.builditsmart.connectors.tapo.protocol.KlapDeviceClient;
import ro.builditsmart.connectors.tapo.protocol.SecurePassThroughDeviceClient;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TapoClientService {

    private final Map<TapoDeviceProtocol, ITapoDeviceClient> clients;

    public TapoClientService() {
        this.clients = new ConcurrentHashMap<>();
        this.clients.put(TapoDeviceProtocol.SecurePassThrough, new SecurePassThroughDeviceClient("private"));
        this.clients.put(TapoDeviceProtocol.Klap, new KlapDeviceClient());
    }

    public ITapoDeviceClient getClient(String protocol) {
        return clients.get(TapoDeviceProtocol.fromAlias(protocol));
    }

}
