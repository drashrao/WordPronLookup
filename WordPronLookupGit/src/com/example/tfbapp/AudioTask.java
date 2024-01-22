package com.example.tfbapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioTask implements Runnable {
	// DTFBFx dtf;
	TFBFxFl dtf;
	LinkedBlockingQueue<short[]> q;
	int n_audiou;
	int n_audiouForReset;
	int n_audiouForResetMax = 240000;// half minute
	ArrayList<Integer> audiou;
	AudioRecord rec;
	int block_size;
	boolean done;
	boolean endActivity = false;
	static final int DEFAULT_BLOCK_SIZE = 512;
	int nBlocksDiscard = 0;
	LinkedBlockingQueue<Integer> cQ;
	LinkedBlockingQueue<Integer> cQR;

	AudioTask(LinkedBlockingQueue<short[]> q, int block_size, TFBFxFl dtf) {
		this.init(q, block_size, dtf);
	}

	void init(LinkedBlockingQueue<short[]> q, int block_size, TFBFxFl dtf) {
		debug("audio: initialized");
		this.done = true;
		this.q = q;
		this.block_size = block_size;
		this.rec = null;
		int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
		int BytesPerElement = 2;
		// int bufferSizeInBytes = BufferElements2Rec * BytesPerElement;
		// above was fixed at 8192 earlier
		int bufferSizeInBytes = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		debug1("bufferSizeInBytes:" + bufferSizeInBytes);
		this.rec = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 8000, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
		audiou = new ArrayList<Integer>();
		this.dtf = dtf;
	}

	public int getBlockSize() {
		return block_size;
	}

	public void setBlockSize(int block_size) {
		this.block_size = block_size;
	}

	public LinkedBlockingQueue<short[]> getQueue() {
		return q;
	}

	public void stop() {
		if (!done) {
			this.rec.stop();
			done = true;
			debug("audio: stopped");
		}
	}

	public void reset() {
		this.done = true;
		q.clear();
		// audiou.clear();
		this.audiou = new ArrayList<Integer>();
		n_audiou = 0;
		n_audiouForReset = 0;
	}

	public void stop1() {
		this.rec.stop();
		n_audiou = 0;
		this.audiou = new ArrayList<Integer>();
		n_audiouForReset = 0;
		this.q.clear();
		debug("audio: stopped");
	}

	public void pause() {
		this.done = true;
		debug("audio: paused");
	}

	public void start() {
		if (done) {
			this.rec.startRecording();
			done = false;
			debug("audio: started");
		}
	}

	public void run() {
		this.rec.startRecording();
		while (true) {
			// debug("audio: thread is running");
			if (!this.done) {
				int nshorts = -1;
				nshorts = this.readBlock();
				boolean breakOnEndOfFile = false;
				if (nshorts <= 0) {
					break;
				}
			}
		}
		// dtf.reset(false);
		this.rec.stop();
		this.rec.release();
	}

	int readBlock() {
		short[] buf = new short[this.block_size];
		int nshorts = this.rec.read(buf, 0, buf.length);
		// debug("nShorts:" + nshorts);
		if (nshorts > 0) {
			if (this.nBlocksDiscard < 3) {
				debug("Discarded audio block:" + nshorts);
				this.nBlocksDiscard++;
			} else {
				this.q.add(buf);
				// debug("buf:b0:" + buf[0] + " b1:" + buf[1]);
				boolean doDTF = true;
				if (doDTF && buf != null && q != null && q.size() > 0) {
					callTFB();
				}
			}
		}
		return nshorts;
	}

	public void process(LinkedBlockingQueue<short[]> audioq, TFBFxFl dtf) throws IOException, InterruptedException {
		// System.out.println("TIMING Started getting audio");
		short[] bufD = null;
		bufD = audioq.take();
		// debug("bufD:b0:" + bufD[0] + " b1:" + bufD[1] + " n:" + bufD.length);
		for (int i1 = 0; i1 < bufD.length; i1++) {
			// sampleNo = (this.n_audiou * blockSize) + i1;
			audiou.add((int) bufD[i1]);
			n_audiouForReset++;
			// checkAndResetAudioBuffer(dtf);
		}
		this.n_audiou++;
		// debug("bufD:b0:" + bufD[0] + " b1:" + bufD[1] + " n:" + bufD.length);
		dtf.genFeatures(bufD);
	}

	private boolean checkAndResetAudioBuffer(TFBFxFl dtf) throws IOException {
		if (n_audiouForReset >= n_audiouForResetMax) {
			n_audiou = 0;
			// audiou.clear();
			this.audiou = new ArrayList<Integer>();
			debug("Reset of audiou at:" + n_audiouForReset);
			n_audiouForReset = 0;
			debug("resetting dtf");
			dtf.reset(false);
			debug("reset dtf done");
			return true;
		}
		return false;
	}

	private void callTFB() {
		try {
			process(q, dtf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void debug(String message) {
		// System.out.println("d0:" + message);
	}

	private void debug1(String message) {
		// System.out.println("dAT1:" + message);
	}
}
