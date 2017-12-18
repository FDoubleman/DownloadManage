package cn.wdcloud.download.downloadUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by fmm on 2017/12/18.
 */

public class IOUtil {
    public static void closeAll(Closeable... closeables){
        if(closeables == null){
            return;
        }
        for (Closeable closeable : closeables) {
            if(closeable!=null){
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
