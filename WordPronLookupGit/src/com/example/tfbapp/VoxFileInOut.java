package com.example.tfbapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import android.os.Environment;

public class VoxFileInOut {
	File dir;
	String dicDir;
	File dicFile;
	String logDir;
	LinkedHashMap<String, String> hMDic;

	public VoxFileInOut() {
		dir = Environment.getExternalStorageDirectory();
		dicDir = "/Android/data/com.TW.TFBApp/dic/";
		dicFile = new File(dir + dicDir + "cmudict-0.7b.txt");
		// dicFile = new File(dir + dicDir + "cleanmail.dic");
		// dicFile = new File(dir + dicDir + "VoxDic10.txt");
		logDir = "/Android/data/com.TW.TFBApp/log/output/";
		hMDic = new LinkedHashMap<String, String>();
	}

	protected void getReducedDic() {
		File dicFile1 = new File(dir + dicDir + "Vox10KWords.txt");
		File dicFileOut = new File(dir + dicDir + "VoxDic10.txt");
		ArrayList<String> aWords = new ArrayList<String>();
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(dicFileOut);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Scanner scanner = new Scanner(dicFile1);
			//
			int nWords = 0;
			while (scanner.hasNextLine()) {
				nWords++;
				String word = scanner.nextLine();
				// debug("word:" + word + ":END");
				aWords.add(word.trim());
				nWords++;
			}
			scanner.close();
			debug("nWords aWords:" + nWords);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// debug("aWords:" + aWords + " size:" + aWords.size());
		debug1("aWords size:" + aWords.size());
		try {
			Scanner scanner = new Scanner(dicFile);
			//
			int nWords = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				// debug("line:" + line + ":END");
				String[] tmp1 = line.split("\\s\\s+");
				// debug("tmp1Size:" + tmp1.length);
				if (tmp1.length > 1) {
					String word = tmp1[0].trim();
					String pron = tmp1[1];
					// debug("word:" + word + " pron:" + pron);
					if (aWords.contains(word)) {
						// write word to file
						String data = word + "  " + pron + "\n";
						try {
							stream.write(data.getBytes());
							nWords++;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						debug("not found word:" + word);
					}
				}
			}
			scanner.close();
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			debug1("nWords Written:" + nWords);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void run() {
		int dicType = 1;// cleanmail
		debug2("dicFile:" + dicFile);
		try {
			Scanner scanner = new Scanner(dicFile);
			//
			int nWords = 0;
			while (scanner.hasNextLine()) {
				nWords++;
				String line = scanner.nextLine();
				//debug2("line:" + line + ":END");
				if (dicType == 0) {
					if (!line.matches(";;")) {
						String[] tmp1 = line.split("\\s\\s+");
						// debug("tmp1Size:" + tmp1.length);
						String word = tmp1[0];
						String pron = tmp1[1];
						// debug("word:" + word + " pron:" + pron);
						hMDic.put(word, pron);
						nWords++;
					}
				} else if (dicType == 1) {
					String[] tmp1 = line.split("\\s\\s+");
					// debug("tmp1Size:" + tmp1.length);
					if (tmp1.length > 1) {
						String word = tmp1[0];
						String pron = tmp1[1];
						debug1("word:" + word + " pron:" + pron);
						hMDic.put(word, pron);
						nWords++;
					}
				}
			}
			scanner.close();
			debug("nWords:" + nWords);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// debug1("success:" + hMDic.get("success"));
		debug2("hMDic:" + hMDic);
	}

	private void debug(String message) {
		// System.out.println("dV0:" + message);
	}

	private void debug1(String message) {
		// System.out.println("dV1:" + message);
	}

	private void debug2(String message) {
		System.out.println("dV2:" + message);
	}
}
