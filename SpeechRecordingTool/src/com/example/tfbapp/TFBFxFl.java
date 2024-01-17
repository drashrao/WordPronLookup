package com.example.tfbapp;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TFBFxFl {
	/*
	 * 
	 */
	private BufferedWriter fout;
	private FileOutputStream fout1;
	private String fileOut;
	ArrayList<Integer> tArray;// Store estimation timings

	private int dtfLogNumber = 0;
	protected boolean writeOutFeatures;
	protected ArrayList<Short> pOut;// for pitch estimation

	TFBFxFl(String file) throws IOException {
		fileOut = file;
		fout = new BufferedWriter(new FileWriter(fileOut + dtfLogNumber + ".xls"));
	}

	TFBFxFl() throws IOException {
	}

	public void genFeatures(short[] sk) throws IOException {
	}

	public void reset(boolean AllIncludingF0) throws IOException {
	}

	private void debug(String message) {
		// Keying.debug(message, getClass().getName());
	}
}
