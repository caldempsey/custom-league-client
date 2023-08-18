package com.hawolt.ui.login;

import com.hawolt.virtual.leagueclient.exception.LeagueException;

import java.io.IOException;

/**
 * Created: 07/08/2023 18:30
 * Author: Twitter @hawolt
 **/

public interface ILoginCallback {
    void onLogin(String username, String password) throws LeagueException, IOException;
}
