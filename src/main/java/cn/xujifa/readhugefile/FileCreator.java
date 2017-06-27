package cn.xujifa.readhugefile;

import java.io.*;
import java.util.Random;

/**
 * Created by xujifa on 17-6-10.
 */
public class FileCreator {

  public static void create() throws IOException {
    //Size in Gbs of my file that I want
    double wantedSize = Double.parseDouble(System.getProperty("size", "1"));

    Random random = new Random();
    File file = new File("test.txt");
    long start = System.currentTimeMillis();
    PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), false);
    int counter = 0;
    while (true) {
      String sep = "";
      for (int i = 0; i < 100; i++) {
        int number;
        if (i % 20 == 0) {

          number =  123;
        } else {

          number = random.nextInt(1000) + 1;
        }
        writer.print(sep);
        writer.print(number / 1e3);
        if (i % 10 == 0) {
          sep = " \n";
        } else {
          sep = " ";
        }
      }
      writer.println();
      //Check to see if the current size is what we want it to be
      if (++counter == 20000) {
        System.out.printf("Size: %.3f GB%n", file.length() / 1e9);
        if (file.length() >= wantedSize * 1e9) {
          writer.close();
          break;
        } else {
          counter = 0;
        }
      }
    }
    long time = System.currentTimeMillis() - start;
    System.out.printf("Took %.1f seconds to create a file of %.3f GB", time / 1e3, file.length() / 1e9);
  }

}
