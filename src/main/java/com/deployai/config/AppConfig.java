package com.deployai.config;

public class AppConfig {

    public static final String API_BASE_URL = "https://deployai-backend.onrender.com/api";

    public static final String ENDPOINT_LOGIN    = "/auth/login";
    public static final String ENDPOINT_REGISTER = "/auth/register";
    public static final String ENDPOINT_CHAT     = "/chat";
    public static final String ENDPOINT_HISTORY  = "/conversations";

    public static final String APP_NAME    = "Deploy AI";
    public static final String APP_VERSION = "1.0.0";

    public static final int WINDOW_WIDTH  = 1100;
    public static final int WINDOW_HEIGHT = 720;
    public static final int MIN_WIDTH     = 800;
    public static final int MIN_HEIGHT    = 550;

    private AppConfig() {}
}