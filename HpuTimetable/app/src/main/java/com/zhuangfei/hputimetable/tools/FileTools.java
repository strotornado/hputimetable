package com.zhuangfei.hputimetable.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import android.graphics.Bitmap;
import android.os.Environment;

/**
 * @author Administrator
 * 
 */
public class FileTools {

	public static final String ROOT = "/zfapp";

	public static String SOFT = "/hpuTimetable";

	public static String IMAGE = "/image";
	public static String TIME_TABLE = "/timtable";
	public static String LOG = "/log";

	public static File saveViewImage(Bitmap bitmap, String fileName) {
		File file = new File(getDir(IMAGE), fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private static boolean checkSdCard() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}

	private static String getSdCard() {
		return Environment.getExternalStorageDirectory() + "/";
	}

	public static String getDir(String dir) {
		String dirPath = getSdCard() + ROOT + SOFT + "/" + dir;
		File file = new File(dirPath);
		if (!file.exists())
			file.mkdirs();
		return dirPath;
	}

	public static void createFileDir(String fileDir) {
		String path = getDir(fileDir);
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();
	}

	public static void writeTimetable(String name, String roomInfo) {
		File file = new File(getDir(TIME_TABLE) + "/" + name +".txt");
		try {
			PrintStream ps = new PrintStream(file);
			ps.println(roomInfo);
			ps.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String readTimetable(String name) {
		File file = new File(getDir(TIME_TABLE) + "/" + name + ".txt");
		String result = "", line = "";
		try {
			if (!file.exists())
				file.createNewFile();
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isd = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isd);
			while ((line = reader.readLine()) != null) {
				result += "\n" + line;
			}
			reader.close();
			isd.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
