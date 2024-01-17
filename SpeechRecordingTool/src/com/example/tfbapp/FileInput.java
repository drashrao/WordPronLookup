package com.example.tfbapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.speech.tts.TextToSpeech;

public class FileInput {
	TFBFxFl tfb;
	int block_size;
	File srcFile = new File("test.wav");
	File srcTxtFile = new File("test.txt");
	FileInputStream fileInputStream = null;
	File dir;
	boolean done = false;
	DspFunctions dFn = new DspFunctions();

	void init(int block_size, TFBFxFl tfbIn) {
		this.block_size = block_size;
		this.tfb = tfbIn;
		dir = Environment.getExternalStorageDirectory();
		done = false;
	}

	public void run() {
		String filesDir = "/Android/data/com.TW.TFBApp/log/data/dataFiles/";
		String base = "ashFig";// iyshiy
		debug("base:" + base + " ----------------- ");
		srcFile = new File(dir + filesDir + base + ".wav");
		try {
			fileInputStream = new FileInputStream(srcFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//// tfb.doPreemp = false;
		while (true) {
			int nshorts = -1;
			nshorts = this.readBufferFromFile();
			// debug("nShorts:" + nshorts);
			if (nshorts <= 0) {
				done = true;
				break;
			}
		}
		// debug("sF:" + srcFile);
		// tfb.reset(false);
		if (fileInputStream != null) {
			try {
				// debug("closing file:" + srcFile);
				fileInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void process(short[] bufD, TFBFxFl tfbIn) throws IOException, InterruptedException {
		tfbIn.genFeatures(bufD);
	}

	private int readBufferFromFile() {
		byte[] buf = new byte[2 * this.block_size];
		short[] shortArr = new short[this.block_size];
		int nShorts = -1;
		// debug("fileIpStream:" + fileInputStream);
		try {
			nShorts = fileInputStream.read(buf);
			if (nShorts > 0) {
				// debug("buf:b0:" + buf[0] + " b1:" + buf[1] + " lt:" + buf.length);
				int j = 0;
				for (int i = 0; i < buf.length - 1; i += 2) {
					byte[] bufI = new byte[2];
					bufI[0] = buf[i];
					bufI[1] = buf[i + 1];
					shortArr[j] = ByteBuffer.wrap(bufI).order(ByteOrder.LITTLE_ENDIAN).getShort();
					// removeHeader = false;
					j++;
				}
				if (buf != null) {
					callTFB(shortArr);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nShorts;
	}

	private void callTFB(short[] shortArr) {
		try {
			process(shortArr, tfb);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void debug(String message) {
		// System.out.println("dF0:" + message);
	}
}
