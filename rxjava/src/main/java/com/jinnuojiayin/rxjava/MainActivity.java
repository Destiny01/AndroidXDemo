package com.jinnuojiayin.rxjava;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //RxJava 三部曲
        //第一步：初始化 Observable
        //第二步：初始化 Observer
        //第三步：建立订阅关系

        test06();
    }

    /**
     * Filter
     * Filter 你会很常用的，它的作用也很简单，过滤器嘛。可以接受一个参数，让其过滤掉不符合我们条件的值
     */
    private void test08() {
        Observable.just(1, 20, 65, -5, 7, 19)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer >= 10;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                Log.e(TAG, "filter : " + integer + "\n");
            }
        });
    }

    /**
     * distinct
     * 这个操作符非常的简单、通俗、易懂，就是简单的去重嘛，
     */
    private void test07() {
        Observable.just(1, 1, 1, 2, 2, 3, 4, 5)
                .distinct()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Log.e(TAG, "distinct : " + integer + "\n");
                    }
                });
    }

    /**
     * concatMap
     * 与FlatMap用法一直，能保证有序
     */
    private void test06() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        Log.e(TAG, "flatMap : accept : " + s + "\n");
                    }
                });
    }

    /**
     * FlatMap
     * FlatMap 是一个很有趣的东西，我坚信你在实际开发中会经常用到。
     * 它可以把一个发射器 Observable 通过某种方法转换为多个 Observables，
     * 然后再把这些分散的 Observables装进一个单一的发射器 Observable。
     * 但有个需要注意的是，flatMap 并不能保证事件的顺序，如果需要保证，需要用到我们下面要讲的 ConcatMap
     */
    private void test05() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        Log.e(TAG, "flatMap : accept : " + s + "\n");
                    }
                });
    }

    /**
     * Concat
     * 对于单一的把两个发射器连接成一个发射器，虽然 zip 不能完成，但我们还是可以自力更生，官方提供的 concat 让我们的问题得到了完美解决。
     */
    private void test04() {
        Observable.concat(Observable.just(1, 2, 3), Observable.just(4, 5, 6))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Log.e(TAG, "concat : " + integer + "\n");
                    }
                });

    }

    /**
     * Zip
     * zip 专用于合并事件，该合并不是连接（连接操作符后面会说），而是两两配对，也就意味着，
     * 最终配对出的 Observable 发射事件数目只和少的那个相同。
     * <p>
     * 注意：zip 组合事件的过程就是分别从发射器 A 和发射器 B 各取出一个事件来组合，并且一个事件只能被使用一次，
     * 组合的顺序是严格按照事件发送的顺序来进行的，所以上面截图中，可以看到，1 永远是和 A 结合的，2 永远是和 B 结合的。
     * 最终接收器收到的事件数量是和发送器发送事件最少的那个发送器的发送事件数目相同，所以如截图中，5 很孤单，没有人愿意和它交往，孤独终老的单身狗。
     */
    private void test03() {
        Observable.zip(getStringObservable(), getIntegerObservable(), new BiFunction<String, Integer, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull Integer integer) throws Exception {
                return s + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.e(TAG, "zip : accept : " + s + "\n");
            }
        });
    }

    private Observable<String> getStringObservable() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext("A");
                    Log.e(TAG, "String emit : A \n");
                    e.onNext("B");
                    Log.e(TAG, "String emit : B \n");
                    e.onNext("C");
                    Log.e(TAG, "String emit : C \n");
                }
            }
        });
    }

    private Observable<Integer> getIntegerObservable() {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext(1);
                    Log.e(TAG, "Integer emit : 1 \n");
                    e.onNext(2);
                    Log.e(TAG, "Integer emit : 2 \n");
                    e.onNext(3);
                    Log.e(TAG, "Integer emit : 3 \n");
                    e.onNext(4);
                    Log.e(TAG, "Integer emit : 4 \n");
                    e.onNext(5);
                    Log.e(TAG, "Integer emit : 5 \n");
                }
            }
        });
    }

    /**
     * Map
     * Map 基本算是 RxJava 中一个最简单的操作符了，熟悉 RxJava 1.x 的知道，
     * 它的作用是对发射时间发送的每一个事件应用一个函数，是的每一个事件都按照指定的函数去变化，
     * 而在 2.x 中它的作用几乎一致。
     * <p>
     * 是的，map 基本作用就是将一个 Observable 通过某种函数关系，转换为另一种 Observable，
     * 上面例子中就是把我们的 Integer 数据变成了 String 类型。从Log日志显而易见。
     */
    private void test02() {

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "This is result" + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG, "accept : " + s + "\n");
            }
        });
    }

    /**
     * Create
     * create 操作符应该是最常见的操作符了，主要用于产生一个 Obserable 被观察者对象，
     * 为了方便大家的认知，以后的教程中统一把被观察者 Observable 称为发射器（上游事件），
     * 观察者 Observer 称为接收器（下游事件）。
     */
    private void test01() {
        //第一步：初始化Observable
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG, "Observable emit 1" + "\n");
                emitter.onNext(1);
                Log.e(TAG, "Observable emit 2" + "\n");
                emitter.onNext(2);
                Log.e(TAG, "Observable emit 3" + "\n");
                emitter.onNext(3);
                emitter.onComplete();
                Log.e(TAG, "Observable emit 4" + "\n");
                emitter.onNext(4);
            }
            //第三步：订阅
        }).subscribe(new Observer<Integer>() {
            //第二步：初始化Observer
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe : " + d.isDisposed() + "\n");
                mDisposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.e(TAG, "onNext : value : " + integer + "\n");
                i++;
                if (i == 2) {
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件
                    mDisposable.dispose();
                    Log.e(TAG, "onNext : isDisposable : " + mDisposable.isDisposed() + "\n");
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError : value : " + e.getMessage() + "\n");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete" + "\n");
            }
        });
    }
}
