package org.wordpress.android.fluxc.instaflux;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.Interceptor;

@Module
public class InterceptorModule {
    @Provides @IntoSet @Named("network-interceptors")
    public Interceptor provideStethoInterceptor() {
        return new StethoInterceptor();
    }
}
