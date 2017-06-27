package cn.xujifa.readhugefile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xujifa on 17-6-26.
 */
public class Manager {

    private static final int THREAD_NUM = 8;

    private String mFile;
    private String mToSearch;
    private String mToReplace;
    private ExecutorService mService;
    private int mOrder;
    private Writer mWriter;
    private Cache mCache;

    public Manager(String file, String toSearch, String toReplace) {
        mFile = file;
        mToSearch = toSearch;
        mToReplace = toReplace;
        mService = Executors.newFixedThreadPool(THREAD_NUM);
        mCache = new Cache();
        mWriter = new Writer(mCache, file);
        new Thread(mWriter).start();
    }

    public void run() {
        replace(0);
    }

    public synchronized void replace(long start) {

        mService.execute(new Reader(start, end -> {
            if (end == Reader.EndIndexReceiver.REACH_END) {
                return;
            }
            this.replace(end + 1);
        }, mCache::complete, mCache, mToSearch, mToReplace, mFile, mOrder));

        mOrder++;
    }

}
