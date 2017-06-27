package cn.xujifa.readhugefile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by xujifa on 17-6-26.
 */
public class Writer implements Runnable {
    private static final String TEMP_FILE = "temp";

    private FileChannel mChannel;
    private Cache mCache;
    private String mResFile;

    public Writer(Cache cache, String resFile) {

        mResFile = resFile;
        File file = new File(TEMP_FILE);
        try {
            if (file.exists()) {
                file.delete();
            }
            if (!file.createNewFile()) {
                return;
            }
            mChannel = new FileOutputStream(file).getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCache = cache;
    }



    @Override
    public void run() {

        boolean isCompelte = false;
        while (!isCompelte) {
            try {
                ByteBuffer byteBuffer = mCache.take();
                if (byteBuffer == null) {
                    isCompelte = true;
                } else {

                    mChannel.write(byteBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            mChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        replaceFile();

    }

    private void replaceFile() {
        File res = new File(mResFile);
        File temp = new File(TEMP_FILE);
        if (!res.delete()) {
            return;
        }
        temp.renameTo(res);
        System.out.println("all complete");
    }
}
