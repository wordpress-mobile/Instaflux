package org.wordpress.android.fluxc.instaflux;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.AuthenticationActionBuilder;
import org.wordpress.android.fluxc.generated.SiteActionBuilder;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.network.HTTPAuthManager;
import org.wordpress.android.fluxc.network.MemorizingTrustManager;
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.util.AppLog;

import javax.inject.Inject;

public class MainInstafluxActivity extends AppCompatActivity {
    @Inject SiteStore mSiteStore;
    @Inject AccountStore mAccountStore;
    @Inject PostStore mPostStore;
    @Inject Dispatcher mDispatcher;
    @Inject HTTPAuthManager mHTTPAuthManager;
    @Inject MemorizingTrustManager mMemorizingTrustManager;

    // Would be great to not have to keep this state, but it makes HTTPAuth and self signed SSL management easier
    private SiteStore.RefreshSitesXMLRPCPayload mSelfhostedPayload;

    private SiteModel mSelectedSite = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((InstafluxApp) getApplication()).component().inject(this);

        // if the user is already logged in switch to PostActivity immediately
        if (mSiteStore.hasSite()) {
            launchPostActivity();
        }

        setContentView(R.layout.activity_main);

        Button signInBtn = (Button) findViewById(R.id.sign_in_button);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Order is important here since onRegister could fire onChanged events. "register(this)" should probably go
        // first everywhere.
        mDispatcher.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDispatcher.unregister(this);
    }

    private void showSignInDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DialogFragment newFragment = ThreeEditTextDialog.newInstance(new ThreeEditTextDialog.Listener() {
            @Override
            public void onClick(String username, String password, String url) {
                signInAction(username, password, url);
            }
        }, "Username", "Password", "XMLRPC Url");
        newFragment.show(ft, "dialog");
    }

    private void showHTTPAuthDialog(final String url) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DialogFragment newFragment = ThreeEditTextDialog.newInstance(new ThreeEditTextDialog.Listener() {
            @Override
            public void onClick(String username, String password, String unused) {
                // Add credentials
                mHTTPAuthManager.addHTTPAuthCredentials(username, password, url, null);
                // Retry login action
                if (mSelfhostedPayload != null) {
                    signInAction(mSelfhostedPayload.username, mSelfhostedPayload.password, url);
                }
            }
        }, "Username", "Password", "unused");
        newFragment.show(ft, "dialog");
    }

    private void showSSLWarningDialog(String certifString) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SSLWarningDialog newFragment = SSLWarningDialog.newInstance(
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Add the certificate to our list
                        mMemorizingTrustManager.storeLastFailure();
                        // Retry login action
                        if (mSelfhostedPayload != null) {
                            signInAction(mSelfhostedPayload.username, mSelfhostedPayload.password,
                                    mSelfhostedPayload.url);
                        }
                    }
                }, certifString);
        newFragment.show(ft, "dialog");
    }

    private void signInAction(final String username, final String password, final String url) {
        if (TextUtils.isEmpty(url)) {
            wpcomFetchSites(username, password);
        } else {
            mSelfhostedPayload = new SiteStore.RefreshSitesXMLRPCPayload();
            mSelfhostedPayload.url = url;
            mSelfhostedPayload.username = username;
            mSelfhostedPayload.password = password;

            mDispatcher.dispatch(AuthenticationActionBuilder.newDiscoverEndpointAction(mSelfhostedPayload));
        }
    }

    private void wpcomFetchSites(String username, String password) {
        AccountStore.AuthenticatePayload payload = new AccountStore.AuthenticatePayload(username, password);
        // Next action will be dispatched if authentication is successful
        payload.nextAction = SiteActionBuilder.newFetchSitesAction();
        mDispatcher.dispatch(AuthenticationActionBuilder.newAuthenticateAction(payload));
    }

    private void selfHostedFetchSites(String username, String password, String xmlrpcEndpoint) {
        SiteStore.RefreshSitesXMLRPCPayload payload = new SiteStore.RefreshSitesXMLRPCPayload();
        payload.username = username;
        payload.password = password;
        payload.url = xmlrpcEndpoint;
        mSelfhostedPayload = payload;
        // Self Hosted don't have any "Authentication" request, try to list sites with user/password
        mDispatcher.dispatch(SiteActionBuilder.newFetchSitesXmlRpcAction(payload));
    }

    private void launchPostActivity() {
        Intent intent = new Intent(this, PostActivity.class);
        startActivity(intent);
    }

    // Event listeners

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountChanged(AccountStore.OnAccountChanged event) {
        if (!mAccountStore.hasAccessToken()) {
            //Signed out!
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthenticationChanged(AccountStore.OnAuthenticationChanged event) {
        if (event.isError) {
            AppLog.e(AppLog.T.API, "Authentication error: " + event.errorType + " - " + event.errorMessage);
            if (event.errorType == AccountStore.AuthenticationError.HTTP_AUTH_ERROR) {
                // Show a Dialog prompting for http username and password
                showHTTPAuthDialog(mSelfhostedPayload.url);
            }
            if (event.errorType == AccountStore.AuthenticationError.INVALID_SSL_CERTIFICATE) {
                // Show a SSL Warning Dialog
                showSSLWarningDialog(mMemorizingTrustManager.getLastFailure().toString());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscoverySucceeded(AccountStore.OnDiscoverySucceeded event) {
        AppLog.i(AppLog.T.API, "Discovery succeeded, endpoint: " + event.xmlRpcEndpoint);
        selfHostedFetchSites(mSelfhostedPayload.username, mSelfhostedPayload.password, event.xmlRpcEndpoint);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscoveryFailed(AccountStore.OnDiscoveryFailed event) {
        if (event.error == SelfHostedEndpointFinder.DiscoveryError.WORDPRESS_COM_SITE) {
            wpcomFetchSites(mSelfhostedPayload.username, mSelfhostedPayload.password);
        } else if (event.error == SelfHostedEndpointFinder.DiscoveryError.HTTP_AUTH_REQUIRED) {
            showHTTPAuthDialog(event.failedEndpoint);
        } else if (event.error == SelfHostedEndpointFinder.DiscoveryError.ERRONEOUS_SSL_CERTIFICATE) {
            mSelfhostedPayload.url = event.failedEndpoint;
            showSSLWarningDialog(mMemorizingTrustManager.getLastFailure().toString());
        }
        AppLog.e(AppLog.T.API, "Discovery failed with error: " + event.error);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSiteChanged(SiteStore.OnSiteChanged event) {
        if (mSiteStore.hasSite()) {
            launchPostActivity();
        }
    }
}