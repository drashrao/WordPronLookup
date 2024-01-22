package com.example.tfbapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import com.app.wordpron.R;

public class MainActivity extends Activity {
	protected static Context sContext;
	boolean doFileInput;
	private Button b1;
	private EditText edittext1;
	Thread audio_thread;
	AudioTask audio;
	LinkedBlockingQueue<short[]> audioq;
	protected static LinkedBlockingQueue<Integer> audioControlq;
	protected static LinkedBlockingQueue<Integer> audioControlqR;
	LinkedBlockingQueue<short[]> q;
	int block_size;
	FileInput fileIn;
	TextFileReadWrite tFRW;
	// DTFBFx dtf;
	TFBFxFl dtf;
	File dir;
	private String logDir;
	// int nBlocksDiscard;
	int fileId;
	TextToSpeech tts = null;
	DspFunctions dFn = new DspFunctions();
	VoxFileInOut voxF = new VoxFileInOut();
	String wordIn = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//voxF.getReducedDic();// do this to get reduced dictionary
		voxF.run();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		sContext = getApplicationContext();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		b1 = (Button) findViewById(R.id.button1);
		edittext1 = (EditText) findViewById(R.id.edittext_id);
		debug("OnCreateOptionsMenu:doFileInput:" + doFileInput);
		dir = Environment.getExternalStorageDirectory();
		q = new LinkedBlockingQueue<short[]>();
		tFRW = new TextFileReadWrite();
		logDir = "/Android/data/com.TW.TFBApp/log/";
		// nBlocksDiscard = 0;
		fileId = 0;
		doFileInput = false;
		try {
			dtf = new TFBFxFl();
			dtf.reset(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		debug("dtf formed:" + dtf);
		fileIn = new FileInput();
		audioq = new LinkedBlockingQueue<short[]>();
		if (!doFileInput) {
			this.audio = new AudioTask(this.audioq, 1024, dtf);
			this.audio_thread = new Thread(this.audio);
			this.audio_thread.start();
		}
		b1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					String eText = edittext1.getText().toString();
					debug("eText:" + eText);
					// Toast.makeText(MainActivity.this, edittext.getText(),
					// Toast.LENGTH_LONG).show();
					wordIn = edittext1.getText().toString().trim();
					wordIn = wordIn.toUpperCase();
					Toast.makeText(MainActivity.this, wordIn + "-" + voxF.hMDic.get(wordIn), Toast.LENGTH_LONG).show();
					debug8("voxF.hMDic:" + voxF.hMDic);
					Log.d("Pressed", "Button pressed");
					if (!doFileInput) {
						// audio.stop1();
						b1.setText("Listening...");
						audio.reset();// added for audio prob
						audioq.clear();
						debug2("audio:" + audio.q.size());
					} else {
						CharSequence cM = "Reading File...";
						Toast msg = Toast.makeText(getBaseContext(), cM, Toast.LENGTH_SHORT);
						msg.show();
					}
					try {
						dtf.reset(true);
						// dtf.init(true);
						debug2("dtf was reset:" + dtf.pOut.size());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (doFileInput) {
						dtf.doPreemp = false;
						fileIn.init(1024, dtf);
						fileIn.run();
					} else {
						// nBlocksDiscard = 0;
						audio.done = false;
						// audioq.clear();
						audio.start();
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.d("Released", "Button released");
					boolean tryTts = false;
					if (tryTts) {
						// File fTTS = new File(dir + filesDir + "fTts" + ".wav");
						tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
							@Override
							public void onInit(int status) {
								debug("TTS Status:" + status + " ErrorStatus:" + TextToSpeech.ERROR);
								if (status != TextToSpeech.ERROR) {
									boolean setUnMute = true;
									if (setUnMute) {
										AudioManager audioManager;
										audioManager = (AudioManager) getApplicationContext()
												.getSystemService(Context.AUDIO_SERVICE);
										audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
												AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
									}
									tts.setLanguage(Locale.US);
									// tts.setPitch(pitch);
									// tts.setSpeechRate(speechRate);
									// CharSequence charSeq = "This is the TTS playing";
									CharSequence charSeq = "Aashaa";
									tts.speak(charSeq, TextToSpeech.QUEUE_FLUSH, null, "0");
									// tts.synthesizeToFile(charSeq, null, fTTS, "0");
								}
							}
						});
					}
					if (!doFileInput) {
						boolean doSleep = false;
						if (doSleep) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						// audio.done = true;
						audio.reset();// added for audio prob
						audioq.clear();
						audio.stop();
					} else {
						while (true) {
							if (fileIn.done) {
								CharSequence cM = "Processed File...";
								Toast msg = Toast.makeText(getBaseContext(), cM, Toast.LENGTH_SHORT);
								msg.show();
								break;
							}
						}
					}
					if (!doFileInput) {
						b1.setText("Get Pronunciation");
					}
					// CharSequence cM1 = "time:" + dtf.tArray.get(out[0]);
					// debug("dtf time:" + dtf.tArray);
					// debug("dtf freq:" + dtf.fArray);
					// debug("dtf Amp:" + dtf.aArray);
				}
				return false;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			audio.done = true;
			audio.stop();
			this.finishAffinity();
			System.exit(0);
			return true;
		} else if (id == R.id.action_fileinput) {
			if (doFileInput) {
				doFileInput = false;
				GraphData.plotSignalInsteadOfFreq = false;
				GraphData.plotDftMagInsteadOfFreq = false;
				CharSequence cM = "File Input OFF";
				Toast msg = Toast.makeText(getBaseContext(), cM, Toast.LENGTH_SHORT);
				msg.show();
			} else {
				doFileInput = true;
				fileIn = new FileInput();
				CharSequence cM = "File Input ON";
				Toast msg = Toast.makeText(getBaseContext(), cM, Toast.LENGTH_SHORT);
				msg.show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		Log.d("Pressed", "Back pressed");
		CharSequence cM = "Back Pressed";
		Toast msg = Toast.makeText(getBaseContext(), cM, Toast.LENGTH_SHORT);
		msg.show();
	}

	private void writeToWav(TFBFxFl dtfb, int bou, int eou, int fileIdIn) {
		// default writes to /Android/data/com.TW.TFBApp/log/data/
		WavWriter wW = new WavWriter();
		wW.writeToWav(dir, fileIdIn, dtfb, bou, eou);
	}

	private void debug(String message) {
		// System.out.println("dM0:" + message);
	}

	private void debug1(String message) {
		// System.out.println("dM0:" + message);
	}

	private void debug2(String message) {
		// System.out.println("dM2:" + message);
	}

	private void debug3(String message) {
		// System.out.println("dM3:" + message);
	}

	private void debug4(String message) {
		// System.out.println("dM4:" + message);
	}

	private void debug5(String message) {
		// System.out.println("dM5:" + message);
	}

	private void debug6(String message) {
		// System.out.println("dM6:" + message);
	}

	private void debug7(String message) {
		// System.out.println("dM7:" + message);
	}

	private void debug8(String message) {
		System.out.println("dM8:" + message);
	}
}
