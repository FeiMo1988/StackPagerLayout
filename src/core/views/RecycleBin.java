package core.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dingwei.chen1988@gmail.com
 * */
public class RecycleBin<T> {

    private List<T> mRecycleBin = new ArrayList<T>();
    private int mMaxRecycleNumber = 0;


    public RecycleBin(int maxRecycleNumber) {
        this.mMaxRecycleNumber = maxRecycleNumber;
    }

    public boolean recycle(T view) {
        if (mRecycleBin.size() < mMaxRecycleNumber) {
            mRecycleBin.add(view);
            return true;
        }
        return false;
    }

    public void recycle(T[] views) {
        if (views == null) {
            return ;
        }
        for(T t:views) {
            if (!recycle(t)) {
                break;
            };
        }
    }

    public void recycle(Collection<T> collection) {
        mRecycleBin.addAll(collection);
    }

    public T obtain() {
        if (mRecycleBin.size() > 0) {
            return mRecycleBin.remove(0);
        }
        return null;
    }


}
