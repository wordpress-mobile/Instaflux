package org.wordpress.android.stores.network.rest.wpcom;

public enum WPCOMREST {
    // Me
    ME("/me/"),
    ME_SETTINGS("/me/settings/"),
    ME_SITES("/me/sites/"),

    // Posts
    POSTS("/sites/$site/posts"),
    // TODO: Collapse into one?
    POST_GET("/sites/$site/posts/$post_ID"),
    POST_EDIT("/sites/$site/posts/$post_ID"),
    POST_NEW("/sites/$site/posts/new"),
    POST_DELETE("/sites/$site/posts/$post_ID/delete"),


    // Sites
    SITES("/sites/"),
    SITES_NEW("/sites/new"),

    // Users
    USERS_NEW("/users/new");

    private static final String WPCOM_REST_PREFIX = "https://public-api.wordpress.com/rest";
    private static final String WPCOM_PREFIX_V1 = WPCOM_REST_PREFIX + "/v1";
    private static final String WPCOM_PREFIX_V1_1 = WPCOM_REST_PREFIX + "/v1.1";
    private static final String WPCOM_PREFIX_V1_2 = WPCOM_REST_PREFIX + "/v1.2";
    private static final String WPCOM_PREFIX_V1_3 = WPCOM_REST_PREFIX + "/v1.3";

    private final String mEndpoint;

    WPCOMREST(String endpoint) {
        mEndpoint = endpoint;
    }

    @Override
    public String toString() {
        return mEndpoint;
    }

    public String getUrlV1() {
        return WPCOM_PREFIX_V1 + mEndpoint;
    }

    public String getUrlV1_1() {
        return WPCOM_PREFIX_V1_1 + mEndpoint;
    }

    public String getUrlV1_2() {
        return WPCOM_PREFIX_V1_2 + mEndpoint;
    }

    public String getUrlV1_3() {
        return WPCOM_PREFIX_V1_3 + mEndpoint;
    }
}
