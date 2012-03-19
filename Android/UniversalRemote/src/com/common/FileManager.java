package com.common;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

	/*
	 * Copys a raw file to phone storage.
	 */
	public static boolean saveAs(Context context, int ressound, String dir,
			String filename) {
		byte[] buffer = null;
		int size = 0;

		boolean exists = (new File(dir)).exists();
		if (!exists) {
			new File(dir).mkdirs();
		}

		exists = (new File(dir + "/" + filename)).exists();
		if (exists) {
			return true;
		}

		InputStream fIn = context.getResources().openRawResource(ressound);

		try {
			size = fIn.available();
			buffer = new byte[size];
			fIn.read(buffer);
			fIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

		FileOutputStream save;
		try {
			save = new FileOutputStream(dir + "/" + filename);
			save.write(buffer);
			save.flush();
			save.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

		return true;
	}
}
