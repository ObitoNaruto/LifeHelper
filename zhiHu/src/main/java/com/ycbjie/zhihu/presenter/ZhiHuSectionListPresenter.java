package com.ycbjie.zhihu.presenter;

import android.support.annotation.NonNull;

import com.blankj.utilcode.util.NetworkUtils;
import com.ycbjie.library.base.config.AppConfig;
import com.ycbjie.library.db.realm.RealmDbHelper;
import com.ycbjie.library.utils.rxUtils.RxUtil;
import com.ycbjie.zhihu.api.ZhiHuModel;
import com.ycbjie.zhihu.contract.ZhiHuSectionListContract;
import com.ycbjie.zhihu.model.ZhiHuSectionChildBean;

import java.util.List;

import io.realm.Realm;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/11/29
 * 描    述：知乎日报模块        日报
 * 修订历史：
 * ================================================
 */
public class ZhiHuSectionListPresenter implements ZhiHuSectionListContract.Presenter {

    private ZhiHuSectionListContract.View mView;
    @NonNull
    private CompositeSubscription mSubscriptions;
    private Realm realm;


    public ZhiHuSectionListPresenter(ZhiHuSectionListContract.View homeView) {
        this.mView = homeView;
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void subscribe() {
        initRealm();
    }

    private void initRealm() {
        if(realm ==null){
            realm = AppConfig.INSTANCE.getRealmHelper();
        }
    }

    @Override
    public void unSubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void getSectionChildData(int id) {
        ZhiHuModel model = ZhiHuModel.getInstance();
        Subscription rxSubscription = model.getSectionChildList(id)
                .compose(RxUtil.<ZhiHuSectionChildBean>rxSchedulerHelper())
                .map(new Func1<ZhiHuSectionChildBean, ZhiHuSectionChildBean>() {
                    @Override
                    public ZhiHuSectionChildBean call(ZhiHuSectionChildBean zhiHuSectionChildBean) {
                        List<ZhiHuSectionChildBean.StoriesBean> list = zhiHuSectionChildBean.getStories();
                        initRealm();
                        for(ZhiHuSectionChildBean.StoriesBean item : list) {
                            item.setReadState(RealmDbHelper.getInstance().queryNewsId(item.getId()));
                        }
                        return zhiHuSectionChildBean;
                    }
                })
                .subscribe(new Subscriber<ZhiHuSectionChildBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(NetworkUtils.isConnected()){
                            mView.setErrorView();
                        }else {
                            mView.setNetworkErrorView();
                        }
                    }

                    @Override
                    public void onNext(ZhiHuSectionChildBean zhiHuSectionChildBean) {
                        if(zhiHuSectionChildBean!=null){
                            mView.setView(zhiHuSectionChildBean);
                        }else {
                            mView.setEmptyView();
                        }
                    }
                });
        mSubscriptions.add(rxSubscription);
    }


    @Override
    public void insertReadToDB(int id) {
        initRealm();
        RealmDbHelper.getInstance().insertNewsId(id);
    }


}
