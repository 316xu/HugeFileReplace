package cn.xujifa.readhugefile;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xujifa on 17-6-26.
 */
public class Cache {


    private int mOrder;
    private Map<Integer, ByteBuffer> mOrderToBuffer;
    private Condition mIsReady;
    private ReentrantLock mLock;
    private int mCompleteOrder = Integer.MAX_VALUE;

    public Cache() {

        mLock = new ReentrantLock(false);
        mIsReady = mLock.newCondition();
        mOrderToBuffer = new HashMap<>();
    }

    public void put(int order, ByteBuffer byteBuffer) throws InterruptedException {
        mLock.lockInterruptibly();
        mOrderToBuffer.put(order, byteBuffer);
        if (order == mOrder) {
            mIsReady.signal();
        }
        mLock.unlock();

    }

    public ByteBuffer take() throws InterruptedException {

        mLock.lockInterruptibly();
        ByteBuffer byteBuffer = mOrderToBuffer.get(mOrder);
        if (byteBuffer == null && mOrder <= mCompleteOrder) {
            mIsReady.await();
            byteBuffer = mOrderToBuffer.get(mOrder);
        }
        mOrder++;
        mLock.unlock();
        return byteBuffer;
    }

    public void complete(int order) {

        mCompleteOrder = order;
    }
}
