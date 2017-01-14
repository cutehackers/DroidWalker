package com.jhlee.android.droidwalker.ui.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * DroidWalker
 *
 * custom event manager.
 *
 * 참조
 * https://github.com/kubode/RxEventBus/blob/master/rxeventbus/src/main/java/com/github/kubode/rxeventbus/RxEventBus.java
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class RxEventManager {

    private static RxEventManager sInstance = new RxEventManager();

    private final Subject<Object, Object> mSubjects = new SerializedSubject<>(PublishSubject.create());
    private final Map<Class, Integer> mRefCountMap = new HashMap<>();

    private RxEventManager() {
        // empty constructor
    }

    public static RxEventManager instance() {
        return sInstance;
    }

    /**
     * Post an event to subscribed handlers.
     * It can detect event is not handled.
     *
     * @param <E>       Type of {@code event}.
     * @param event     An event to post.
     * @param unhandled It will be called if {@code event} is not handled.
     *                  Note: If handler subscribed by using async {@link Scheduler}, it can't guarantee {@code event} is actually handled.
     */
    public <E> void post(@NonNull E event, @Nullable Action1<E> unhandled) {
        if (getRefCount(event.getClass()) > 0) {
            mSubjects.onNext(event);
        } else {
            if (unhandled != null) {
                unhandled.call(event);
            }
        }
    }

    /**
     * Post an event to subscribed handlers.
     * Do nothing on unhandled.
     *
     * @param <E>   Type of {@code event}.
     * @param event An event to post.
     * @see #post(Object, Action1)
     */
    public <E> void post(@NonNull E event) {
        post(event, null);
    }

    /**
     * Subscribe {@code handler} to receive events type of specified class.
     * <p>
     * You should call {@link Subscription#unsubscribe()} if you want to stop receiving events.
     *
     * @param <E>       Type of {@code event}.
     * @param clazz     Type of event that you want to receive.
     * @param handler   It will be called when {@code clazz} and the same type of events were posted.
     * @param scheduler {@code handler} will dispatched to this scheduler.
     * @return A {@link Subscription} which can stop observing by calling {@link Subscription#unsubscribe()}.
     */
    public <E> Subscription subscribe(@NonNull final Class<E> clazz, @NonNull Action1<E> handler, @NonNull Scheduler scheduler) {
        addRefCount(clazz);
        return mSubjects
                .ofType(clazz)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        removeRefCount(clazz);
                    }
                })
                .observeOn(scheduler)
                .subscribe(handler);
    }

    /**
     * Subscribe {@code handler} to receive events type of specified class.
     * <p>
     * Handler scheduled by {@link Schedulers#immediate()}
     *
     * @param <E>     Type of {@code event}.
     * @param clazz   Type of event that you want to receive.
     * @param handler It will be called when {@code clazz} and the same type of events were posted.
     * @return A {@link Subscription} which can stop observing by calling {@link Subscription#unsubscribe()}.
     * @see #subscribe(Class, Action1, Scheduler)
     */
    public <E> Subscription subscribe(@NonNull Class<E> clazz, @NonNull Action1<E> handler) {
        return subscribe(clazz, handler, Schedulers.immediate());
    }

    private synchronized int getRefCount(Class clazz) {
        if (mRefCountMap.containsKey(clazz)) {
            return mRefCountMap.get(clazz);
        } else {
            return 0;
        }
    }

    private synchronized void setRefCount(Class clazz, int refCount) {
        if (refCount == 0) {
            mRefCountMap.remove(clazz);
        } else {
            mRefCountMap.put(clazz, refCount);
        }
    }

    private synchronized void addRefCount(Class clazz) {
        setRefCount(clazz, getRefCount(clazz) + 1);
    }

    private synchronized void removeRefCount(Class clazz) {
        setRefCount(clazz, getRefCount(clazz) - 1);
    }
}
