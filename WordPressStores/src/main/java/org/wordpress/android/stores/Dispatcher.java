package org.wordpress.android.stores;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.wordpress.android.stores.action.Action;
import org.wordpress.android.stores.store.Store;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import javax.inject.Singleton;

@Singleton
public class Dispatcher {
    private final Bus mBus;

    public Dispatcher() {
        mBus = new Bus(ThreadEnforcer.ANY);
    }

    public void register(final Object object) {
        mBus.register(object);
        if (object instanceof Store) {
            ((Store) object).onRegister();
        }
    }

    public void unregister(final Object object) {
        mBus.unregister(object);
    }

    public void dispatch(Action action) {
        AppLog.d(T.API, "Dispatching action: " + action.getType().getClass().getSimpleName()
                + "-" + action.getType().name());
        post(action);
    }

    public void emitChange(final Object changeEvent) {
        mBus.post(changeEvent);
    }

    private void post(final Object event) {
        mBus.post(event);
    }
}
