package com.hawolt.client;

import com.hawolt.authentication.ICookieSupplier;
import com.hawolt.authentication.LocalCookieSupplier;
import com.hawolt.http.Gateway;
import com.hawolt.virtual.riotclient.instance.MultiFactorSupplier;

/**
 * Created: 27/07/2023 21:31
 * Author: Twitter @hawolt
 **/

public class ClientConfiguration {
    private final Builder builder;

    public ClientConfiguration(Builder builder) {
        this.builder = builder;
    }

    public MultiFactorSupplier getMultifactorSupplier() {
        return builder.multifactor;
    }

    public String getUsername() {
        return builder.username;
    }

    public String getPassword() {
        return builder.password;
    }

    public boolean getIgnoreSummoner() {
        return builder.ignoreSummoner;
    }

    public boolean getSelfRefresh() {
        return builder.selfRefresh;
    }

    public boolean getComplete() {
        return builder.complete;
    }

    public boolean getMinimal() {
        return builder.minimal;
    }

    public Gateway getGateway() {
        return builder.gateway;
    }

    public ICookieSupplier getCookieSupplier() {
        return builder.supplier;
    }

    public static class Builder {
        private Boolean ignoreSummoner, selfRefresh, complete, minimal;
        private String username, password;
        private MultiFactorSupplier multifactor;
        private ICookieSupplier supplier;
        private Gateway gateway;

        public Builder setGateway(Gateway gateway) {
            this.gateway = gateway;
            return this;
        }

        public Builder setMultifactorSupplier(MultiFactorSupplier multifactor) {
            this.multifactor = multifactor;
            return this;
        }

        public Builder setSupplier(ICookieSupplier supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder setIgnoreSummoner(boolean ignoreSummoner) {
            this.ignoreSummoner = ignoreSummoner;
            return this;
        }

        public Builder setSelfRefresh(boolean selfRefresh) {
            this.selfRefresh = selfRefresh;
            return this;
        }

        public Builder setMinimal(boolean minimal) {
            this.minimal = minimal;
            return this;
        }

        public Builder setComplete(boolean complete) {
            this.complete = complete;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public ClientConfiguration build() throws IncompleteConfigurationException {
            if (ignoreSummoner == null || selfRefresh == null || complete == null || minimal == null) {
                throw new IncompleteConfigurationException();
            }
            return new ClientConfiguration(this);
        }
    }

    public static ClientConfiguration getDefault(String username, String password, MultiFactorSupplier multifactor) {
        try {
            return new Builder()
                    .setSupplier(new LocalCookieSupplier())
                    .setMultifactorSupplier(multifactor)
                    .setIgnoreSummoner(false)
                    .setUsername(username)
                    .setPassword(password)
                    .setSelfRefresh(true)
                    .setMinimal(false)
                    .setComplete(true)
                    .setGateway(null)
                    .build();
        } catch (IncompleteConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
