package com.example.tfbapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.app.wordpron.R;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphData extends Activity implements View.OnClickListener {
	/*
	 * Cleaned ... for old one refer to TFB9
	 */
	private boolean outputA3MinusA0 = false;
	private boolean scaleTimeAxis = true;
	private boolean useDftMagAxis = true;
	private boolean usevistaDbg1Axis = true;
	private boolean usevistaDbg2Axis = true;
	private boolean scaleToUtt = true;
	private boolean doMelConversion = true;
	private boolean doMelConversionOfLines = true;
	protected static boolean plotMFB = false;
	protected static boolean plotDftMagInsteadOfFreq = false;// set in pulse.java
	protected static boolean plotSignalInsteadOfFreq = false;
	protected static boolean plotVistaDebugInsteadOfAmp = true;
	protected static boolean plotDftMagInsteadOfPitch = false;// set in pulse.java
	private boolean plot_entire_pitch = true;
	private boolean usePIndicator = true;
	private boolean plot_pitch_feats = false;
	private int pad = 200;
	private int plot_pitch_feat_type = 9;
	private boolean doAmpDB = true;
	private boolean plotBwtAlongWithFreq = true;
	private boolean plotPitchOnSecondGraph = false;// true for icassp paper figure
	private boolean makeGraphBackgroundWhiteForPitch = false;// true for icassp
	private boolean makeGraphBackgroundWhite = false;
	DspFunctions dFn = new DspFunctions();

	/** Called when the activity is first created. */
	@SuppressLint("UseSparseArrays")
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug2("GraphData started...");
		setContentView(R.layout.graphs1);
		doMelConversionOfLines = false;
		if (plotDftMagInsteadOfPitch || plotDftMagInsteadOfFreq || plotSignalInsteadOfFreq
				|| plotVistaDebugInsteadOfAmp) {
			scaleTimeAxis = false;
		}
		// set the color of title bar
		boolean doViewBackgroundColor = false;
		if (doViewBackgroundColor) {
			View title = getWindow().findViewById(android.R.id.title);
			View titleBar = (View) title.getParent();
			titleBar.setBackgroundColor(getResources().getColor(R.color.blue, null));
		}

		Intent intent = getIntent();
		Bundle bun = intent.getBundleExtra("bundle");
		debug("bundle:" + bun);
		int M = intent.getExtras().getInt("M");
		int bouIndex = intent.getExtras().getInt("bouIndex");
		int eouIndex = intent.getExtras().getInt("eouIndex");
		int finalBou = intent.getExtras().getInt("finalBou");
		int finalEou = intent.getExtras().getInt("finalEou");
		// plotDftMag = intent.getExtras().getBoolean("plotDftMag");
		ArrayList<ArrayList<Integer>> amp = (ArrayList<ArrayList<Integer>>) bun.getSerializable("amp");
		ArrayList<Integer> time = (ArrayList<Integer>) bun.getSerializable("time");
		ArrayList<ArrayList<Integer>> freq = (ArrayList<ArrayList<Integer>>) bun.getSerializable("freq");
		ArrayList<ArrayList<Integer>> hFreq = (ArrayList<ArrayList<Integer>>) bun.getSerializable("hFreq");
		HashMap<Integer, Integer> pInd = (HashMap<Integer, Integer>) bun.getSerializable("pInd");
		HashMap<Integer, Integer> pAvg = (HashMap<Integer, Integer>) bun.getSerializable("pAvg");
		HashMap<Integer, ArrayList<Integer>> pSBD = (HashMap<Integer, ArrayList<Integer>>) bun.getSerializable("pSBD");
		ArrayList<Integer> pChn0 = (ArrayList<Integer>) bun.getSerializable("pChn0");
		ArrayList<Integer> pChn1 = (ArrayList<Integer>) bun.getSerializable("pChn1");
		ArrayList<Integer> pChn2 = (ArrayList<Integer>) bun.getSerializable("pChn2");
		ArrayList<Integer> pChn3 = (ArrayList<Integer>) bun.getSerializable("pChn3");
		ArrayList<ArrayList<Integer>> hFreq0 = (ArrayList<ArrayList<Integer>>) bun.getSerializable("hFreq0");
		ArrayList<Integer> hTime0 = (ArrayList<Integer>) bun.getSerializable("hTime0");
		ArrayList<ArrayList<Integer>> hFreq1 = (ArrayList<ArrayList<Integer>>) bun.getSerializable("hFreq1");
		ArrayList<Integer> hTime1 = (ArrayList<Integer>) bun.getSerializable("hTime1");
		ArrayList<ArrayList<Integer>> hFreq2 = (ArrayList<ArrayList<Integer>>) bun.getSerializable("hFreq2");
		ArrayList<Integer> hTime2 = (ArrayList<Integer>) bun.getSerializable("hTime2");
		ArrayList<ArrayList<Integer>> hFreq3 = (ArrayList<ArrayList<Integer>>) bun.getSerializable("hFreq3");
		ArrayList<Integer> hTime3 = (ArrayList<Integer>) bun.getSerializable("hTime3");
		ArrayList<ArrayList<Float>> bwt = (ArrayList<ArrayList<Float>>) bun.getSerializable("bwt");
		ArrayList<ArrayList<Integer>> bwtMel = (ArrayList<ArrayList<Integer>>) bun.getSerializable("bwtMel");
		ArrayList<Float> dftMag = (ArrayList<Float>) bun.getSerializable("dftMag");
		ArrayList<Integer> dftMagAxis = (ArrayList<Integer>) bun.getSerializable("dftMagAxis");
		ArrayList<Float> vistaDbg1 = (ArrayList<Float>) bun.getSerializable("vistaDbg1");
		ArrayList<Integer> vistaDbg1Axis = (ArrayList<Integer>) bun.getSerializable("vistaDbg1Axis");
		ArrayList<Float> vistaDbg2 = (ArrayList<Float>) bun.getSerializable("vistaDbg2");
		ArrayList<Integer> vistaDbg2Axis = (ArrayList<Integer>) bun.getSerializable("vistaDbg2Axis");
		ArrayList<ArrayList<Integer>> melFB = (ArrayList<ArrayList<Integer>>) bun.getSerializable("melFB");
		ArrayList<Integer> melFBAxis = (ArrayList<Integer>) bun.getSerializable("melFBAxis");
		ArrayList<Float> melFBSpec = (ArrayList<Float>) bun.getSerializable("melFBSpec");
		ArrayList<Short> signal = (ArrayList<Short>) bun.getSerializable("signal");
		int[] aLevels = (int[]) bun.getSerializable("aLevels");
		int a3MinusA0Med = intent.getExtras().getInt("a3MinusA0Med");
		ArrayList<ArrayList<Integer>> pitch = (ArrayList<ArrayList<Integer>>) bun.getSerializable("pitch");
		ArrayList<Integer> timePitch = (ArrayList<Integer>) bun.getSerializable("timePitch");
		ArrayList<String> pitchSBands = (ArrayList<String>) bun.getSerializable("pitchSBands");
		ArrayList<ArrayList<Integer>> pitchProb = (ArrayList<ArrayList<Integer>>) bun.getSerializable("pitchProb");
		ArrayList<ArrayList<Integer>> subbandPitch = (ArrayList<ArrayList<Integer>>) bun
				.getSerializable("subbandPitch");
		ArrayList<ArrayList<Integer>> pitchEnergy = (ArrayList<ArrayList<Integer>>) bun.getSerializable("pitchEnergy");
		ArrayList<ArrayList<Integer>> pitchBwt = (ArrayList<ArrayList<Integer>>) bun.getSerializable("pitchBwt");
		ArrayList<ArrayList<Integer>> specBwt = (ArrayList<ArrayList<Integer>>) bun.getSerializable("specBwt");
		ArrayList<ArrayList<Integer>> aTotEnergy = (ArrayList<ArrayList<Integer>>) bun.getSerializable("totEnergy");
		ArrayList<ArrayList<Integer>> aFreqCentroid = (ArrayList<ArrayList<Integer>>) bun
				.getSerializable("freqCentroid");
		ArrayList<ArrayList<Integer>> aMaxEnergy = (ArrayList<ArrayList<Integer>>) bun.getSerializable("maxEnergy");
		ArrayList<ArrayList<Integer>> aFreqMaxEnergy = (ArrayList<ArrayList<Integer>>) bun
				.getSerializable("freqMaxEnergy");
		ArrayList<Integer> nZerosInNasalBand = (ArrayList<Integer>) bun.getSerializable("nZerosInNasalBand");
		ArrayList<Integer> antiformant = (ArrayList<Integer>) bun.getSerializable("antiformant");
		ArrayList<Integer> nAntiformants = (ArrayList<Integer>) bun.getSerializable("nAntiformants");
		ArrayList<Integer> subbandMaxProb5 = (ArrayList<Integer>) bun.getSerializable("subbandMaxProb5");
		ArrayList<Integer> subbandMaxProbL = (ArrayList<Integer>) bun.getSerializable("subbandMaxProbL");
		ArrayList<Integer> hfMultiplePMatch = (ArrayList<Integer>) bun.getSerializable("hfMultiplePMatch");
		ArrayList<Integer> nazFeat = (ArrayList<Integer>) bun.getSerializable("nazFeat");
		ArrayList<Integer> zFeat = (ArrayList<Integer>) bun.getSerializable("zFeat");
		ArrayList<Integer> zHFeat = (ArrayList<Integer>) bun.getSerializable("zHFeat");
		ArrayList<Integer> weakFricFeat = (ArrayList<Integer>) bun.getSerializable("wFFeat");
		ArrayList<ArrayList<Integer>> pitchRaw = null;//new ArrayList<ArrayList<Integer>>(pitch);
		ArrayList<Integer> aPitch = (ArrayList<Integer>) bun.getSerializable("isPitch");
		ArrayList<Integer> aPitchSmooth = (ArrayList<Integer>) bun.getSerializable("aPitchSmooth");
		ArrayList<Integer> aFinalPitchHz = (ArrayList<Integer>) bun.getSerializable("finalPitchHz");
		debug2("GraphData 1");
		debug("timePitch:" + timePitch);
		debug("pitch:" + pitch);
		debug("pitchRaw:" + pitchRaw);
		debug("pitchSBands:" + pitchSBands);
		if (pitchRaw == null || pitchRaw.get(0).size() == 0 || pitchSBands == null || pitchSBands.size() == 0) {
			plot_entire_pitch = false;
		}
		int num = amp.get(0).size();
		ArrayList<ArrayList<Integer>> bwtInt = new ArrayList<ArrayList<Integer>>();
		if (doAmpDB) {
			getAllAmpsDB(amp, M, num, aLevels, a3MinusA0Med);
		}
		boolean doBwtInt = true;
		if (doBwtInt) {
			bwtInt = getAllBwsInt(bwt, M, num);
		}
		debug("GraphData: M: " + M + " num:" + num + " ampSize:" + amp.get(0).size() + " timeSize:" + time.size());
		debug("GraphData:time:size:" + time.size() + " time:" + time);
		GraphViewData[] data = null;
		LineGraphView graphView;
		LinearLayout layout;
		////////////////////////////// GRAPH 1 ////////////////////////////////
		// amp (first graph)
		graphView = new LineGraphView(this, "");
		if (plotVistaDebugInsteadOfAmp && vistaDbg1 != null && vistaDbg1.size() > 1) {
			int nFFT = vistaDbg1.size();
			debug4("vistaDbg1:" + vistaDbg1);
			data = new GraphViewData[nFFT];
			for (int j = 0; j < nFFT; j++) {
				if (usevistaDbg1Axis && vistaDbg1Axis != null && vistaDbg1Axis.size() == nFFT) {
					data[j] = new GraphViewData(vistaDbg1Axis.get(j), vistaDbg1.get(j));
				} else {
					data[j] = new GraphViewData(j, vistaDbg1.get(j));
				}
			}
			GraphViewSeries series0 = new GraphViewSeries("vistaDbg1",
					new GraphViewSeriesStyle(Color.rgb(255, 255, 255), 1), data);
			graphView.addSeries(series0);
			if (!usevistaDbg1Axis || vistaDbg1Axis == null || vistaDbg1Axis.size() != nFFT) {
				graphView.setViewPort(0, nFFT);
			} else {
				graphView.setViewPort(vistaDbg1Axis.get(0), vistaDbg1Axis.get(nFFT - 1));
			}
			graphView.setScalable(true);
			graphView.setScrollable(true);
			debug("GraphData: Plotted DFT");
		} else {
			for (int i = 0; i < M; i++) {
				data = new GraphViewData[num];
				for (int j = 0; j < num; j++) {
					data[j] = new GraphViewData(time.get(j), amp.get(i).get(j));
				}
				int thickness = 1;
				if (makeGraphBackgroundWhite) {
					thickness = 5;
				}
				switch (i) {
				case 0:
					GraphViewSeries series0 = new GraphViewSeries("dtf 0",
							new GraphViewSeriesStyle(Color.rgb(200, 50, 00), thickness), data);
					graphView.addSeries(series0);
					break;
				case 1:
					GraphViewSeries series1 = new GraphViewSeries("dtf 1",
							new GraphViewSeriesStyle(Color.rgb(200, 250, 00), thickness), data);
					graphView.addSeries(series1);
					break;
				case 2:
					GraphViewSeries series2 = new GraphViewSeries("dtf 2",
							new GraphViewSeriesStyle(Color.rgb(100, 150, 00), thickness), data);
					graphView.addSeries(series2);
					break;
				case 3:
					GraphViewSeries series3 = new GraphViewSeries("dtf 3",
							new GraphViewSeriesStyle(Color.rgb(90, 250, 00), thickness), data);
					graphView.addSeries(series3);
					break;
				}
			}
		}
		debug("GraphData: Plotted Amp");
		debug("bouIndex:" + bouIndex + " eouIndex:" + eouIndex + " finalBou:" + finalBou + " finalEou:" + finalEou);
		if (scaleTimeAxis) {
			if (scaleToUtt) {
				int nS = Math.max(0, time.get(bouIndex) - pad);
				int nE = Math.min(time.get(time.size() - 1), time.get(eouIndex) + pad);
				int nEnd = time.get(time.size() - 1);
				int nSize = nE - nS + pad;
				debug("nS:" + nS + " nE:" + nE + " fB:" + finalBou + " fE:" + finalEou + " pad:" + pad + " tS:"
						+ time.size() + " nEnd:" + nEnd);
				graphView.setViewPort(nS, nSize);
			} else {
				graphView.setViewPort(0, time.get(time.size() - 1));
			}
			// graphView.setScalable(true);
			// graphView.setScrollable(true);
			// graphView.setShowLegend(true);
		}
		layout = (LinearLayout) findViewById(R.id.graph1);
		if (makeGraphBackgroundWhite) {
			layout.setBackgroundColor(getResources().getColor(R.color.white, null));
		}
		layout.addView(graphView);
		debug("GraphData: Plotted graph1");
		////////////////////////////// GRAPH 2 ////////////////////////////////
		if (doMelConversion) {
			debug("freq before:" + freq.get(1));
			freq = dFn.transformFrequencies(freq);
			debug("freq after:" + freq.get(1));
			if (doMelConversionOfLines) {
				if (hFreq != null && hFreq.size() > 0) {
					hFreq = dFn.transformFrequencies(hFreq);
				}
				if (hFreq0 != null && hFreq0.size() > 0) {
					hFreq0 = dFn.transformFrequencies(hFreq0);
				}
				if (hFreq1 != null && hFreq1.size() > 0) {
					hFreq1 = dFn.transformFrequencies(hFreq1);
				}
				if (hFreq2 != null && hFreq2.size() > 0) {
					hFreq2 = dFn.transformFrequencies(hFreq2);
				}
				if (hFreq3 != null && hFreq3.size() > 0) {
					hFreq3 = dFn.transformFrequencies(hFreq3);
				}
			}
		}
		graphView = new LineGraphView(this, "");
		if (plotDftMagInsteadOfPitch && plotVistaDebugInsteadOfAmp && vistaDbg2 != null && vistaDbg2.size() > 1) {
			graphView = new LineGraphView(this, "DFT Mag");
			int nFFT = vistaDbg2.size();
			data = new GraphViewData[nFFT];
			for (int j = 0; j < nFFT; j++) {
				if (usevistaDbg2Axis && vistaDbg2Axis != null && vistaDbg2Axis.size() == nFFT) {
					data[j] = new GraphViewData(vistaDbg2Axis.get(j), vistaDbg2.get(j));
				} else {
					data[j] = new GraphViewData(j, vistaDbg2.get(j));
				}
			}
			GraphViewSeries series0 = new GraphViewSeries("dtfMag",
					new GraphViewSeriesStyle(Color.rgb(255, 255, 255), 1), data);
			graphView.addSeries(series0);
			if (!usevistaDbg2Axis || vistaDbg2Axis == null || vistaDbg2Axis.size() != nFFT) {
				graphView.setViewPort(0, nFFT);
			} else {
				graphView.setViewPort(vistaDbg2Axis.get(0), vistaDbg2Axis.get(nFFT - 1));
			}
			graphView.setScalable(true);
			graphView.setScrollable(true);
			debug("GraphData: Plotted DFT");
		} else if (plotDftMagInsteadOfPitch && dftMag != null) {
			graphView = new LineGraphView(this, "DFT Mag");
			int nFFT = dftMag.size();
			data = new GraphViewData[nFFT];
			for (int j = 0; j < nFFT; j++) {
				data[j] = new GraphViewData(j, dftMag.get(j));
			}
			GraphViewSeries series0 = new GraphViewSeries("dtfMag",
					new GraphViewSeriesStyle(Color.rgb(255, 255, 255), 1), data);
			graphView.addSeries(series0);
			graphView.setViewPort(0, nFFT);
			graphView.setScalable(true);
			graphView.setScrollable(true);
			debug("GraphData: Plotted DFT");
		} else {
			if (hFreq0 != null && hFreq0.size() > 0) {
				boolean reDefineTimes = false;
				if (reDefineTimes) {
					hTime0 = new ArrayList<Integer>(time);
					hTime1 = new ArrayList<Integer>(time);
					hTime2 = new ArrayList<Integer>(time);
					hTime3 = new ArrayList<Integer>(time);
				}
				// if (removeFormants) {
				graphView = new LineGraphView(this, "");
				// }
				// 0 and 2 have purpleish color and 1 and 3 have grey
				int numH = hTime0.size();
				int M1 = hFreq0.size();
				debug1("t0S:" + hTime0.size() + " t1S:" + hTime1.size() + " t2S:" + hTime2.size() + " t3S:"
						+ hTime3.size() + " tS:" + time.size());
				debug("t:" + time);
				debug("t0:" + hTime0);
				debug1("M1:" + M1 + " numH:" + numH);
				// debug22("sP:" +);
				debug1("hFreq0:" + hFreq0);
				debug1("hFreq1:" + hFreq1);
				debug1("hFreq2:" + hFreq2);
				debug1("hFreq3:" + hFreq3);
				for (int j = 0; j < M1; j++) {
					debug1("Plotting j:" + j + " --------------- ");
					int hFreq0Size = hFreq0.get(j).size();
					int hFreq1Size = hFreq1.get(j).size();
					int hFreq2Size = hFreq2.get(j).size();
					int hFreq3Size = hFreq3.get(j).size();
					int thickness1 = 3;// default was 1
					int thickness2 = 3;
					if (makeGraphBackgroundWhite || makeGraphBackgroundWhiteForPitch) {
						thickness1 = 4;
						thickness2 = 4;
					} else {
						thickness1 = 1;
						thickness2 = 1;
					}
					int deltaTime = 30;
					int tPrev = -1;
					int fPrev = -1;
					// hFreq0
					numH = hTime0.size();
					if (hFreq0 != null && hTime0 != null && M1 > 0 && pInd != null) {
						debug1("Plotting hFreq0...");
						for (int i = 0; i < numH; i++) {
							data = new GraphViewData[2];
							int fVal = 0;
							if (i < hFreq0Size) {
								fVal = hFreq0.get(j).get(i);
							}
							debug("i:" + i + " hTime0:" + hTime0.get(i) + " pInd:" + pInd.get(hTime0.get(i)));
							if (pInd.get(hTime0.get(i)) == null) {
								fPrev = -1;
								tPrev = hTime0.get(i);
								continue;
							}
							int pIndicator = pInd.get(hTime0.get(i));
							if (fVal > 0 && (!usePIndicator || pIndicator == 1)) {
								if (fPrev == -1 || tPrev == -1) {
									data[0] = new GraphViewData(hTime0.get(i) - deltaTime, fVal);
									data[1] = new GraphViewData(hTime0.get(i) + deltaTime, fVal);
								} else {
									data[0] = new GraphViewData(tPrev, fPrev);
									data[1] = new GraphViewData(hTime0.get(i), fVal);
								}
								GraphViewSeries series3h = new GraphViewSeries("dtf 3a",
										new GraphViewSeriesStyle(Color.rgb(173, 188, 230), thickness1), data);
								if (makeGraphBackgroundWhite || makeGraphBackgroundWhiteForPitch) {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(191, 0, 0), thickness1), data);
								} else {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(200, 50, 0), thickness1), data);
								}
								graphView.addSeries(series3h);
								fPrev = fVal;
							} else {
								fPrev = -1;
							}
							tPrev = hTime0.get(i);
						}
					}
					// hFreq1
					numH = hTime1.size();
					// if (hFreq1 != null && hFreq1.size() > 0) {
					// if (hFreq1 != null && hFreq1.size() == M1 && hFreq1.get(j).size() == numH) {
					if (hFreq1 != null && hTime1 != null && M1 > 0 && pInd != null) {
						debug1("Plotting hFreq1...");
						tPrev = -1;
						fPrev = -1;
						for (int i = 0; i < numH; i++) {
							data = new GraphViewData[2];
							int fVal = 0;
							if (i < hFreq1Size) {
								fVal = hFreq1.get(j).get(i);
							}
							boolean isCloseToFBelow = false;
							if (pInd.get(hTime1.get(i)) == null) {
								fPrev = -1;
								tPrev = hTime1.get(i);
								continue;
							}
							int pIndicator = pInd.get(hTime1.get(i));
							if (fVal > 0) {
								// isCloseToFBelow = isFreqCloseToBelow(fVal, i, j, hTime0, hTime1, hFreq0);
							}
							if (fVal > 0 && !isCloseToFBelow && (!usePIndicator || pIndicator == 1)) {
								if (fPrev == -1 || tPrev == -1) {
									data[0] = new GraphViewData(hTime1.get(i) - deltaTime, fVal);
									data[1] = new GraphViewData(hTime1.get(i) + deltaTime, fVal);
								} else {
									data[0] = new GraphViewData(tPrev, fPrev);
									data[1] = new GraphViewData(hTime1.get(i), fVal);
								}
								GraphViewSeries series3h = new GraphViewSeries("dtf 3a",
										new GraphViewSeriesStyle(Color.rgb(211, 211, 211), thickness2), data);
								if (makeGraphBackgroundWhite || makeGraphBackgroundWhiteForPitch) {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(255, 192, 0), thickness1), data);
								} else {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(200, 250, 0), thickness1), data);
								}
								graphView.addSeries(series3h);
								fPrev = fVal;
							} else {
								fPrev = -1;
							}
							tPrev = hTime1.get(i);
						}
					}
					// hFreq2
					numH = hTime2.size();
					// if (hFreq2 != null && hFreq2.size() == M1 && hFreq2.get(j).size() == numH) {
					if (hFreq2 != null && hTime2 != null && M1 > 0 && pInd != null) {
						debug1("Plotting hFreq2...");
						tPrev = -1;
						fPrev = -1;
						for (int i = 0; i < numH; i++) {
							data = new GraphViewData[2];
							int fVal = 0;
							if (i < hFreq2Size) {
								fVal = hFreq2.get(j).get(i);
							}
							if (pInd.get(hTime2.get(i)) == null) {
								fPrev = -1;
								tPrev = hTime2.get(i);
								continue;
							}
							int pIndicator = pInd.get(hTime2.get(i));
							if (fVal > 0 && (!usePIndicator || pIndicator == 1)) {
								if (fPrev == -1 || tPrev == -1) {
									data[0] = new GraphViewData(hTime2.get(i) - deltaTime, fVal);
									data[1] = new GraphViewData(hTime2.get(i) + deltaTime, fVal);
								} else {
									data[0] = new GraphViewData(tPrev, fPrev);
									data[1] = new GraphViewData(hTime2.get(i), fVal);
								}
								GraphViewSeries series3h = new GraphViewSeries("dtf 3a",
										new GraphViewSeriesStyle(Color.rgb(173, 188, 230), thickness1), data);
								if (makeGraphBackgroundWhite || makeGraphBackgroundWhiteForPitch) {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(0, 176, 80), thickness1), data);
								} else {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(100, 150, 0), thickness1), data);
								}
								graphView.addSeries(series3h);
								fPrev = fVal;
							} else {
								fPrev = -1;
							}
							tPrev = hTime2.get(i);
						}
					}
					// hFreq3
					numH = hTime3.size();
					// if (hFreq3 != null && hFreq3.size() == M1 && hFreq3.get(j).size() == numH) {
					if (hFreq3 != null && hTime3 != null && M1 > 0 && pInd != null) {
						debug1("Plotting hFreq3...");
						tPrev = -1;
						fPrev = -1;
						for (int i = 0; i < numH; i++) {
							data = new GraphViewData[2];
							int fVal = 0;
							if (i < hFreq3Size) {
								fVal = hFreq3.get(j).get(i);
							}
							if (pInd.get(hTime3.get(i)) == null) {
								fPrev = -1;
								tPrev = hTime3.get(i);
								continue;
							}
							int pIndicator = pInd.get(hTime3.get(i));
							if (fVal > 0 && (!usePIndicator || pIndicator == 1)) {
								if (fPrev == -1 || tPrev == -1) {
									data[0] = new GraphViewData(hTime3.get(i) - deltaTime, fVal);
									data[1] = new GraphViewData(hTime3.get(i) + deltaTime, fVal);
								} else {
									data[0] = new GraphViewData(tPrev, fPrev);
									data[1] = new GraphViewData(hTime3.get(i), fVal);
								}
								GraphViewSeries series3h = new GraphViewSeries("dtf 3a",
										new GraphViewSeriesStyle(Color.rgb(211, 211, 211), thickness2), data);
								if (makeGraphBackgroundWhite || makeGraphBackgroundWhiteForPitch) {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(0, 176, 240), thickness1), data);
								} else {
									series3h = new GraphViewSeries("dtf 3a",
											new GraphViewSeriesStyle(Color.rgb(90, 250, 0), thickness1), data);
								}
								graphView.addSeries(series3h);
								fPrev = fVal;
							} else {
								fPrev = -1;
							}
							tPrev = hTime3.get(i);
						}
					}
				}
			}
		}
		if (scaleTimeAxis) {
			if (scaleToUtt) {
				int nS = Math.max(0, time.get(bouIndex) - pad);
				int nE = Math.min(time.get(time.size() - 1), time.get(eouIndex) + pad);
				int nSize = nE - nS + pad;
				debug("nS:" + nS + " nE:" + nE + " finalBou:" + finalBou + " finalEou:" + finalEou);
				graphView.setViewPort(nS, nSize);
			} else {
				graphView.setViewPort(0, time.get(time.size() - 1));
			}
			// graphView.setScalable(true);
			// graphView.setScrollable(true);
			// graphView.setShowLegend(true);
		}
		// Plot Pitch
		if (plotPitchOnSecondGraph) {
			layout = (LinearLayout) findViewById(R.id.graph2);
		} else {
			layout = (LinearLayout) findViewById(R.id.graph3);
		}
		if (makeGraphBackgroundWhite || makeGraphBackgroundWhiteForPitch) {
			layout.setBackgroundColor(getResources().getColor(R.color.white, null));
		}
		layout.addView(graphView);
		debug("GraphData: Plotted Freq");

		// Plot pitch
		if (plot_entire_pitch && !plotVistaDebugInsteadOfAmp) {
			debug("aPitch:" + aPitch);
			debug("aPitchSize:" + aPitch.size() + " timeSize:" + time.size() + " timePitchSize:" + timePitch.size());
			debug("subbandPitch:" + subbandPitch);
			ArrayList<Integer> pitchPlotLevelForChannels = new ArrayList<Integer>();
			for (int j = 0; j < M; j++) {
				if (j == 0) {
					pitchPlotLevelForChannels.add(0);
				} else {
					pitchPlotLevelForChannels.add(40 * j);
				}
			}
			// first plot bestPitch then on top other channels
			if (pAvg != null) {
				int numSBP = pAvg.size();
				int numSBT = hTime0.size();
				debug("numSBP:" + numSBP + " numSBT:" + numSBT);
				data = new GraphViewData[numSBT];
				// TBD: plot avg pitch and valid subband pitch (pArray) as indicators on top
				for (int i = 0; i < numSBT; i++) {
					int t = hTime0.get(i);
					if (t < 0 || pAvg.get(t) == null) {
						data[i] = new GraphViewData(t, 0);
						continue;
					}
					int p = pAvg.get(t);
					int pIndicator = pInd.get(hTime0.get(i));
					debug("t:" + t + " p:" + p + " i:" + i);
					if (p > 0 && (!usePIndicator || pIndicator == 1)) {
						data[i] = new GraphViewData(t, p);
						// data[i] = new GraphViewData(t, 1000);
					} else {
						data[i] = new GraphViewData(t, 0);
						// data[i] = new GraphViewData(t, 1000);
					}
				}
				GraphViewSeries pitchTrack = new GraphViewSeries("pitch 0",
						new GraphViewSeriesStyle(Color.rgb(200, 200, 200), 1), data);
				graphView.addSeries(pitchTrack);
				boolean addsubbands = true;
				if (addsubbands) {
					debug("adding subbands");
					for (int j = 0; j < M; j++) {
						data = new GraphViewData[numSBT];
						// TBD: plot avg pitch and valid subband pitch (pArray) as indicators on top
						for (int i = 0; i < numSBT; i++) {
							int t = hTime0.get(i);
							if (t < 0 || pAvg.get(t) == null) {
								data[i] = new GraphViewData(t, 0);
								continue;
							}
							int p = pAvg.get(t);
							int pIndicator = pInd.get(hTime0.get(i));
							int pC = 0;
							boolean useSBD = true;
							if (useSBD) {
								pC = pSBD.get(t).get(j);
							} else {
								if (hTime0.contains(t)) {
									if (j == 0) {
										pC = pChn0.get(hTime0.indexOf(t));
									} else if (j == 1) {
										pC = pChn1.get(hTime0.indexOf(t));
									} else if (j == 2) {
										pC = pChn2.get(hTime0.indexOf(t));
									} else if (j == 3) {
										pC = pChn3.get(hTime0.indexOf(t));
									}
								}
							}
							debug("t:" + t + " p:" + p + " i:" + i);
							if (p > 0 && pC > 0 && (!usePIndicator || pIndicator == 1)) {
								data[i] = new GraphViewData(t, p + 30 * (j + 1));
							} else {
								data[i] = new GraphViewData(t, 0);
							}
						}
						pitchTrack = new GraphViewSeries("pitch 0",
								new GraphViewSeriesStyle(Color.rgb(200, 200, 200), 1), data);
						graphView.addSeries(pitchTrack);
					}
				}
			}
		}
		debug("GraphData: Done with Plotting pitch");
		////////////////////////////// GRAPH 3 ////////////////////////////////
		// add 3rd plot freq
		// Frequency
		debug3("plotSignalInsteadOfFreq:" + plotSignalInsteadOfFreq + " signal:" + signal);
		graphView = new LineGraphView(this, "");
		boolean plotMelSpec = false;
		if (plotMFB && plotMelSpec && melFBSpec != null) {
			graphView = new LineGraphView(this, "Mel FB");
			int N = melFBSpec.size();
			data = new GraphViewData[N];
			for (int j = 0; j < N; j++) {
				data[j] = new GraphViewData(j, melFBSpec.get(j));
			}
			GraphViewSeries series0 = new GraphViewSeries("mel", new GraphViewSeriesStyle(Color.rgb(200, 50, 0), 1),
					data);
			graphView.addSeries(series0);
			graphView.setViewPort(0, N - 1);
			graphView.setScalable(true);
		} else if (plotMFB && melFB != null) {
			boolean doMfbDebug = false;
			boolean doDb = true;
			boolean useFalseChannels = false;
			boolean addFourChannels = true;
			int c1 = 1;
			int c2 = 5;
			int c3 = 6;
			int c4 = 7;
			if (useFalseChannels) {
				// increase a3-a0
				c1 = 0;
				c2 = 5;
				// completely fail
				c1 = 1;
				c2 = 7;
			}
			// 1: 200, 50, 00
			// 2: 200, 250, 00
			// 3: 100, 150, 00
			// 4: 90, 250, 00
			ArrayList<Integer> aMfbDbg = new ArrayList<Integer>();
			graphView = new LineGraphView(this, "Mel FB");
			int N = melFBAxis.size();// melFB.get(0).size();
			debug6("N:" + N + " NAxis:" + melFBAxis.size());
			// add first chn (red)
			data = new GraphViewData[N];
			int minDb = 0;
			int maxDb = 200;
			debug6("aE:c1:" + melFB.get(c1));
			for (int j = 0; j < N; j++) {
				int melFBDb = (int) (10 * Math.log10(melFB.get(c1).get(j)));
				if (melFBDb < minDb) {
					melFBDb = minDb;
				}
				if (melFBDb > maxDb) {
					melFBDb = maxDb;
				}
				if (doMfbDebug) {
					aMfbDbg.add(melFBDb);
				}
				if (doDb) {
					data[j] = new GraphViewData(melFBAxis.get(j), melFBDb);
				} else {
					data[j] = new GraphViewData(melFBAxis.get(j), melFB.get(c1).get(j));
				}
			}
			GraphViewSeries series0 = new GraphViewSeries("mel", new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1),
					data);
			graphView.addSeries(series0);
			debug6("c1:" + c1 + " aMfbDbg:" + aMfbDbg);
			// add second chn (green)
			debug6("aE:c2:" + melFB.get(c2));
			data = new GraphViewData[N];
			if (doMfbDebug) {
				aMfbDbg.clear();
			}
			for (int j = 0; j < N; j++) {
				int melFBDb = (int) (10 * Math.log10(melFB.get(c2).get(j)));
				if (melFBDb < minDb) {
					melFBDb = minDb;
				}
				if (melFBDb > maxDb) {
					melFBDb = maxDb;
				}
				if (doMfbDebug) {
					aMfbDbg.add(melFBDb);
				}
				if (doDb) {
					data[j] = new GraphViewData(melFBAxis.get(j), melFBDb);
				} else {
					data[j] = new GraphViewData(melFBAxis.get(j), melFB.get(c2).get(j));
				}
			}
			debug6("c2:" + c2 + " aMfbDbg:" + aMfbDbg);
			GraphViewSeries series1 = new GraphViewSeries("mel", new GraphViewSeriesStyle(Color.rgb(200, 250, 00), 1),
					data);
			graphView.addSeries(series1);
			if (addFourChannels) {
				// add 3rd chn (blue)
				data = new GraphViewData[N];
				for (int j = 0; j < N; j++) {
					int melFBDb = (int) (10 * Math.log10(melFB.get(c3).get(j)));
					if (melFBDb < minDb) {
						melFBDb = minDb;
					}
					if (melFBDb > maxDb) {
						melFBDb = maxDb;
					}
					if (doMfbDebug) {
						aMfbDbg.add(melFBDb);
					}
					if (doDb) {
						data[j] = new GraphViewData(melFBAxis.get(j), melFBDb);
					} else {
						data[j] = new GraphViewData(melFBAxis.get(j), melFB.get(c3).get(j));
					}
				}
				GraphViewSeries series2 = new GraphViewSeries("mel",
						new GraphViewSeriesStyle(Color.rgb(100, 150, 00), 1), data);
				graphView.addSeries(series2);
				debug6("c1:" + c1 + " aMfbDbg:" + aMfbDbg);
				// add 4th chn (green)
				debug6("aE:c2:" + melFB.get(c4));
				data = new GraphViewData[N];
				if (doMfbDebug) {
					aMfbDbg.clear();
				}
				for (int j = 0; j < N; j++) {
					int melFBDb = (int) (10 * Math.log10(melFB.get(c4).get(j)));
					if (melFBDb < minDb) {
						melFBDb = minDb;
					}
					if (melFBDb > maxDb) {
						melFBDb = maxDb;
					}
					if (doMfbDebug) {
						aMfbDbg.add(melFBDb);
					}
					if (doDb) {
						data[j] = new GraphViewData(melFBAxis.get(j), melFBDb);
					} else {
						data[j] = new GraphViewData(melFBAxis.get(j), melFB.get(c4).get(j));
					}
				}
				GraphViewSeries series3 = new GraphViewSeries("mel",
						new GraphViewSeriesStyle(Color.rgb(90, 250, 00), 1), data);
				graphView.addSeries(series3);
			}
			// set axis
			graphView.setViewPort(melFBAxis.get(0), melFBAxis.get(N - 1));
			graphView.setScalable(true);
			graphView.setScrollable(true);
			debug("GraphData: Plotted Mel");
		} else if (plotDftMagInsteadOfFreq && dftMag != null /* && dftMagAxis.size() != 0 */) {
			// graphView = new LineGraphView(this, "DFT Mag");
			int nFFT = dftMag.size();
			debug4("dftMag:" + dftMag);
			data = new GraphViewData[nFFT];
			for (int j = 0; j < nFFT; j++) {
				if (useDftMagAxis && dftMagAxis != null && dftMagAxis.size() == nFFT) {
					data[j] = new GraphViewData(dftMagAxis.get(j), dftMag.get(j));
				} else {
					data[j] = new GraphViewData(j, dftMag.get(j));
				}
			}
			GraphViewSeries series0 = new GraphViewSeries("dtfMag",
					new GraphViewSeriesStyle(Color.rgb(255, 255, 255), 1), data);
			graphView.addSeries(series0);
			if (!useDftMagAxis || dftMagAxis == null || dftMagAxis.size() != nFFT) {
				graphView.setViewPort(0, nFFT);
			} else {
				graphView.setViewPort(dftMagAxis.get(0), dftMagAxis.get(nFFT - 1));
			}
			graphView.setScalable(true);
			graphView.setScrollable(true);
			debug("GraphData: Plotted DFT");
		} else if (plotSignalInsteadOfFreq && signal != null) {
			// graphView = new LineGraphView(this, "DFT Mag");
			int nS = signal.size();
			data = new GraphViewData[nS];
			for (int j = 0; j < nS; j++) {
				data[j] = new GraphViewData(j, signal.get(j));
			}
			GraphViewSeries series0 = new GraphViewSeries("dtfMag",
					new GraphViewSeriesStyle(Color.rgb(255, 255, 255), 1), data);
			graphView.addSeries(series0);
			graphView.setViewPort(0, nS);
			graphView.setScalable(true);
			graphView.setScrollable(true);
			debug("GraphData: Plotted Signal");
		} else {
			for (int i = 0; i < M; i++) {
				data = new GraphViewData[num];
				for (int j = 0; j < num; j++) {
					data[j] = new GraphViewData(time.get(j), freq.get(i).get(j));
				}
				switch (i) {
				case 0:
					GraphViewSeries series0 = new GraphViewSeries("dtf 0",
							new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1), data);
					graphView.addSeries(series0);
					break;
				case 1:
					GraphViewSeries series1 = new GraphViewSeries("dtf 1",
							new GraphViewSeriesStyle(Color.rgb(200, 250, 00), 1), data);
					graphView.addSeries(series1);
					break;
				case 2:
					GraphViewSeries series2 = new GraphViewSeries("dtf 2",
							new GraphViewSeriesStyle(Color.rgb(100, 150, 00), 1), data);
					graphView.addSeries(series2);
					break;
				case 3:
					GraphViewSeries series3 = new GraphViewSeries("dtf 3",
							new GraphViewSeriesStyle(Color.rgb(90, 250, 00), 1), data);
					graphView.addSeries(series3);
					break;
				}
			}
			if (plotBwtAlongWithFreq) {
				int doBwtHz = 2;// 0 is false, 1 is Hz, 2 is Mel
				int maxFreq3 = 0;
				for (int i = 0; i < M; i++) {
					for (int j = 0; j < num; j++) {
						if (i == (M - 1) && freq.get(i).get(j) > maxFreq3) {
							maxFreq3 = freq.get(i).get(j);
						}
					}
				}
				int bwtForPlot = maxFreq3 + 100;// 3500;
				int bwtMax = 200;// 0.95 is 130, 0.9 is 268, 0.85 is 413, 0.8 is 568
				int bwtScale = 400;
				int minBwt = 950;
				int maxBwt = 1000;
				int rangeBwt = maxBwt - minBwt;
				for (int j = 0; j < M; j++) {
					data = new GraphViewData[num];
					int bwtMelJSize = bwtMel.get(j).size();
					for (int i = 0; i < num; i++) {
						if (doBwtHz == 1) {
							int dataValue;
							float bwtJ = bwt.get(j).get(i);
							int bwtI = 0;
							if (bwtJ > 0) {
								bwtI = (int) (-(Math.log(bwtJ) * 8000) / Math.PI);
								if (bwtI > bwtMax) {
									bwtI = bwtMax;
								}
								bwtI = (bwtI * bwtScale) / bwtMax;
							}
							dataValue = bwtForPlot + bwtI;
							data[i] = new GraphViewData(time.get(i), dataValue);
						} else if (doBwtHz == 2) {
							if (i < bwtMelJSize) {
								int bwtMelMax = 500;
								int dataValue;
								int bwtJ = bwtMel.get(j).get(i);
								if (bwtJ > 0) {
									if (bwtJ > bwtMelMax) {
										bwtJ = bwtMelMax;
									}
								}
								dataValue = bwtForPlot + bwtJ;
								data[i] = new GraphViewData(time.get(i), dataValue);
							} else {
								debug2("i exceeds:i:" + i + " bwtMelJSize:" + bwtMelJSize);
								data[i] = new GraphViewData(time.get(i), 0);
							}
						} else {
							int dataValue = -200 - rangeBwt;// -400;
							int bwtJ = (int) (1000 * bwt.get(j).get(i));
							if (bwtJ > minBwt) {
								dataValue = -200 - (maxBwt - bwtJ);
							}
							data[i] = new GraphViewData(time.get(i), dataValue);
						}
					}
					switch (j) {
					case 0:
						GraphViewSeries series0 = new GraphViewSeries("dtf 0",
								new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1), data);
						graphView.addSeries(series0);
						break;
					case 1:
						GraphViewSeries series1 = new GraphViewSeries("dtf 1",
								new GraphViewSeriesStyle(Color.rgb(200, 250, 00), 1), data);
						graphView.addSeries(series1);
						break;
					case 2:
						GraphViewSeries series2 = new GraphViewSeries("dtf 2",
								new GraphViewSeriesStyle(Color.rgb(100, 150, 00), 1), data);
						graphView.addSeries(series2);
						break;
					case 3:
						GraphViewSeries series3 = new GraphViewSeries("dtf 2",
								new GraphViewSeriesStyle(Color.rgb(100, 150, 00), 1), data);
						graphView.addSeries(series3);
						break;
					}
				}
			}
		}
		if (plotPitchOnSecondGraph) {
			// Plot Freq in 3rd graph
			layout = (LinearLayout) findViewById(R.id.graph3);
		} else {
			layout = (LinearLayout) findViewById(R.id.graph2);
		}
		if (makeGraphBackgroundWhite) {
			layout.setBackgroundColor(getResources().getColor(R.color.white, null));
		}
		layout.addView(graphView);
		debug("GraphData: Plotted Freq...");
		if (scaleTimeAxis) {
			if (scaleToUtt) {
				int nS = Math.max(0, time.get(bouIndex) - pad);
				int nE = Math.min(time.get(time.size() - 1), time.get(eouIndex) + pad);
				int nSize = nE - nS + pad;
				debug("nS:" + nS + " nE:" + nE + " finalBou:" + finalBou + " finalEou:" + finalEou);
				debug2("Setting view port:" + nSize);
				graphView.setViewPort(nS, nSize);
				debug2("Setting view port DONE");
			} else {
				graphView.setViewPort(0, time.get(time.size() - 1));
			}
		}
		if (plot_pitch_feats) {
			debug("pProb:" + pitchProb);
			debug("pE:" + pitchEnergy);
			debug("pBwt:" + pitchBwt);
			graphView = new LineGraphView(this, "");
			int minPE = -1;
			int minME = -1;
			if (plot_pitch_feat_type == 6) {
				for (int j = 0; j < M; j++) {
					for (int i1 = 0; i1 < num; i1++) {
						int pE = pitchEnergy.get(j).get(i1);
						if (minPE == -1 || pE < minPE) {
							minPE = pE;
						}
						int mE = aMaxEnergy.get(j).get(i1);
						if (minME == -1 || mE < minME) {
							minME = mE;
						}
					}
				}
			}
			for (int j = 0; j < M; j++) {
				int num1 = Math.min(num, pitchProb.get(0).size());
				data = new GraphViewData[num1];
				for (int i1 = 0; i1 < num1; i1++) {
					// 0:pitchProb, 1:aMaxEnergy, 2:specBwt, 3:pitchBwt, 4: pitchCnt,
					// 5:antiformant, 6:pitchEnergy, 7:aFreqMaxEnergy, 8:aTotEnergy,
					// 9:aFreqCentroid, 10: nAntiformants, 11: nZerosInNasalBand, 12:
					// subbandMaxProb5, 13: subbandMaxProbL, 14: hfMultiplePMatch, 15: nazFeat, 16:
					// zFeat, 17: ZHFeat, 18: weakFricFeat
					if (plot_pitch_feat_type == 0) {
						int pV = pitchProb.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 1) {
						int pV = aMaxEnergy.get(j).get(i1);
						boolean doBelow = false;
						if (doBelow) {
							if (pV != 0) {
								data[i1] = new GraphViewData(time.get(i1), pV);
							} else {
								data[i1] = new GraphViewData(time.get(i1), minME);
							}
						} else {
							data[i1] = new GraphViewData(time.get(i1), Math.max(20, pV));
						}
					} else if (plot_pitch_feat_type == 2) {
						int pV = specBwt.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 3) {
						int pV = pitchBwt.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 4) {
						// int pV = subbandCnt.get(j).get(i1);
						int pV = subbandPitch.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 5) {
						int pV = antiformant.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 6) {
						int pV = pitchEnergy.get(j).get(i1);
						if (pV != 0) {
							data[i1] = new GraphViewData(time.get(i1), pV);
						} else {
							data[i1] = new GraphViewData(time.get(i1), minPE);
						}
					} else if (plot_pitch_feat_type == 7) {
						int pV = aFreqMaxEnergy.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 8) {
						int pV = aTotEnergy.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 9) {
						int pV = aFreqCentroid.get(j).get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 10) {
						int pV = nAntiformants.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 11) {
						int pV = nZerosInNasalBand.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 12) {
						int pV = subbandMaxProb5.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 13) {
						int pV = subbandMaxProbL.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 14) {
						int pV = hfMultiplePMatch.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 15) {
						int pV = nazFeat.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 16) {
						int pV = zFeat.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 17) {
						int pV = zHFeat.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					} else if (plot_pitch_feat_type == 18) {
						int pV = weakFricFeat.get(i1);
						data[i1] = new GraphViewData(time.get(i1), pV);
					}
				}
				switch (j) {
				case 0:
					GraphViewSeries series0 = new GraphViewSeries("dtf 0",
							new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1), data);
					graphView.addSeries(series0);
					break;
				case 1:
					GraphViewSeries series1 = new GraphViewSeries("dtf 1",
							new GraphViewSeriesStyle(Color.rgb(200, 250, 00), 1), data);
					graphView.addSeries(series1);
					break;
				case 2:
					GraphViewSeries series2 = new GraphViewSeries("dtf 2",
							new GraphViewSeriesStyle(Color.rgb(100, 150, 00), 1), data);
					graphView.addSeries(series2);
					break;
				case 3:
					GraphViewSeries series3 = new GraphViewSeries("dtf 3",
							new GraphViewSeriesStyle(Color.rgb(90, 250, 00), 1), data);
					graphView.addSeries(series3);
					break;
				}
			}
			if (scaleTimeAxis) {
				if (scaleToUtt) {
					int nS = Math.max(0, time.get(finalBou) - pad);
					int nE = Math.min(time.get(time.size() - 1), time.get(finalEou) + pad);
					int nSize = nE - nS + pad;
					graphView.setViewPort(nS, nSize);
				} else {
					graphView.setViewPort(0, time.get(time.size() - 1));
				}
				// graphView.setScalable(true);
				// graphView.setScrollable(true);
				// graphView.setShowLegend(true);
			}
			layout = (LinearLayout) findViewById(R.id.graph3);
			layout.addView(graphView);
			debug("GraphData: Plotted Pitch Feats");
		}
		boolean showOutput = false;
		if (showOutput) {
			// Print
			TextView outputText;
			outputText = (TextView) this.findViewById(R.id.textOutput);
			// int snrF = (int) ((snr[0] + snr[1]) / 2);
			// outputText.setText("SNR is " + snrF + " dB; ");
			/*
			 * outputText.setText("Signal-to-noise ratio is " + snrF + " dB; " +
			 * "sub-band SNRs are "); for (int i = 0; i < M - 2; i++) { int snrI = (int)
			 * snr[i]; outputText.append(snrI + ", "); } int snrI = (int) snr[M - 1];
			 * outputText.append("and " + snrI + " dBs\n");
			 */
			debug("SNR printed");
			int nS = Math.max(0, time.get(bouIndex) - pad);
			int nE = Math.min(time.get(time.size() - 1), time.get(eouIndex) + pad);
			outputText.append("nS:" + nS + " nE:" + nE + " finalBou:" + finalBou + " finalEou:" + finalEou + "\n");
			ArrayList<Integer> aDur = new ArrayList<Integer>();
			ArrayList<Integer> aScores = new ArrayList<Integer>();
			if (outputA3MinusA0) {
				outputText.append("\n");
			}
			if (outputA3MinusA0) {
				outputText.append("\n");
			}
			outputText.append("D:" + aDur + " ");
			outputText.append("S:" + aScores + " ");
		}
	}

	private ArrayList<Integer> getBwtRatiosdB(ArrayList<Integer> a) {
		float maxA = (float) Collections.max(a);
		if (maxA <= 1) {
			return null;
		}
		ArrayList<Integer> aRat = new ArrayList<Integer>();
		for (int j = 0; j < a.size(); j++) {
			float rat = ((float) a.get(j)) / maxA;
			aRat.add((int) (rat));
		}
		return aRat;
	}

	private void getAllAmpsDB(ArrayList<ArrayList<Integer>> a, int M, int num, int[] aLevels, int thrSS) {
		int chopThresh = 20;
		boolean doUpperThreshold = false;
		boolean doLowerThreshold = false;
		// int thrSS = getSpectralShapeThreshold(aLevels);
		// System.out.println("getAllAmpsDB:thrSS:" + thrSS);
		boolean doDebug = false;
		if (doDebug) {
			debug5("a size:" + a.size() + " num:" + num);
			for (int i = 0; i < a.size(); i++) {
				debug5("i:" + i + " size:" + a.get(i).size());
			}
		}
		for (int i = 0; i < num; i++) {
			ArrayList<Integer> aJ = new ArrayList<Integer>();
			for (int j = 0; j < M; j++) {
				aJ.add(a.get(j).get(i));
			}
			ArrayList<Integer> aJ1 = new ArrayList<Integer>();
			// aJ1 = getAmplitudeRatiosdB(aJ, false, doAmpDB);
			aJ1 = dFn.getAmplitudeRatiosdB(aJ, false, true, true, false);
			debug("getAllAmpsDB:aJ1:" + aJ1);
			if (aJ1 != null) {
				for (int j = 0; j < M; j++) {
					int b = aJ1.get(j);
					if (doLowerThreshold && (b < -chopThresh)) {
						b = -chopThresh;
					} else if (doUpperThreshold && (b > chopThresh)) {
						b = chopThresh;
					}
					a.get(j).set(i, b);
				}
			} else {
				for (int j = 0; j < M; j++) {
					a.get(j).set(i, 0);
				}
			}
		}
	}

	private ArrayList<ArrayList<Integer>> getAllBwsInt(ArrayList<ArrayList<Float>> bwt, int M, int num) {
		ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();
		for (int j = 0; j < M; j++) {
			ArrayList<Integer> bI = new ArrayList<Integer>();
			for (int i = 0; i < num; i++) {
				bI.add((int) (1000 * bwt.get(j).get(i)));
			}
			out.add(bI);
		}
		return out;
	}

	public void onClick(View v) {
		debug("graphData finish");
		finish();
	}

	private void debug(String message) {
		// System.out.println(":d0:" + message);
		// Keying.debug(message, getClass().getName());
	}

	private void debug1(String message) {
		// System.out.println(":d1:" + message);
	}

	private void debug2(String message) {
		// System.out.println(":d2:" + message);
	}

	private void debug3(String message) {
		// System.out.println(":d3:" + message);
	}

	private void debug4(String message) {
		// System.out.println(":dG4:" + message);
	}

	private void debug5(String message) {
		// System.out.println("dG5:" + message);
	}

	private void debug6(String message) {
		// System.out.println("dG6:" + message);
	}

	private void debug7(String message) {
		System.out.println("dG7:" + message);
	}
}
