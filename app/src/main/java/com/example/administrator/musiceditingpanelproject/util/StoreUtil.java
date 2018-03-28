package com.example.administrator.musiceditingpanelproject.util;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.example.administrator.musiceditingpanelproject.bean.MusicBean;
import com.example.administrator.musiceditingpanelproject.bean.MusicGroup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * 缓存工具类，用于缓存音乐列表、缓存音乐文件、读取音乐列表、整理缓存、删除缓存
 * Edited by Administrator on 2018/3/25.
 */

public class StoreUtil {

    // 分隔符
    private static final String DELIMITER = "@#";
    // 缓存文件夹名
    private static final String CACHE_FOLDER = "/Shelter";
    // 缓存音乐列表文件夹名
    private static final String CACHE_LIST_FOLDER = "/Shelter/ListCache";
    // 缓存音乐列表文件名
    private static final String CACHE_LIST_FILE = "cache";

    /**
     * 整理缓存
     * <p>
     * 音乐文件的每个缓存文件名为：版本名+“@#”+原文件名
     * 这里拿到musicGroups中的所有项，将每个项的版本名+“@#”+原文件名，然后丢到HashSet中
     * 遍历所有缓存文件，在HashSet中寻找是否存在该缓存文件的文件名，如果不存在则说明该文件被淘汰了，应该将该文件删除
     * <p>
     * 注：即使文件的原文件名与musicGroups中的某一项原文件名相同，但版本号不同，只可能是musicGroups中的项版本被更新
     * 这样造成拼接起来的文件名不同，这样文件也应该要被删除
     *
     * @param musicGroups 音频信息组列表
     */
    public static void sortOutCache(ArrayList<MusicGroup> musicGroups) {
        HashSet<String> cacheFileNameSet = new HashSet<>(musicGroups.size());
        // 遍历musicGroups中所有MusicBean将对应的version+"@#"+filename丢进HashSet中
        for (MusicGroup musicGroup : musicGroups) {
            ArrayList<MusicBean> musicBeans = musicGroup.getMusicBeans();
            for (MusicBean musicBean : musicBeans) {
                cacheFileNameSet.add(getCacheFilename(musicBean.getVersion(), getFileName(musicBean.getUrl())));
            }
        }
        File folder = new File(getCacheFolderDir());
        if (!folder.exists() && !folder.mkdir()) {
            return;
        }
        // 获得该文件夹内的所有文件
        File[] files = folder.listFiles();
        for (File file : files) {
            if (!file.isFile()) continue;
            // 在HashSet中找是否存在该文件名
            if (!cacheFileNameSet.contains(file.getName())) {
                file.delete();
            }
        }
    }

    /**
     * 删除缓存
     *
     * @param version  版本号
     * @param filename 原文件名
     * @return 是否成功
     */
    public static boolean deleteCache(@NonNull String version, @NonNull String filename) {
        File file = new File(getCacheFileAbsolutePath(version, filename));
        // 如果不存在文件，返回成功，如果存在，则返回是否删除成功
        return !file.exists() || file.delete();
    }

    /**
     * 寻找有没有对应的缓存文件
     *
     * @param version  版本号
     * @param filename 文件名
     * @return 缓存文件
     */
    public static File findCacheFile(@NonNull String version, @NonNull String filename) {
        File file = new File(getCacheFileAbsolutePath(version, filename));
        if (file.exists()) return file;
        return null;
    }

    /**
     * 获得所有缓存文件名，用HashSet便于访问
     */
    public static HashSet<String> getAllCacheFileName() {
        File folder = new File(getCacheFolderDir());
        if (!folder.exists() || !folder.isDirectory()) return null;
        String[] filenames = folder.list();
        HashSet<String> hashSet = new HashSet<>(filenames.length);
        hashSet.addAll(Arrays.asList(filenames));
        return hashSet;
    }


    /**
     * 缓存音频文件
     *
     * @param musicByte 音频文件的byte数组
     * @param version   版本号
     * @param filename  原文件名
     * @return 是否成功
     */
    public static boolean cacheMusicFile(byte[] musicByte, String version, String filename) {
        File file = new File(getCacheFileAbsolutePathTemp(version, filename));
//        File file = new File(MusicEditingPanelApplication.getApplication().getCacheDir().getAbsolutePath(), filename);
        // 如果存在文件，但是删不掉
        if (file.exists() && !file.delete()) {
            return false;
        }
        // 缓存
        BufferedOutputStream bufferedOutputStream = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(musicByte);
            bufferedOutputStream.flush();
            File renameFile = new File(getCacheFileAbsolutePath(version,filename));
            if (!renameFile.exists()) {
                file.renameTo(renameFile);
                return true;
            }else {
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 缓存音频列表
     *
     * @param musicGroups 音频信息组列表
     * @return 是否成功
     */
    public static boolean cacheMusicList(ArrayList<MusicGroup> musicGroups) {
        File folder = new File(getListCacheFolderDir());
        // 如果不存在也不能创建路径
        if (!folder.exists() && !folder.mkdir()) {
            return false;
        }
        File file = new File(getListCacheFileAbsolutePath());
        if (file.exists()) {
            // 如果删除不了
            if (!file.delete()) {
                return false;
            }
        } else {
            try {
                // 如果创建不了文件
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 缓存
        ObjectOutputStream objectOutputStream = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
            objectOutputStream.writeObject(musicGroups);
            objectOutputStream.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取缓存的音频信息组列表
     *
     * @return 是否成功
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<MusicGroup> readMusicList() {
        File file = new File(getListCacheFileAbsolutePath());
        // 如果文件不存在
        if (!file.exists()) {
            return null;
        }
        // 缓存
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            return (ArrayList<MusicGroup>) object;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 拿到网络url中的文件名
     *
     * @param url 网络url
     * @return 文件名
     */
    public static String getFileName(String url) {
        // “/”为分隔符
        String[] strings = url.split("/");
        if (strings.length == 0) {
            return "";
        }
        // 返回最后一个string
        return strings[strings.length - 1];
    }

    /**
     * 文件名转为加上版本号的缓存文件名
     */
    public static String getCacheFilename(String version, String filename) {
        return version + DELIMITER + filename;
    }

    private static String getTempCacheFilename(String version, String filename) {
        return version + DELIMITER + filename + ".temp";
    }

    public static String getCacheFileAbsolutePath(String version, String filename) {
        return getCacheFolderDir() + "/" + getCacheFilename(version, filename);
    }

    private static String getCacheFileAbsolutePathTemp(String version, String filename) {
        return getCacheFolderDir() + "/" + getTempCacheFilename(version, filename);
    }

    private static String getCacheFolderDir() {
        return Environment.getExternalStorageDirectory() + CACHE_FOLDER;
    }

    private static String getListCacheFolderDir() {
        return Environment.getExternalStorageDirectory() + CACHE_LIST_FOLDER;
    }

    private static String getListCacheFileAbsolutePath() {
        return getListCacheFolderDir() + "/" + CACHE_LIST_FILE;
    }
}
