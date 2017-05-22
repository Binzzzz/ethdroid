package com.sqli.blockchain.ethdroid.model;

import com.sqli.blockchain.ethdroid.EthDroid;

import org.ethereum.geth.Header;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Subscription;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by gunicolas on 22/05/17.
 */

public class Filter {

    private static final Scheduler DEFAULT_SCHEDULER = Schedulers.newThread();
    private static final long DEFAULT_BUFFER_SIZE = 16; //See go-ethereum tests https://git.io/v9X1O

    public static Observable<Header> newHeadFilter(EthDroid eth){
        return Observable.create(new HeadFilterSubscriber(eth))
            .subscribeOn(DEFAULT_SCHEDULER);
    }

    private static class HeadFilterSubscriber implements Observable.OnSubscribe<Header>{

        EthDroid eth;

        HeadFilterSubscriber(EthDroid eth) {
            this.eth = eth;
        }

        @Override
        public void call(Subscriber<? super Header> subscriber) {
            HeadListener listener = new HeadListener(subscriber);
            try {
                listener.subscription = eth.getClient().subscribeNewHead(eth.getMainContext(),listener,DEFAULT_BUFFER_SIZE);
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        }
    }
    private static class HeadListener implements NewHeadHandler{

        Subscriber<? super Header> subscriber;
        Subscription subscription;

        public HeadListener(Subscriber<? super Header> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onError(String s) {
            if( subscriber.isUnsubscribed() ){
                subscription.unsubscribe();
            } else{
                subscriber.onError(new Throwable(s));
            }
        }

        @Override
        public void onNewHead(Header header) {
            if( subscriber.isUnsubscribed() ){
                subscription.unsubscribe();
            } else{
                subscriber.onNext(header);
            }
        }
    }

}
