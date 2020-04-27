package org.steps.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LoadExt {
    private static Logger logger = Logger.getLogger(LoadExt.class);
    private static ConcurrentHashMap jarMaps = new ConcurrentHashMap();
    public static List<String> loadExtLib(List<String> libs){
        List<String> success = new ArrayList<String>();
        for(String path : libs){
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {

                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
                addURL.setAccessible(true);
                File file = new File(path);
                List<String> paths = new ArrayList<String>();
                findJars(file,paths);//寻找路径下的jar包
                for(String jarPath : paths){
                    File jar = new File(jarPath);
                    FileInputStream inputStream = new FileInputStream(jar);
                    String jarHash = md5HashCode(inputStream);
                    if(!jarMaps.containsKey(jarHash)){
                        jarMaps.put(jarHash,jarPath);
                        URL url = jar.toURI().toURL();
                        addURL.invoke(cl, new Object[] { url });
                        success.add(jar.getName());
                        logger.info("成功加载："+jarPath);
                    }
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("加载kettle类库失败,原因");
            }
        }
        return success;
    }
    public static void findJars(File file,List<String> jarPaths){
        File[] files = file.listFiles();
        for(File a :files){
            String path = a.getPath();
            if(a.isFile() && (path.endsWith(".jar") || path.endsWith(".zip"))){
                jarPaths.add(path);
            }
            if(a.isDirectory()){
                findJars(a,jarPaths);
            }
        }
    }
    public static String md5HashCode(InputStream fis) {
        try {
            //拿到一个MD5转换器,如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
            MessageDigest md = MessageDigest.getInstance("MD5");

            //分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            fis.close();
            //转换并返回包含16个元素字节数组,返回数值范围为-128到127
            byte[] md5Bytes  = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);//1代表绝对值
            return bigInt.toString(16);//转换为16进制
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
