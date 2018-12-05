package com.sometimestwo.jumble.API;

public class GfyCatTokenRequest {
    private String grant_type;
    private String client_id;
    private String client_secret;

    public GfyCatTokenRequest(String grantType, String clientId, String clientSecret) {
        this.grant_type = grantType;
        this.client_id = clientId;
        this.client_secret = clientSecret;
    }
}
