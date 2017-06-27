package cn.xujifa.readhugefile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;


/**
 * Created by xujifa on 17-6-25.
 */
public class Reader implements Runnable {

    private static final long SIZE = 10000;

    private long mStart;
    private EndIndexReceiver mEndReceiver;
    private CompleteReceiver mCompleteReceiver;
    private byte[] mSearchBytes;
    private byte[] mReplaceBytes;
    private String mFile;
    private Cache mCache;
    private int mOrder;


    public Reader(long start,
                  EndIndexReceiver endReceiver, CompleteReceiver completeReceiver,
                  Cache cache,
                  String searchStr, String replaceStr,
                  String file,
                  int order) {

        mStart = start;
        mEndReceiver = endReceiver;
        mCompleteReceiver = completeReceiver;
        mCache = cache;
        mSearchBytes = searchStr.getBytes();
        mReplaceBytes = replaceStr.getBytes();
        mFile = file;
        mOrder = order;
    }

    @Override
    public void run() {

        Path path = Paths.get(mFile);
        FileChannel channel = null;
        try {
            channel = (FileChannel) Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ));
            long size = mStart + SIZE > channel.size() ? channel.size() - mStart : SIZE;
            MappedByteBuffer mappedByteBuffer
                    = channel.map(FileChannel.MapMode.READ_ONLY, mStart, size);

            int position = getBreakPosition(size, mappedByteBuffer);
            int actualLength = position + 1;
            long end = mStart + position;
            if (end == channel.size() - 1) {
                end = EndIndexReceiver.REACH_END;
            }
            mEndReceiver.onEndIndexReceived(end);
            List<Integer> matchList = search(mappedByteBuffer, actualLength, mSearchBytes);
            ByteBuffer result = replace(actualLength, matchList, mappedByteBuffer);

            mCache.put(mOrder, result);
            if (end == EndIndexReceiver.REACH_END) {
                mCompleteReceiver.onComplete(mOrder);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获得分隔符的位置 '\n' | ' '
     * @param size
     * @param source
     */
    private int getBreakPosition(long size, MappedByteBuffer source) {

        boolean isMatchBreak = false;
        int position = (int) size - 1;
        while (!isMatchBreak && position >= 0) {
            char c = (char) source.get(position);

            if (c == ' ' || c == '\n') {
                isMatchBreak = true;
            } else {
                position--;
            }
        }
        if (position == -1) {
            position = (int) size - 1;
        }
        return position;
    }

    private ByteBuffer replace(int actualLength, List<Integer> matchList, MappedByteBuffer source) {


        int lengthAfterReplace = actualLength
                + matchList.size() * (mReplaceBytes.length - mSearchBytes.length);
        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthAfterReplace);

        int searchLength = mSearchBytes.length;
        source.rewind();


        for (int index : matchList) {
            source.limit(index);
            byteBuffer.put(source);

            byteBuffer.put(mReplaceBytes);
            source.limit(index + searchLength);
            source.position(index + searchLength);
        }
        source.limit(actualLength);
        byteBuffer.put(source);
        byteBuffer.flip();
        return byteBuffer;
    }

    private List<Integer> search(MappedByteBuffer buffer, int length, byte[] toSearch) {

        List<Integer> matchList = new ArrayList<>();

        int match = 0;
        int searchLength = toSearch.length;
        for (int i = 0; i < length; i++) {
            // TODO: replace with buffer.get()
            if (toSearch[match] == buffer.get(i)) {
                match++;
                if (match == searchLength) {
                    matchList.add(i - searchLength + 1);
                    match = 0;
                }
            } else {
                match = 0;
            }
        }
        return matchList;

    }


    public interface EndIndexReceiver {

        int REACH_END = -1;

        void onEndIndexReceived(long end);
    }

    public interface CompleteReceiver {
        void onComplete(int order);
    }
}
