package cn.xujifa.readhugefile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xujifa on 17-6-10.
 */
public class Main {

  public static void main(String[] args) throws IOException {
//    FileCreator.create();
      Manager manager = new Manager("test.txt", "123", "ab");
      manager.run();

  }
}
