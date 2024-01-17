package com.example.tfbapp;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class WavWriter {

	protected void writeToWav(File dir, int fileId, TFBFxFl dtfb, int bou, int eou) {
		boolean doDebug = false;
		if (doDebug) {
			bou = 0;
			eou = 16000;
		}
		short[] buffer = new short[eou - bou + 1];
		int j = 0;
		for (int i = bou; i <= eou; i++) {
			if (doDebug) {
				buffer[j] = (short) Math.cos(2 * Math.PI * (1000 / 8000) * j);
			} else {
				if (i < dtfb.pOut.size()) {
					buffer[j] = dtfb.pOut.get(i);
				} else {
					buffer[j] = 0;
				}
			}
			j++;
		}
		debug1("bou:" + bou + " eou:" + eou + " bufSize:" + buffer.length);
		writeWAV(dir, null, fileId, buffer);
	}

	protected void writeWAV(File dir, String logDir, int fileID, short[] buffer) {
		try {
			long mySubChunk1Size = 16;
			int myBitsPerSample = 16;
			int myFormat = 1;
			long myChannels = 1;
			long mySampleRate = 8000;
			long myByteRate = mySampleRate * myChannels * myBitsPerSample / 8;
			int myBlockAlign = (int) (myChannels * myBitsPerSample / 8);

			// byte[] clipData = null;// getBytesFromFile(fileToConvert);
			byte[] dataByte = getByteArrayFromShortArray(buffer);

			long myDataSize = dataByte.length;
			long myChunk2Size = myDataSize * myChannels * myBitsPerSample / 8;
			long myChunkSize = 36 + myChunk2Size;

			debug1("DataSize:" + myDataSize);
			OutputStream os;
			if (logDir == null) {
				logDir = "/Android/data/com.TW.TFBApp/log/";
			}
			os = new FileOutputStream(new File(dir + logDir + "data/" + "Rec" + fileID + ".wav"));
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream outFile = new DataOutputStream(bos);

			outFile.writeBytes("RIFF"); // 00 - RIFF
			outFile.write(intToByteArray((int) myChunkSize), 0, 4); // 04 - how big is the rest of this file?
			outFile.writeBytes("WAVE"); // 08 - WAVE
			outFile.writeBytes("fmt "); // 12 - fmt
			outFile.write(intToByteArray((int) mySubChunk1Size), 0, 4); // 16 - size of this chunk
			outFile.write(shortToByteArray((short) myFormat), 0, 2); // 20 - what is the audio format? 1 for PCM = Pulse
																		// Code Modulation
			outFile.write(shortToByteArray((short) myChannels), 0, 2); // 22 - mono or stereo? 1 or 2? (or 5 or ???)
			outFile.write(intToByteArray((int) mySampleRate), 0, 4); // 24 - samples per second (numbers per second)
			outFile.write(intToByteArray((int) myByteRate), 0, 4); // 28 - bytes per second
			outFile.write(shortToByteArray((short) myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all
																			// channels
			outFile.write(shortToByteArray((short) myBitsPerSample), 0, 2); // 34 - how many bits in a sample(number)?
																			// usually 16 or 24
			outFile.writeBytes("data"); // 36 - data
			outFile.write(intToByteArray((int) myDataSize), 0, 4); // 40 - how big is this data chunk
			outFile.write(dataByte); // 44 - the actual data itself - just a long string of numbers

			outFile.flush();
			outFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void writeWAVA(File dir, String logDir, int fileID, ArrayList<Short> aBuffer) {
		try {
			long mySubChunk1Size = 16;
			int myBitsPerSample = 16;
			int myFormat = 1;
			long myChannels = 1;
			long mySampleRate = 8000;
			long myByteRate = mySampleRate * myChannels * myBitsPerSample / 8;
			int myBlockAlign = (int) (myChannels * myBitsPerSample / 8);

			short[] buffer = new short[aBuffer.size()];
			for (int i = 0; i < aBuffer.size(); i++) {
				buffer[i] = aBuffer.get(i);
			}
			// byte[] clipData = null;// getBytesFromFile(fileToConvert);
			byte[] dataByte = getByteArrayFromShortArray(buffer);

			long myDataSize = dataByte.length;
			long myChunk2Size = myDataSize * myChannels * myBitsPerSample / 8;
			long myChunkSize = 36 + myChunk2Size;

			debug1("DataSize:" + myDataSize);
			OutputStream os;
			if (logDir == null) {
				logDir = "/Android/data/com.TW.TFBApp/log/";
			}
			os = new FileOutputStream(new File(dir + logDir + "Rec" + fileID + ".wav"));
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream outFile = new DataOutputStream(bos);

			outFile.writeBytes("RIFF"); // 00 - RIFF
			outFile.write(intToByteArray((int) myChunkSize), 0, 4); // 04 - how big is the rest of this file?
			outFile.writeBytes("WAVE"); // 08 - WAVE
			outFile.writeBytes("fmt "); // 12 - fmt
			outFile.write(intToByteArray((int) mySubChunk1Size), 0, 4); // 16 - size of this chunk
			outFile.write(shortToByteArray((short) myFormat), 0, 2); // 20 - what is the audio format? 1 for PCM = Pulse
																		// Code Modulation
			outFile.write(shortToByteArray((short) myChannels), 0, 2); // 22 - mono or stereo? 1 or 2? (or 5 or ???)
			outFile.write(intToByteArray((int) mySampleRate), 0, 4); // 24 - samples per second (numbers per second)
			outFile.write(intToByteArray((int) myByteRate), 0, 4); // 28 - bytes per second
			outFile.write(shortToByteArray((short) myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all
																			// channels
			outFile.write(shortToByteArray((short) myBitsPerSample), 0, 2); // 34 - how many bits in a sample(number)?
																			// usually 16 or 24
			outFile.writeBytes("data"); // 36 - data
			outFile.write(intToByteArray((int) myDataSize), 0, 4); // 40 - how big is this data chunk
			outFile.write(dataByte); // 44 - the actual data itself - just a long string of numbers

			outFile.flush();
			outFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private byte[] intToByteArray(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);
		return b;
	}

	// convert a short to a byte array
	private byte[] shortToByteArray(short data) {
		/*
		 * NB have also tried: return new byte[]{(byte)(data & 0xff),(byte)((data >> 8)
		 * & 0xff)};
		 * 
		 */

		// return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
		return new byte[] { (byte) (data & 0xff), (byte) ((data >> 8) & 0xff) };
	}

	private byte[] getByteArrayFromShortArray(short[] buffer) {
		int N = buffer.length;
		ByteBuffer byteBuf = ByteBuffer.allocate(2 * N);
		for (short s : buffer) {
			// byteBuf.putShort(s);
			byteBuf.put(shortToByteArray(s));
		}
		/*
		 * int i = 0; while (N >= i) { byteBuf.putShort(buffer[i]); i++; }
		 */
		byte[] out = byteBuf.array();
		return out;
	}

	private void debug1(String message) {
		// System.out.println("d1:" + message);
	}
}
