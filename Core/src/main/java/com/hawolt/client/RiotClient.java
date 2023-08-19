package com.hawolt.client;

import com.hawolt.client.handler.RMSHandler;
import com.hawolt.client.handler.RTMPHandler;
import com.hawolt.client.handler.XMPPHandler;
import com.hawolt.virtual.leagueclient.client.VirtualLeagueClient;
import com.hawolt.virtual.leagueclient.exception.LeagueException;
import com.hawolt.virtual.leagueclient.instance.VirtualLeagueClientInstance;
import com.hawolt.virtual.riotclient.client.VirtualRiotClient;
import com.hawolt.virtual.riotclient.instance.VirtualRiotClientInstance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Created: 11/08/2023 19:20
 * Author: Twitter @hawolt
 **/

public class RiotClient implements BiConsumer<VirtualLeagueClient, Throwable> {

    private final ClientConfiguration configuration;
    private final IClientCallback callback;

    public RiotClient(ClientConfiguration configuration, IClientCallback callback) {
        this.configuration = configuration;
        this.callback = callback;
        this.loginAndCreate();
    }

    public ClientConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void accept(VirtualLeagueClient virtualLeagueClient, Throwable throwable) {
        if (throwable != null) callback.onLoginFlowException(throwable);
        else {
            LeagueClient client = new LeagueClient(virtualLeagueClient);
            try {
                finalize(client);
                callback.onClient(client);
            } catch (IOException | URISyntaxException e) {
                callback.onLoginFlowException(e);
            }
        }
    }

    private void finalize(LeagueClient client) throws IOException, URISyntaxException {
        if (configuration.getComplete()) client.setRMS(RMSHandler.build(client).connect());
        if (!configuration.getMinimal()) client.setXMPP(XMPPHandler.build(client).connect());
        if (configuration.getComplete()) client.setRTMP(RTMPHandler.build(client).connect());
    }

    private void loginAndCreate() {
        VirtualRiotClientInstance virtualRiotClientInstance = VirtualRiotClientInstance.create(
                configuration.getGateway(),
                configuration.getCookieSupplier(),
                false
        );
        try {
            login(configuration, virtualRiotClientInstance);
        } catch (IOException | LeagueException e) {
            callback.onLoginFlowException(e);
        }
    }

    private void login(ClientConfiguration configuration, VirtualRiotClientInstance virtualRiotClientInstance) throws IOException, LeagueException {
        String username = configuration.getUsername();
        String password = configuration.getPassword();
        VirtualRiotClient virtualRiotClient = virtualRiotClientInstance.login(username, password, configuration.getMultifactorSupplier());
        VirtualLeagueClientInstance virtualLeagueClientInstance = virtualRiotClient.createVirtualLeagueClientInstance();
        CompletableFuture<VirtualLeagueClient> virtualLeagueClientFuture = virtualLeagueClientInstance.login(
                configuration.getIgnoreSummoner(),
                configuration.getSelfRefresh(),
                configuration.getComplete(),
                configuration.getMinimal()
        );
        virtualLeagueClientFuture.whenComplete(this);
    }
}
