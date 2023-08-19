package com.hawolt.client;

/**
 * Created: 11/08/2023 19:21
 * Author: Twitter @hawolt
 **/

public interface IClientCallback {
    void onClient(LeagueClient client);

    void onLoginFlowException(Throwable throwable);
}
