package com.example.tfbapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.os.Environment;

public class TFBFxFl {
	/*
	 * The authors hereby grant you a non-exclusive, non-transferable, free of
	 * charge right to copy, modify, merge, publish, distribute, and sublicense the
	 * Software for the sole purpose of performing non-commercial scientific
	 * research, non-commercial education, or non-commercial artistic projects. Any
	 * other use, in particular any use for commercial purposes, is prohibited. This
	 * includes, without limitation, incorporation in a commercial product, use in a
	 * commercial service, or production of other artefacts for commercial purposes.
	 * 
	 * For details, see the license.pdf file included
	 */
	enum State {

		INIT, ACQUIRE, TRACK
	};

	private boolean doFixedPoint = false;
	/*
	 * If _fx means fixedpoint; else floating point. If both done in a fn it has
	 * function_FxFl()
	 */
	private State state;
	public final int M = 4;
	protected static final int LL = 120;
	private final int LLMaxFreq = 128;
	protected static final int PitchWindow = 256;// 512;// 256;
	protected int chnForDsfOut = -1;
	public int nTime;
	public int f0cntMax;
	private int f0cnt3;
	private int f0cnt3Max;
	private int nInit;
	private int f0cnt;
	private int[] f0Hz = new int[M];
	private float[] fmtInit;
	public int fLowFreqCounter;
	//
	private float f0Initial;
	private final float SAMPLING_FREQUENCY = 8000f;
	private final float LOW_FREQ_THR_MIN = 50f / SAMPLING_FREQUENCY;
	private final float LOW_FREQ_THR_MAX = 200f / SAMPLING_FREQUENCY;
	private final float FREQ_THR = 250f / SAMPLING_FREQUENCY;
	private final float FREQ_THR_MIN = 50f / SAMPLING_FREQUENCY;
	private final float F0INIT = 500.0f / SAMPLING_FREQUENCY;
	private final float F1INIT = 1500.0f / SAMPLING_FREQUENCY;
	private final float F2INIT = 2700.0f / SAMPLING_FREQUENCY;
	private final float F3INIT = 3500.0f / SAMPLING_FREQUENCY;
	private final float AMP_THR = 0.00001f;// dynamic range of ear
	private final float rp = 0.9f;// use 0.99f and 0.2f/0.1f for paper analysis
	private final float rz = 1.0f;// rz when freq get close // use 0.1f and 0.5f for paper analysis
	private final float rzFC = 0.0f;
	private final float rPre = 0.95f;
	private float[] gain;
	private float[] bbr;
	private float[] f0;
	private float[] fz;
	private float[] pp;
	private float[][] p;
	private float[][] hp;
	private short[][] dsfOut;
	private short[] melIn;
	private float[] bz;
	private float[] bz1;
	private float[] bp;
	private float[] pAcf;
	private float prho;
	private float[] a0;
	private float[] bw0;
	private float[] lpcCofs;
	private float[] f0Old, a0Old, bw0Old;
	private float f03;
	private float bw03;
	private float a03;
	private float skMinus1;// data at k-1 (used for preemp)
	private float spMinus2;// preemp data at [k-2]
	private float spMinus1;// preemp data at [k-1]
	private float sp;// preemp data at [k]
	//
	FixedPoint fp;
	private long M_Fx;
	private long f0Initial_fx;
	private long SAMPLING_FREQUENCY_FX;
	private long LOW_FREQ_THR_MIN_FX;
	private long LOW_FREQ_THR_MAX_FX;
	private long FREQ_THR_FX;
	private long FREQ_THR_MIN_FX;
	private long AMP_THR_FX;
	private long rp_fx;
	private long rz_fx;
	private long rzFC_fx;
	private long rpre_fx;
	private long[] gain_fx;
	private long[] bbr_fx;
	private long[] f0_fx;
	private long[] fz_fx;
	private long[][] p_fx;
	private long[][] hp_fx;
	private long[] bz_fx;
	private long[] bz1_fx;
	private long[] bp_fx;
	private long[] pp_fx;
	private long[] pAcf_fx;
	private long prho_fx;
	private long[] a0_fx;
	private long[] bw0_fx;
	private long[] lpcCofs_fx;
	private long[] f0Old_fx, a0Old_fx, bw0Old_fx;
	private long f03_fx;
	private long bw03_fx;
	private long a03_fx;
	private long skMinus1_fx;// data at k-1 (used for preemp)
	private long spMinus2_fx;// preemp data at [k-2]
	private long spMinus1_fx;// preemp data at [k-1]
	private long sp_fx;// preemp data at [k]
	// Pars below are used simply for Fx computation as temp storage
	private int[] pitch;
	private int maxFreq = 0;
	private int strongestFreq = -1;
	private int strongestFreqK = 0;
	LinkedHashMap<Integer, String> strongestFreqTrackChange;
	LinkedHashMap<Integer, String> maxAmpBetweenStrongestFreqTrackChanges;
	LinkedHashMap<Integer, String> changeFreqTracks;
	LinkedHashMap<Integer, String> changeFreqMaxAmp;
	private int changeFreqK = 0;
	Integer[] freqTrackOrdering;
	// private int maxFreqAmp = 0;
	private BufferedWriter fout;
	private FileOutputStream fout1;
	private String fileOut;
	ArrayList<Integer> tArray;// Store estimation timings
	ArrayList<ArrayList<Integer>> fArray;// frequencies array
	ArrayList<ArrayList<Integer>> aArray;// prho_fx's array
	ArrayList<ArrayList<Integer>> aDBArray;// prho_fx's array
	ArrayList<ArrayList<Float>> bArray;// bw's array
	ArrayList<ArrayList<Integer>> bMelArray;// bw->Hz->Mel array
	LinkedHashMap<Integer, Integer> hMTimeToPitchInd;// pitch Indicators
	LinkedHashMap<Integer, Integer> hMTimeToPitchAvg;// pitch Avg across channels
	LinkedHashMap<Integer, ArrayList<Integer>> hMTimeToSubbandPitch;// sub-band pitch array
	ArrayList<Integer> f3Array;// f3 estimated faster rate
	ArrayList<Integer> t3Array;// time for f3
	ArrayList<Integer> maxFreqArray;// Store estimated spectral Max
	private ArrayList<ArrayList<Float>> mPar;// Store estimated featuresF
	private int dtfLogNumber = 0;
	protected boolean writeOutFeatures;
	private boolean outputMelFeatures = false;// true;
	protected ArrayList<Short> pOut;// for pitch estimation
	protected boolean doPreemp = true;
	private boolean UsePreEmphDataForPitch = true;
	private boolean OverlapWindowProcessing = false;
	private boolean doMaxFreqStoring = false;// no need of this
	protected boolean doPitchProcessing = true;
	protected static boolean doPitchAllSubbands = true;
	private boolean interpolatePitch = false;
	private boolean doCheckFrequencyCloseness = true;
	private boolean doAZFZeroOnFreqClose = false;
	private boolean doPrevFreqOnFreqClose = false;
	private boolean doConstantQ = false;
	private boolean doConstantQForC3 = false;
	private float rpC3 = 0.0f;// 0.85f;// 0 if not to be used
	private boolean doF3WithSmallerProcessingWindow = false;// change f0cnt3Max if needed
	private boolean doConstantQForF3Faster = true;
	protected static int dataTypeForDSF = 1;// 0=sp_fx,1=azf.2=dtf o/p_fx
	DspFunctions dFn = new DspFunctions();

	TFBFxFl(String file) throws IOException {
		writeOutFeatures = true;
		fileOut = file;
		fout = new BufferedWriter(new FileWriter(fileOut + dtfLogNumber + ".xls"));
		// fout1 = new FileOutputStream(fileOut + "T" + dtfLogNumber + ".xls");
		createPars();
		init(true);
	}

	TFBFxFl() throws IOException {
		writeOutFeatures = false;
		createPars();
		init(true);
	}

	private void createPars() {
		fmtInit = new float[M];
		pitch = new int[M];
		dsfOut = new short[PitchWindow][M];
		melIn = new short[PitchWindow];
		//
		pp = new float[LL];
		p = new float[LL][M];
		hp = new float[3][(M - 1) * M];
		fz = new float[M];
		bz = new float[M];
		bz1 = new float[M];
		bp = new float[M];
		gain = new float[M];
		bbr = new float[M];
		f0 = new float[M];
		pAcf = new float[3];
		lpcCofs = new float[3];
		a0 = new float[M];
		bw0 = new float[M];
		f0Old = new float[M];
		a0Old = new float[M];
		bw0Old = new float[M];
		//
		pp_fx = new long[LL];
		p_fx = new long[LL][M];
		hp_fx = new long[3][(M - 1) * M];
		fp = new FixedPoint();
		fz_fx = new long[M];
		bz_fx = new long[M];
		bz1_fx = new long[M];
		bp_fx = new long[M];
		gain_fx = new long[M];
		bbr_fx = new long[M];
		f0_fx = new long[M];
		pAcf_fx = new long[3];
		lpcCofs_fx = new long[3];
		a0_fx = new long[M];
		bw0_fx = new long[M];
		f0Old_fx = new long[M];
		a0Old_fx = new long[M];
		bw0Old_fx = new long[M];
		//
		tArray = new ArrayList<Integer>();
		fArray = new ArrayList<ArrayList<Integer>>(M);
		aArray = new ArrayList<ArrayList<Integer>>(M);
		aDBArray = new ArrayList<ArrayList<Integer>>(M);
		bArray = new ArrayList<ArrayList<Float>>(M);
		bMelArray = new ArrayList<ArrayList<Integer>>(M);
		hMTimeToPitchInd = new LinkedHashMap<Integer, Integer>();
		hMTimeToPitchAvg = new LinkedHashMap<Integer, Integer>();
		hMTimeToSubbandPitch = new LinkedHashMap<Integer, ArrayList<Integer>>();
		f3Array = new ArrayList<Integer>();
		t3Array = new ArrayList<Integer>();
		if (doMaxFreqStoring) {
			maxFreqArray = new ArrayList<Integer>();
		}
		mPar = new ArrayList<ArrayList<Float>>(M);
		if (UsePreEmphDataForPitch) {
			pOut = new ArrayList<Short>();
		}
		// Insert arrays required for storing the features
		for (int i = 0; i < M; i++) {
			fArray.add(new ArrayList<Integer>());
			aArray.add(new ArrayList<Integer>());
			aDBArray.add(new ArrayList<Integer>());
			bArray.add(new ArrayList<Float>());
			bMelArray.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < M; i++) {
			mPar.add(new ArrayList<Float>(3));
		}
	}

	public void genFeatures(short[] sk) throws IOException {
		int n = 0;
		debug("nTime:" + nTime + " state:" + state + " sk:" + sk);
		while (n < sk.length) {
			switch (state) {
			case INIT:
				if (doFixedPoint) {
					initSamples_Fx(sk[n]);
				} else {
					initSamples(sk[n]);
				}
				n++;
				break;
			case ACQUIRE:
				// debug("Acq starting at:" + nTime);
				if (doFixedPoint) {
					acquire_Fx(sk[n]);
				} else {
					acquire(sk[n]);
				}
				n++;
				break;
			case TRACK:
				// debug("Tracking starting at:" + nTime);
				if (doFixedPoint) {
					track_Fx(sk, n);
				} else {
					track(sk, n);
				}
				n++;
				break;
			default:
				break;
			}
			// debug("nTime:" + nTime + " state:" + state);
		}
	}

	public void reset(boolean AllIncludingF0) throws IOException {
		doPreemp = true;
		this.tArray.clear();
		this.hMTimeToPitchInd.clear();
		this.hMTimeToPitchAvg.clear();
		this.hMTimeToSubbandPitch.clear();
		this.t3Array.clear();
		this.f3Array.clear();
		for (int i = 0; i < M; i++) {
			this.fArray.get(i).clear();
			this.aArray.get(i).clear();
			this.aDBArray.get(i).clear();
			this.bArray.get(i).clear();
			this.bMelArray.get(i).clear();
			this.mPar.get(i).clear();
		}
		if (UsePreEmphDataForPitch) {
			this.pOut.clear();
		}
		if (doMaxFreqStoring) {
			this.maxFreqArray.clear();
		}
		fclose();
		if (writeOutFeatures) {
			dtfLogNumber++;
			fout = new BufferedWriter(new FileWriter(fileOut + dtfLogNumber + ".xls"));
		}
		init(AllIncludingF0);
	}

	protected void init(boolean AllIncludingF0) throws IOException {
		state = State.INIT;
		nTime = 0;
		f0cnt = 0;
		f0cntMax = LL;
		f0cnt3 = 0;
		f0cnt3Max = LL / 2;
		nInit = 2;
		fmtInit[0] = F0INIT;
		fmtInit[1] = F1INIT;
		fmtInit[2] = F2INIT;
		fmtInit[3] = F3INIT;
		//
		SAMPLING_FREQUENCY_FX = fp.ConvertFloat2S13(SAMPLING_FREQUENCY);
		LOW_FREQ_THR_MIN_FX = fp.ConvertFloat2S13(LOW_FREQ_THR_MIN);
		LOW_FREQ_THR_MAX_FX = fp.ConvertFloat2S13(LOW_FREQ_THR_MAX);
		FREQ_THR_FX = fp.ConvertFloat2S13(FREQ_THR);
		FREQ_THR_MIN_FX = fp.ConvertFloat2S13(FREQ_THR_MIN);
		AMP_THR_FX = fp.ConvertFloat2S13(AMP_THR);
		rp_fx = fp.ConvertFloat2S13(rp);
		// note rz_fx = 1 gives spikes for IY sound
		rz_fx = fp.ConvertFloat2S13(rz);
		// rz_fx = fp.ConvertFloat2S13(0.99f);
		// rz_fx = fp.ConvertFloat2S13(0.8f);
		rzFC_fx = fp.ConvertFloat2S13(rzFC);
		rpre_fx = fp.ConvertFloat2S13(rPre);
		f0Initial_fx = rzFC_fx;// fp.ConvertFloat2S13(0.0f);
		f0Initial_fx = fp.DvdS13ByS13RoundS13(f0Initial_fx, SAMPLING_FREQUENCY_FX);
		// Preemp initialization
		skMinus1_fx = fp.FIXED_ZERO;
		spMinus2_fx = fp.FIXED_ZERO;
		spMinus1_fx = fp.FIXED_ZERO;
		sp_fx = fp.FIXED_ZERO;
		M_Fx = fp.ConvertInt2S13(M);
		// Initialization
		for (int ik = 0; ik < M; ik++) {
			bz_fx[ik] = rz_fx;
			bz[ik] = rz;
			bp_fx[ik] = rp_fx;
			bp[ik] = rp;
			if (rpC3 > 0.0f && ik == 3) {
				bp_fx[ik] = fp.ConvertFloat2S13(rpC3);
				bp[ik] = rpC3;
			}
			fz_fx[ik] = fp.FIXED_ZERO;
			fz[ik] = 0.0f;
			if (AllIncludingF0) {
				// f0_fx[ik] = setInitialFrequency(ik);
				f0_fx[ik] = setInitialFrequencyBasedOnFormants_Fx(ik);
				f0[ik] = setInitialFrequencyBasedOnFormants(ik);
			}
			a0_fx[ik] = fp.FIXED_ZERO;
			a0[ik] = 0.0f;
			bw0_fx[ik] = fp.FIXED_ZERO;
			bw0[ik] = 0.0f;
			gain_fx[ik] = fp.FIXED_1;
			gain[ik] = 1.0f;
			bbr_fx[ik] = fp.FIXED_ZERO;
			bbr[ik] = 0.0f;
		}
		f03_fx = f0_fx[3];
		f03 = f0[3];

		for (int ik = 0; ik < (M - 1) * M; ik++) {
			for (int k = 0; k < 3; k++) {
				hp_fx[k][ik] = fp.FIXED_ZERO;
				hp[k][ik] = 0.0f;
			}
		}
		initDSFData();
		// Set time index to zero and store all features
		int k = 0;
		writeFeatures_FxFl(k);
		addToFeatureArray_Fx(k);

		for (int ik = 0; ik < M; ik++) {
			if (doFixedPoint) {
				computeFilterPars_Fx(ik, true);
			} else {
				computeFilterPars(ik, true);
			}
		}

		for (int i = 0; i < M; i++) {
			mPar.get(i).add(0.0f);
			mPar.get(i).add(0.0f);
			mPar.get(i).add(0.0f);
		}

		fLowFreqCounter = 0;
	}

	private void initSamples_Fx(short sk) {
		if (nTime < nInit) {
			storePreEmpSamples_Fx(sp_fx);
			sp_fx = preEmp_Fx(sk, true);
			for (int ik = 0; ik < M; ik++) {
				p_fx[nTime][ik] = sp_fx;
			}
			nTime++;
		}
		if (nTime == nInit) {
			state = State.ACQUIRE;
			// debug("nTime:" + nTime + " state:" + state);
		}
	}

	private void initSamples(short sk) {
		if (nTime < nInit) {
			storePreEmpSamples(sp);
			sp = preEmp(sk, true);
			for (int ik = 0; ik < M; ik++) {
				p[nTime][ik] = sp;
			}
			nTime++;
		}
		if (nTime == nInit) {
			state = State.ACQUIRE;
			// debug("nTime:" + nTime + " state:" + state);
		}
	}

	private void acquire_Fx(short sk) throws IOException {
		long azfOut;
		if (nTime >= nInit && nTime < LL) {
			storePreEmpSamples_Fx(sp_fx);
			sp_fx = preEmp_Fx(sk, true);
			// Estimate freq during acq
			// estimateAcqFrequency(k);
			// writeFeatures(k);
			// addToFeatureArray(k);
			for (int ik = 0; ik < M; ik++) {
				azfOut = suppressNeighbours_Fx(ik, true);
				p_fx[nTime][ik] = dtfFilter_Fx(azfOut, p_fx[nTime - 1][ik], p_fx[nTime - 2][ik], gain_fx[ik],
						bbr_fx[ik], bp_fx[ik]);
				if (dataTypeForDSF == 0) {
					insertInDSFData_Fx(sp_fx, ik, false, false);
				} else if (dataTypeForDSF == 1) {
					insertInDSFData_Fx(azfOut, ik, false, false);
				} else {
					insertInDSFData_Fx(p_fx[nTime][ik], ik, false, false);
				}
			}
			if (UsePreEmphDataForPitch) {
				pOut.add((short) fp.ConvertS132Int(sp_fx));
				// pOut.add(sk);
			}
			nTime++;
		}
		if (nTime == LL) {
			f0cnt = f0cntMax;
			state = State.TRACK;
			// debug("nTime:" + nTime + " state:" + state);
		}
	}

	private void acquire(short sk) throws IOException {
		float azfOut;
		if (nTime >= nInit && nTime < LL) {
			storePreEmpSamples(sp);
			sp = preEmp(sk, true);
			// Estimate freq during acq
			// estimateAcqFrequency(k);
			// writeFeatures(k);
			// addToFeatureArray(k);
			for (int ik = 0; ik < M; ik++) {
				azfOut = suppressNeighbours(ik, true);
				p[nTime][ik] = dtfFilter(azfOut, p[nTime - 1][ik], p[nTime - 2][ik], gain[ik], bbr[ik], bp[ik]);
				if (dataTypeForDSF == 0) {
					insertInDSFData(sp, ik, false, false);
				} else if (dataTypeForDSF == 1) {
					insertInDSFData(azfOut, ik, false, false);
				} else {
					insertInDSFData(p[nTime][ik], ik, false, false);
				}
			}
			if (UsePreEmphDataForPitch) {
				pOut.add((short) sp);
				// pOut.add(sk);
			}
			nTime++;
		}
		if (nTime == LL) {
			f0cnt = f0cntMax;
			state = State.TRACK;
			// debug("nTime:" + nTime + " state:" + state);
		}
	}

	private void track_Fx(short[] s, int n) throws IOException {
		short sk = s[n];
		long azfOut;
		if (nTime >= LL) {
			storePreEmpSamples_Fx(sp_fx);
			sp_fx = preEmp_Fx(sk, false);
			// Estimate Center Frequency
			if (f0cnt == f0cntMax) {
				estimateFrequency_Fx();
				if (OverlapWindowProcessing) {
					// f0cnt = (LL / 4) + 1;// 25% overlap
					f0cnt = (LL / 2) + 1;// 50% overlap
					// f0cnt = (3 * LL / 4) + 1;// 75% overlap
				} else {
					f0cnt = 1;// 0 overlap
				}
				writeFeatures_FxFl(nTime);
				addToFeatureArray_Fx(nTime);
				if (doPitchProcessing) {
					// estimateSubBandPitches(s);
				}
			} else {
				f0cnt++;
			}
			if (doF3WithSmallerProcessingWindow) {
				if (f0cnt3 == f0cnt3Max) {
					estimateFrequency3_Fx(3);
					f0cnt3 = 1;
					addToFeatureArray3_FxFl(nTime);
				} else {
					f0cnt3++;
				}
			}
			for (int ik = 0; ik < M; ik++) {
				azfOut = suppressNeighbours_Fx(ik, false);
				shiftMatrix_fx(p_fx, ik);
				p_fx[LL - 1][ik] = dtfFilter_Fx(azfOut, p_fx[LL - 2][ik], p_fx[LL - 3][ik], gain_fx[ik], bbr_fx[ik],
						bp_fx[ik]);
				if (dataTypeForDSF == 0) {
					insertInDSFData_Fx(sp_fx, ik, true, false);
				} else if (dataTypeForDSF == 1) {
					insertInDSFData_Fx(azfOut, ik, true, false);
				} else {
					insertInDSFData_Fx(p_fx[LL - 1][ik], ik, true, false);
				}
			}
			if (UsePreEmphDataForPitch) {
				pOut.add((short) fp.ConvertS132Int(sp_fx));
				// pOut.add(sk);
			}
			nTime++;
		}
	}

	private void track(short[] s, int n) throws IOException {
		short sk = s[n];
		float azfOut;
		if (nTime >= LL) {
			storePreEmpSamples(sp);
			sp = preEmp(sk, false);
			// Estimate Center Frequency
			if (f0cnt == f0cntMax) {
				estimateFrequency();
				if (OverlapWindowProcessing) {
					// f0cnt = (LL / 4) + 1;// 25% overlap
					f0cnt = (LL / 2) + 1;// 50% overlap
					// f0cnt = (3 * LL / 4) + 1;// 75% overlap
				} else {
					f0cnt = 1;// 0 overlap
				}
				writeFeatures_FxFl(nTime);
				addToFeatureArray(nTime);
				if (doPitchProcessing) {
					// estimateSubBandPitches(s);
				}
			} else {
				f0cnt++;
			}
			if (doF3WithSmallerProcessingWindow) {
				if (f0cnt3 == f0cnt3Max) {
					estimateFrequency3(3);
					f0cnt3 = 1;
					addToFeatureArray3_FxFl(nTime);
				} else {
					f0cnt3++;
				}
			}
			for (int ik = 0; ik < M; ik++) {
				azfOut = suppressNeighbours(ik, false);
				shiftMatrix(p, ik);
				p[LL - 1][ik] = dtfFilter(azfOut, p[LL - 2][ik], p[LL - 3][ik], gain[ik], bbr[ik], bp[ik]);
				if (dataTypeForDSF == 0) {
					insertInDSFData(sp, ik, true, false);
				} else if (dataTypeForDSF == 1) {
					insertInDSFData(azfOut, ik, true, false);
				} else {
					insertInDSFData(p[LL - 1][ik], ik, true, false);
				}
			}
			if (UsePreEmphDataForPitch) {
				pOut.add((short) sp);
				// pOut.add(sk);
			}
			nTime++;
		}
	}

	private void fprintf(float x, String s) throws IOException {
		if (writeOutFeatures) {
			fout.write(x + s);
			// fout.flush();
			// fout1.write((int) x);
		}
	}

	public void fclose() throws IOException {
		if (writeOutFeatures) {
			fout.flush();
			fout.close();
			// fout1.close();
		}
	}

	private void computeACF_Fx(long[] pWavBuf, int nSamples, long nAcf) {
		int k;
		for (k = 0; k < nAcf; k++) {
			pAcf_fx[k] = computeKthAC_Fx(pWavBuf, nSamples, k);
		}
	}

	private void computeACF(float[] pWavBuf, int nSamples, long nAcf) {
		int k;
		for (k = 0; k < nAcf; k++) {
			pAcf[k] = computeKthAC(pWavBuf, nSamples, k);
		}
	}

	private long computeKthAC_Fx(long[] pWavBuf, int nSamples, int k) {
		int i;
		long acf = fp.FIXED_ZERO;
		for (i = 0; i < (nSamples - k); i++) {
			acf += fp.MpyS13ByS13RoundS13(pWavBuf[i], pWavBuf[i + k]);
		}
		return fp.DvdS13ByS13RoundS13(acf, fp.ConvertInt2S13(nSamples));
	}

	private float computeKthAC(float[] pWavBuf, int nSamples, int k) {
		int i;
		float acf = 0.0f;
		for (i = 0; i < (nSamples - k); i++) {
			acf += pWavBuf[i] * pWavBuf[i + k];
		}
		return acf / nSamples;
	}

	private void computeACF_Q_Fx(long[] pWavBuf, int nSamples, long nAcf, int nStart) {
		int k;
		for (k = 0; k < nAcf; k++) {
			pAcf_fx[k] = computeKthAC_Q_Fx(pWavBuf, nSamples, k, nStart);
		}
	}

	private void computeACF_Q(float[] pWavBuf, int nSamples, int nAcf, int nStart) {
		int k;
		for (k = 0; k < nAcf; k++) {
			pAcf[k] = computeKthAC_Q(pWavBuf, nSamples, k, nStart);
		}
	}

	private long computeKthAC_Q_Fx(long[] pWavBuf, int nSamples, int k, int nStart) {
		int i;
		long acf = fp.FIXED_ZERO;
		for (i = nStart; i < (nSamples - k); i++) {
			acf += fp.MpyS13ByS13RoundS13(pWavBuf[i], pWavBuf[i + k]);
		}
		return fp.DvdS13ByS13RoundS13(acf, fp.ConvertInt2S13(nSamples - nStart));
	}

	private float computeKthAC_Q(float[] pWavBuf, int nSamples, int k, int nStart) {
		int i;
		float acf = 0.0f;
		for (i = nStart; i < (nSamples - k); i++) {
			acf += pWavBuf[i] * pWavBuf[i + k];
		}
		return acf / (nSamples - nStart);
	}

	private void levinson2_Fx(long[] rxx) {
		long a11, rho1, a22, a21, rho2;
		// Levinson for 2-nd Order AR Process;
		// NOTE: rxx0=0 or rho1=0 => all blows up
		// if (rxx[0] == fp.FIXED_ZERO || rxx[0] ==
		// fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, rxx[1]) || (rxx[0] ==
		// rxx[1])) {
		if (rxx[0] == fp.FIXED_ZERO) {
			// debug("WARNING: rxx[0] is:" + rxx[0]);
			lpcCofs_fx[0] = 1;
			lpcCofs_fx[1] = 1;
			lpcCofs_fx[2] = 0;
		} else {
			a11 = fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, fp.DvdS13ByS13RoundS13(rxx[1], rxx[0]));
			rho1 = fp.MpyS13ByS13RoundS13((fp.FIXED_1 - fp.MpyS13ByS13RoundS13(a11, a11)), rxx[0]);
			if (rho1 == 0) {
				// debug("WARNING: rho1 is:" + rho1);
				lpcCofs_fx[0] = 1;
				lpcCofs_fx[1] = 1;
				lpcCofs_fx[2] = 0;
			} else {
				long term1 = fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, (rxx[2] + fp.MpyS13ByS13RoundS13(a11, rxx[1])));
				a22 = fp.DvdS13ByS13RoundS13(term1, rho1);
				a21 = a11 + fp.MpyS13ByS13RoundS13(a22, a11);
				rho2 = fp.MpyS13ByS13RoundS13((fp.FIXED_1 - fp.MpyS13ByS13RoundS13(a22, a22)), rho1);
				if (nTime <= LL) {
					prho_fx = fp.FIXED_ZERO;
				} else {
					prho_fx = rho2;
				}
				this.lpcCofs_fx[0] = fp.FIXED_1;
				this.lpcCofs_fx[1] = a21;
				this.lpcCofs_fx[2] = a22;
			}
		}
	}

	private void levinson2(float[] rxx) {
		float a11, rho1, a22, a21, rho2;
		// Levinson for 2-nd Order AR Process;
		// NOTE: rxx0=0 or rho1=0 => all blows up
		// if (rxx[0] == fp.FIXED_ZERO || rxx[0] ==
		// fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, rxx[1]) || (rxx[0] ==
		// rxx[1])) {
		if (rxx[0] == 0.0f) {
			// debug("WARNING: rxx[0] is:" + rxx[0]);
			lpcCofs[0] = 1;
			lpcCofs[1] = 1;
			lpcCofs[2] = 0;
		} else {
			a11 = -rxx[1] / rxx[0];
			rho1 = (1 - (a11 * a11)) * rxx[0];
			if (rho1 == 0) {
				// debug("WARNING: rho1 is:" + rho1);
				lpcCofs[0] = 1;
				lpcCofs[1] = 1;
				lpcCofs[2] = 0;
			} else {
				float term1 = -(rxx[2] + a11 * rxx[1]);
				a22 = term1 / rho1;
				a21 = a11 + a22 * a11;
				rho2 = (1 - (a22 * a22)) * rho1;
				if (nTime <= LL) {
					prho = 0.0f;
				} else {
					prho = rho2;
				}
				this.lpcCofs[0] = 1.0f;
				this.lpcCofs[1] = a21;
				this.lpcCofs[2] = a22;
			}
		}
	}

	private void writeFeatures_FxFl(int k) throws IOException {
		boolean writeForPlotting = true;
		boolean doBwtHz = true;
		if (writeForPlotting) {
			fprintf(k, "\t");
			for (int ik = 0; ik < M; ik++) {
				int freqHz = 0;
				if (doFixedPoint) {
					freqHz = (int) fp.ConvertS132Float(fp.MpyS13ByS13RoundS13(f0_fx[ik], SAMPLING_FREQUENCY_FX));
				} else {
					freqHz = (int) (f0[ik] * SAMPLING_FREQUENCY);
				}
				// MelConvert f = new MelConvert(true);
				// int melF = f.convertHzToMel(freqHz);
				fprintf(freqHz, "\t");
				// fprintf(melF, "\t");
			}
			for (int ik = 0; ik < M; ik++) {
				int freqHz = 0;
				if (doFixedPoint) {
					freqHz = (int) fp.ConvertS132Float(fp.MpyS13ByS13RoundS13(f0_fx[ik], SAMPLING_FREQUENCY_FX));
				} else {
					freqHz = (int) (f0[ik] * SAMPLING_FREQUENCY);
				}
				MelConvert f = new MelConvert(true);
				int melF = f.convertHzToMel(freqHz);
				// fprintf(freqHz, "\t");
				fprintf(melF, "\t");
			}
			for (int ik = 0; ik < M; ik++) {
				int ampInt = 0;
				if (doFixedPoint) {
					ampInt = Math.round(fp.ConvertS132Float(a0_fx[ik]));
				} else {
					ampInt = Math.round(a0[ik]);
				}
				int aDb = dFn.convertToDB(ampInt, 0, 200);
				fprintf(aDb, "\t");
				//
			}
			for (int ik = 0; ik < M; ik++) {
				if (doBwtHz) {
					int bwtMax = 200;// 0.95 is 130, 0.9 is 268, 0.85 is 413, 0.8 is 568
					int bwtScale = 400;
					// dtf has 0.9 i.e. 268 Hz so 0.95 is 130 needs to be corrected: 268+130=398
					float bwtF = 0;
					if (doFixedPoint) {
						bwtF = fp.ConvertS132Float(bw0_fx[ik]);
					} else {
						bwtF = bw0[ik];
					}
					int bwt = 0;
					if (bwtF > 0) {
						bwt = (int) (-(Math.log(bwtF) * SAMPLING_FREQUENCY) / Math.PI);
						// int bwt = (int) (1000 * bwtF);
						if (bwt > bwtMax) {
							bwt = bwtMax;
						}
						bwt = (bwt * bwtScale) / bwtMax;
					}
					fprintf(bwt, "\t");
				} else {
					int bwt = 0;
					if (doFixedPoint) {
						bwt = (int) (1000 * fp.ConvertS132Float(bw0_fx[ik]));
					} else {
						bwt = (int) (1000 * bw0[ik]);
					}
					fprintf(bwt, "\t");
				}
			}
			fprintf(111, "\n");
		} else {
			fprintf(k, "\t");
			for (int ik = 0; ik < M; ik++) {
				if (doFixedPoint) {
					fprintf(fp.ConvertS132Float(f0_fx[ik]) * SAMPLING_FREQUENCY, "\t");
				} else {
					fprintf(f0[ik] * SAMPLING_FREQUENCY, "\t");
				}
			}
			for (int ik = 0; ik < M; ik++) {
				if (doFixedPoint) {
					fprintf(fp.ConvertS132Float(a0_fx[ik]), "\t");
				} else {
					fprintf(a0[ik], "\t");
				}
			}
			for (int ik = 0; ik < M; ik++) {
				if (doFixedPoint) {
					fprintf(fp.ConvertS132Float(bw0_fx[ik]), "\t");
				} else {
					fprintf(bw0[ik], "\t");
				}
			}
			fprintf(111, "\n");
		}
	}

	private void addToFeatureArray_Fx(int k) {
		for (int j = 0; j < M; j++) {
			// FREQ
			long term1 = fp.MpyS13ByS13RoundS13(f0_fx[j], SAMPLING_FREQUENCY_FX);
			if (!outputMelFeatures) {
				int fHz = Math.round(fp.ConvertS132Float(term1));
				this.fArray.get(j).add(fHz);
				f0Hz[j] = fHz;
			} else {
				MelConvert f = new MelConvert(true);
				int floatFreq = (int) fp.ConvertS132Float(term1);
				int melF = f.convertHzToMel(floatFreq);
				this.fArray.get(j).add(melF);
				// debug("MelF:old:" + floatFreq + " new:" + melF);
			}
			// AMP
			int ampInt = Math.round(fp.ConvertS132Float(a0_fx[j]));
			this.aArray.get(j).add(ampInt);
			int aDb = dFn.convertToDB(ampInt, 0, 200);
			this.aDBArray.get(j).add(aDb);
			// BWT
			float bwInt = fp.ConvertS132Float(bw0_fx[j]);
			this.bArray.get(j).add(bwInt);
			// add bwt Mel
			int bHz = (int) (((-Math.log(bwInt)) * SAMPLING_FREQUENCY) / Math.PI);
			MelConvert f = new MelConvert(true);
			int bMel = f.convertHzToMel(bHz);
			this.bMelArray.get(j).add(bMel);
		}
		this.tArray.add(k);
		if (doMaxFreqStoring) {
			this.maxFreqArray.add(maxFreq);
		}
	}

	private void addToFeatureArray(int k) {
		for (int j = 0; j < M; j++) {
			// FREQ
			float term1 = f0[j] * SAMPLING_FREQUENCY;
			if (!outputMelFeatures) {
				int fHz = Math.round(term1);
				this.fArray.get(j).add(fHz);
				f0Hz[j] = fHz;
			} else {
				MelConvert f = new MelConvert(true);
				int floatFreq = (int) (term1);
				int melF = f.convertHzToMel(floatFreq);
				this.fArray.get(j).add(melF);
				// debug("MelF:old:" + floatFreq + " new:" + melF);
			}
			// AMP
			int ampInt = Math.round(a0[j]);
			this.aArray.get(j).add(ampInt);
			int aDb = dFn.convertToDB(ampInt, 0, 200);
			this.aDBArray.get(j).add(aDb);
			// BWT
			float bwInt = bw0[j];
			this.bArray.get(j).add(bwInt);
			// add bwt Mel
			int bHz = (int) (((-Math.log(bwInt)) * SAMPLING_FREQUENCY) / Math.PI);
			MelConvert f = new MelConvert(true);
			int bMel = f.convertHzToMel(bHz);
			this.bMelArray.get(j).add(bMel);
		}
		this.tArray.add(k);
		if (doMaxFreqStoring) {
			this.maxFreqArray.add(maxFreq);
		}
	}

	private void addToFeatureArray3_FxFl(int k) {
		if (doFixedPoint) {
			long term1 = fp.MpyS13ByS13RoundS13(f03_fx, SAMPLING_FREQUENCY_FX);
			int fHz = Math.round(fp.ConvertS132Float(term1));
			this.f3Array.add(fHz);
			this.t3Array.add(k);
		} else {
			float term1 = f03 * SAMPLING_FREQUENCY;
			int fHz = Math.round(term1);
			this.f3Array.add(fHz);
			this.t3Array.add(k);
		}
	}

	private void applyWindow_Fx(int ik) {
		for (int ik1 = 0; ik1 < LL; ik1++) {
			long term1 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fp.ConvertInt2S13(ik1));
			long term2 = fp.DvdS13ByS13RoundS13(term1, fp.ConvertInt2S13(LL - 1));
			float temp = fp.ConvertS132Float(term2);
			long term3 = fp.ConvertFloat2S13((float) Math.cos(temp));
			long term4 = fp.MpyS13ByS13RoundS13(fp.FIXED_POINT46, term3);
			long term5 = fp.FIXED_POINT54 - term4;
			pp_fx[ik1] = fp.MpyS13ByS13RoundS13(term5, p_fx[ik1][ik]);
		}
	}

	private void applyWindow(int ik) {
		for (int ik1 = 0; ik1 < LL; ik1++) {
			float term1 = (float) (2 * Math.PI * ik1);
			float term2 = term1 / (LL - 1);
			float temp = term2;
			float term3 = (float) Math.cos(temp);
			float term4 = 0.46f * term3;
			float term5 = 0.54f - term4;
			pp[ik1] = term5 * p[ik1][ik];
		}
	}

	private void applyWindow3_Fx(int ik) {
		for (int ik1 = 0; ik1 < LL; ik1++) {
			long term1 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fp.ConvertInt2S13(ik1));
			long term2 = fp.DvdS13ByS13RoundS13(term1, fp.ConvertInt2S13(LL - 1));
			float temp = fp.ConvertS132Float(term2);
			long term3 = fp.ConvertFloat2S13((float) Math.cos(temp));
			long term4 = fp.MpyS13ByS13RoundS13(fp.FIXED_POINT46, term3);
			long term5 = fp.FIXED_POINT54 - term4;
			pp_fx[ik1] = fp.MpyS13ByS13RoundS13(term5, p_fx[ik1][ik]);
		}
	}

	private void applyWindow3(int ik) {
		for (int ik1 = 0; ik1 < LL; ik1++) {
			float term1 = (float) (2 * Math.PI * ik1);
			float term2 = term1 / (LL - 1);
			float temp = term2;
			float term3 = (float) Math.cos(temp);
			float term4 = 0.46f * term3;
			float term5 = 0.54f - term4;
			pp[ik1] = term5 * p[ik1][ik];
		}
	}

	private int applyWindowConstantQ_Fx(int ik, float Q, int LL_QIn) {
		/*
		 * Formula is LL_Q = (Q*Fs)/Fc. We start from LL-LL_Q in pp_fx means we take
		 * last LL_Q samples for freq est. Problem: amp variations show up and nothing
		 * new in freq details
		 */
		int LL_Q_MIN = 20;
		int LL_Q;
		if (LL_QIn != -1) {
			// debug("Fc was " + f0Old_fx[ik]);
			long tempF = fp.MpyS13ByS13RoundS13(f0Old_fx[ik], SAMPLING_FREQUENCY_FX);
			float tempFFloat = fp.ConvertS132Float(tempF);
			// debug("Fc converted " + tempFFloat);
			LL_Q = ((int) ((Q * SAMPLING_FREQUENCY) / tempFFloat));
			if (nTime <= 4000) {
				debug("nTime:" + nTime + " ik:" + ik + " LL_Q:" + LL_Q + " for freq " + tempFFloat);
			}
		} else {
			LL_Q = LL_QIn;
		}
		if (LL_Q > LL) {
			LL_Q = LL;
		} else if (LL_Q < LL_Q_MIN) {
			LL_Q = LL_Q_MIN;
		}
		int nStart = LL - LL_Q;
		for (int ik1 = nStart; ik1 < LL; ik1++) {
			long term1 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fp.ConvertInt2S13(ik1));
			long term2 = fp.DvdS13ByS13RoundS13(term1, fp.ConvertInt2S13(LL_Q - 1));
			float temp = fp.ConvertS132Float(term2);
			long term3 = fp.ConvertFloat2S13((float) Math.cos(temp));
			long term4 = fp.MpyS13ByS13RoundS13(fp.FIXED_POINT46, term3);
			long term5 = fp.FIXED_POINT54 - term4;
			pp_fx[ik1] = fp.MpyS13ByS13RoundS13(term5, p_fx[ik1][ik]);
		}
		return nStart;
	}

	private int applyWindowConstantQ(int ik, float Q, int LL_QIn) {
		/*
		 * Formula is LL_Q = (Q*Fs)/Fc. We start from LL-LL_Q in pp means we take last
		 * LL_Q samples for freq est. Problem: amp variations show up and nothing new in
		 * freq details
		 */
		int LL_Q_MIN = 20;
		int LL_Q;
		if (LL_QIn != -1) {
			// debug("Fc was " + f0Old[ik]);
			float tempF = f0Old[ik] * SAMPLING_FREQUENCY;
			float tempFFloat = tempF;
			// debug("Fc converted " + tempFFloat);
			LL_Q = ((int) ((Q * SAMPLING_FREQUENCY) / tempFFloat));
			if (nTime <= 4000) {
				debug("nTime:" + nTime + " ik:" + ik + " LL_Q:" + LL_Q + " for freq " + tempFFloat);
			}
		} else {
			LL_Q = LL_QIn;
		}
		if (LL_Q > LL) {
			LL_Q = LL;
		} else if (LL_Q < LL_Q_MIN) {
			LL_Q = LL_Q_MIN;
		}
		int nStart = LL - LL_Q;
		for (int ik1 = nStart; ik1 < LL; ik1++) {
			float term1 = (float) (2 * Math.PI * ik1);
			float term2 = term1 / (LL_Q - 1);
			float temp = term2;
			float term3 = (float) Math.cos(temp);
			float term4 = 0.46f * term3;
			float term5 = 0.54f - term4;
			pp[ik1] = term5 * p[ik1][ik];
		}
		return nStart;
	}

	private void applyWindowAcq_Fx(int k, int ik) {
		for (int i = 0; i < k; i++) {
			long term1 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fp.ConvertInt2S13(i));
			long term2 = fp.DvdS13ByS13RoundS13(term1, fp.ConvertInt2S13(k - 1));
			float temp = fp.ConvertS132Float(term2);
			long term3 = fp.ConvertFloat2S13((float) Math.cos(temp));
			long term4 = fp.MpyS13ByS13RoundS13(fp.FIXED_POINT46, term3);
			long term5 = fp.FIXED_POINT54 - term4;
			pp_fx[i] = fp.MpyS13ByS13RoundS13(term5, p_fx[i][ik]);
		}
	}

	private void applyWindowAcq(int k, int ik) {
		for (int i = 0; i < k; i++) {
			float term1 = (float) (2 * Math.PI * i);
			float term2 = term1 / (k - 1);
			float temp = term2;
			float term3 = (float) Math.cos(temp);
			float term4 = 0.46f * term3;
			float term5 = 0.54f - term4;
			pp[i] = term5 * p[i][ik];
		}
	}

	private void rootPolynomial_Fx(int ik) {
		long term1 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[1], this.lpcCofs_fx[1]);
		long term2 = fp.MpyS13ByS13RoundS13(fp.FIXED_4, fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[0], this.lpcCofs_fx[2]));
		if ((term1 <= term2) && (this.lpcCofs_fx[2] > 0)) {
			long term3 = fp.ConvertFloat2S13((float) Math.sqrt(fp.ConvertS132Float(lpcCofs_fx[2])));
			long tempVar = fp.DvdS13ByS13RoundS13(-lpcCofs_fx[1], 2 * term3);
			if ((tempVar > fp.FIXED_MINUS1) && (tempVar < fp.FIXED_1)) {
				long term4 = fp.DvdS13ByS13RoundS13(
						fp.ConvertFloat2S13((float) Math.acos(fp.ConvertS132Float(tempVar))), fp.FIX13_2PI);
				if (term4 < fp.FIXED_HALF) {
					f0_fx[ik] = term4;
					bw0_fx[ik] = fp.ConvertFloat2S13((float) Math.sqrt(fp.ConvertS132Float(this.lpcCofs_fx[2])));
				}
			}
		}
		a0_fx[ik] = prho_fx;
		// a0_fx[ik] = a0_fx[ik] - fp.MpyS13ByS13RoundS13(rpre_fx,a0Old_fx[ik]);
	}

	private void rootPolynomial(int ik) {
		float term1 = this.lpcCofs[1] * this.lpcCofs[1];
		float term2 = 4.0f * (this.lpcCofs[0] * this.lpcCofs[2]);
		if ((term1 <= term2) && (this.lpcCofs[2] > 0)) {
			float term3 = (float) Math.sqrt(lpcCofs[2]);
			float tempVar = -lpcCofs[1] / (2 * term3);
			if ((tempVar > -1) && (tempVar < 1)) {
				float term4 = (float) (Math.acos(tempVar) / (2 * Math.PI));
				if (term4 < 0.5f) {
					f0[ik] = term4;
					bw0[ik] = (float) Math.sqrt(this.lpcCofs[2]);
				}
			}
		}
		a0[ik] = prho;
		// a0_fx[ik] = a0_fx[ik] - fp.MpyS13ByS13RoundS13(rpre_fx,a0Old_fx[ik]);
	}

	private void rootPolynomial3_Fx() {
		long term1 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[1], this.lpcCofs_fx[1]);
		long term2 = fp.MpyS13ByS13RoundS13(fp.FIXED_4, fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[0], this.lpcCofs_fx[2]));
		if ((term1 <= term2) && (this.lpcCofs_fx[2] > 0)) {
			long term3 = fp.ConvertFloat2S13((float) Math.sqrt(fp.ConvertS132Float(lpcCofs_fx[2])));
			long tempVar = fp.DvdS13ByS13RoundS13(-lpcCofs_fx[1], 2 * term3);
			if ((tempVar > fp.FIXED_MINUS1) && (tempVar < fp.FIXED_1)) {
				long term4 = fp.DvdS13ByS13RoundS13(
						fp.ConvertFloat2S13((float) Math.acos(fp.ConvertS132Float(tempVar))), fp.FIX13_2PI);
				if (term4 < fp.FIXED_HALF) {
					f03_fx = term4;
					bw03_fx = fp.ConvertFloat2S13((float) Math.sqrt(fp.ConvertS132Float(this.lpcCofs_fx[2])));
				}
			}
		}
		a03_fx = prho_fx;
		// a0_fx[ik] = a0_fx[ik] - fp.MpyS13ByS13RoundS13(rpre_fx,a0Old_fx[ik]);
	}

	private void rootPolynomial3() {
		float term1 = this.lpcCofs[1] * this.lpcCofs[1];
		float term2 = 4 * (this.lpcCofs[0] * this.lpcCofs[2]);
		if ((term1 <= term2) && (this.lpcCofs[2] > 0)) {
			float term3 = (float) Math.sqrt(lpcCofs[2]);
			float tempVar = -lpcCofs[1] / (2 * term3);
			if ((tempVar > -1) && (tempVar < 1)) {
				float term4 = (float) (Math.acos(tempVar) / (2 * Math.PI));
				if (term4 < 0.5f) {
					f03 = term4;
					bw03 = (float) Math.sqrt((this.lpcCofs[2]));
				}
			}
		}
		a03 = prho;
		// a0_fx[ik] = a0_fx[ik] - fp.MpyS13ByS13RoundS13(rpre_fx,a0Old_fx[ik]);
	}

	private long[] inverseFilter_Fx() {
		/*
		 * Error signal y[i] = this.lpcCofs[0]* pp_fx[i] - this.lpcCofs[1]*pp_fx[i-1] -
		 * this.lpcCofs[2]*pp_fx[i-2]; optional: y[i] = y[i] / prho_fx;
		 */
		long[] y = new long[pp_fx.length];
		long term1 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[0], pp_fx[0]);
		y[0] = term1;
		// y[0] = fp.DvdS13ByS13RoundS13(y[0], prho_fx);
		//
		term1 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[0], pp_fx[1]);
		long term2 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[1], pp_fx[0]);
		y[1] = term1 - term2;
		// y[1] = fp.DvdS13ByS13RoundS13(y[1], prho_fx);
		//
		for (int i = 2; i < pp_fx.length; i++) {
			term1 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[0], pp_fx[i]);
			term2 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[1], pp_fx[i - 1]);
			long term3 = fp.MpyS13ByS13RoundS13(this.lpcCofs_fx[2], pp_fx[i - 2]);
			y[i] = term1 - term2 - term3;
			// y[i] = fp.DvdS13ByS13RoundS13(y[i], prho_fx);
		}
		return y;
	}

	private float[] inverseFilter() {
		/*
		 * Error signal y[i] = this.lpcCofs[0]* pp_fx[i] - this.lpcCofs[1]*pp_fx[i-1] -
		 * this.lpcCofs[2]*pp_fx[i-2]; optional: y[i] = y[i] / prho_fx;
		 */
		float[] y = new float[pp.length];
		float term1 = this.lpcCofs[0] * pp[0];
		y[0] = term1;
		// y[0] = fp.DvdS13ByS13RoundS13(y[0], prho_fx);
		//
		term1 = this.lpcCofs[0] * pp[1];
		float term2 = this.lpcCofs[1] * pp[0];
		y[1] = term1 - term2;
		// y[1] = fp.DvdS13ByS13RoundS13(y[1], prho_fx);
		//
		for (int i = 2; i < pp.length; i++) {
			term1 = this.lpcCofs[0] * pp[i];
			term2 = this.lpcCofs[1] * pp[i - 1];
			float term3 = this.lpcCofs[2] * pp[i - 2];
			y[i] = term1 - term2 - term3;
			// y[i] = fp.DvdS13ByS13RoundS13(y[i], prho_fx);
		}
		return y;
	}

	private void storeFeatures_Fx(int m) {
		if (mPar.size() > 0 && mPar.get(m).size() > 0 && f0_fx.length > 0 && a0_fx.length > 0 && bw0_fx.length > 0) {
			mPar.get(m).set(0, fp.ConvertS132Float(f0_fx[m]));
			mPar.get(m).set(1, fp.ConvertS132Float(a0_fx[m]));
			mPar.get(m).set(2, fp.ConvertS132Float(bw0_fx[m]));
		}
	}

	private void storeFeatures(int m) {
		if (mPar.size() > 0 && mPar.get(m).size() > 0 && f0.length > 0 && a0.length > 0 && bw0.length > 0) {
			mPar.get(m).set(0, f0[m]);
			mPar.get(m).set(1, a0[m]);
			mPar.get(m).set(2, bw0[m]);
		}
	}

	private void estimateAcqFrequency_Fx(int k) {
		for (int ik = 0; ik < M; ik++) {
			applyWindowAcq_Fx(k, ik);
			computeACF_Fx(pp_fx, k, 3);
			levinson2_Fx(pAcf_fx);
			rootPolynomial_Fx(ik);
			storeFeatures_Fx(ik);
		}
		sortFrequencies_Fx();
		for (int ik = 0; ik < M; ik++) {
			computeFilterPars_Fx(ik, false);
		}
	}

	private void estimateAcqFrequency(int k) {
		for (int ik = 0; ik < M; ik++) {
			applyWindowAcq(k, ik);
			computeACF(pp, k, 3);
			levinson2(pAcf);
			rootPolynomial(ik);
			storeFeatures(ik);
		}
		sortFrequencies();
		for (int ik = 0; ik < M; ik++) {
			computeFilterPars(ik, false);
		}
	}

	private void getMaxFreqLocation_Fx() {
		// maxFreq is the fk for which ak is maximum
		long maxAmp = a0_fx[0];
		this.maxFreq = (int) (fp.ConvertS132Float(f0_fx[0]) * SAMPLING_FREQUENCY);
		for (int ik = 1; ik < M; ik++) {
			if (a0_fx[ik] > maxAmp) {
				maxAmp = a0_fx[ik];
				this.maxFreq = (int) (fp.ConvertS132Float(f0_fx[ik]) * SAMPLING_FREQUENCY);
			}
		}
	}

	private void getMaxFreqLocation() {
		// maxFreq is the fk for which ak is maximum
		float maxAmp = a0[0];
		this.maxFreq = (int) (f0[0] * SAMPLING_FREQUENCY);
		for (int ik = 1; ik < M; ik++) {
			if (a0[ik] > maxAmp) {
				maxAmp = a0[ik];
				this.maxFreq = (int) (f0[ik] * SAMPLING_FREQUENCY);
			}
		}
	}

	private void maxValue(float[] x) {
		int max = (int) x[0];
		int maxI = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i] > max) {
				max = (int) x[i];
				maxI = i;
			}
		}
		maxFreq = (int) ((maxI * SAMPLING_FREQUENCY) / (LLMaxFreq - 1));
		// maxFreqAmp = max;
	}

	private float area(int x1, int y1, int x2, int y2, int x3, int y3) {
		float out = (float) Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
		return out;
	}

	protected boolean isF1F2InsideQuad(int x, int y) {
		int tLF1 = 250;
		int tLF2 = 2200;
		int tRF1 = 300;
		int tRF2 = 725;
		int bLF1 = 700;
		int bLF2 = 1400;
		int bRF1 = 700;
		int bRF2 = 1000;
		float areaQuad = 1096875;
		// Calculate area of triangles for point P(x.y)
		float A1 = area(x, y, tLF1, tLF2, tRF1, tRF2);
		float A2 = area(x, y, tLF1, tLF2, bLF1, bLF2);
		float A3 = area(x, y, bLF1, bLF2, bRF1, bRF2);
		float A4 = area(x, y, tRF1, tRF2, bRF1, bRF2);
		// Check if sum of A1, A2 and A3 is same as A
		float QPoint = A1 + A2 + A3 + A4;
		boolean out = (areaQuad == QPoint);
		debug("isF1F2InsideQuad:" + out + " QPoint:" + QPoint + " aQ:" + areaQuad);
		return out;
	}

	private int computeError(ArrayList<Integer> aF1, ArrayList<Integer> aF2) {
		int ex = aF1.get(0) - aF2.get(0);
		int ey = aF1.get(1) - aF2.get(1);
		return (ex * ex) + (ey * ey);
	}

	private void estimatePitch(short[] s, boolean doSubband, float[] magDftX) {

	}

	private ArrayList<Integer> getA0ForPitch_FxFl() {
		ArrayList<Integer> a0ForPitch = new ArrayList<Integer>();
		for (int j = 0; j < M; j++) {
			int ampInt = 0;
			if (doFixedPoint) {
				// a0ForPitch.add(fp.ConvertS132Int(a0_fx[j]));
				ampInt = Math.round(fp.ConvertS132Float(a0_fx[j]));
			} else {
				// a0ForPitch.add(fp.ConvertS132Int(a0_fx[j]));
				ampInt = Math.round(a0[j]);
			}
			a0ForPitch.add(ampInt);
		}
		return a0ForPitch;
	}

	private void estimateFrequency_Fx() {
		boolean doFixedFilters = false;
		updateOldPars_Fx();
		for (int ik = 0; ik < M; ik++) {
			if (!doConstantQ) {
				if (doConstantQForC3 && ik == 3) {
					// int LL_Q = applyWindowConstantQ(ik, 7.5f, -1);
					int LL_Q = applyWindowConstantQ_Fx(ik, 0, 90);
					computeACF_Q_Fx(pp_fx, LL, 3, LL_Q);
				} else {
					applyWindow_Fx(ik);
					computeACF_Fx(pp_fx, LL, 3);
				}
			} else {
				int LL_Q = applyWindowConstantQ_Fx(ik, 7.5f, -1);
				computeACF_Q_Fx(pp_fx, LL, 3, LL_Q);
			}
			levinson2_Fx(pAcf_fx);
			rootPolynomial_Fx(ik);
			storeFeatures_Fx(ik);
			// sbP.runCep(pp_fx, ik, true);
		}
		sortFrequencies_Fx();
		isLowFrequency_Fx(f0_fx[0]);
		// checkFrequencyCloseness();
		doNonlinearMasking_FxFl();
		// commented below on 4/28/2019 ... see wavelet152 for before
		// checkAmplitudeLow();
		// confirmPitch();
		for (int ik = 0; ik < M; ik++) {
			storeFeatures_Fx(ik);
		}
		sortFrequencies_Fx();
		for (int ik = 0; ik < M; ik++) {
			computeFilterPars_Fx(ik, false);
		}
		if (doMaxFreqStoring) {
			getMaxFreqLocation_Fx();
		}
		if (doFixedFilters) {
			for (int ik = 0; ik < M; ik++) {
				f0_fx[ik] = f0Old_fx[ik];
				// a0_fx[ik] = a0Old_fx[ik];
				// bw0_fx[ik] = bw0Old_fx[ik];
			}
		}
	}

	private void estimateFrequency() {
		boolean doFixedFilters = false;
		updateOldPars();
		for (int ik = 0; ik < M; ik++) {
			if (!doConstantQ) {
				if (doConstantQForC3 && ik == 3) {
					// int LL_Q = applyWindowConstantQ(ik, 7.5f, -1);
					int LL_Q = applyWindowConstantQ(ik, 0, 90);
					computeACF_Q(pp, LL, 3, LL_Q);
				} else {
					applyWindow(ik);
					computeACF(pp, LL, 3);
				}
			} else {
				int LL_Q = applyWindowConstantQ(ik, 7.5f, -1);
				computeACF_Q(pp, LL, 3, LL_Q);
			}
			levinson2(pAcf);
			rootPolynomial(ik);
			storeFeatures(ik);
			// sbP.runCep(pp_fx, ik, true);
		}
		sortFrequencies();
		isLowFrequency(f0[0]);
		// checkFrequencyCloseness();
		doNonlinearMasking_FxFl();
		// commented below on 4/28/2019 ... see wavelet152 for before
		// checkAmplitudeLow();
		// confirmPitch();
		for (int ik = 0; ik < M; ik++) {
			storeFeatures(ik);
		}
		sortFrequencies();
		for (int ik = 0; ik < M; ik++) {
			computeFilterPars(ik, false);
		}
		if (doMaxFreqStoring) {
			getMaxFreqLocation();
		}
		if (doFixedFilters) {
			for (int ik = 0; ik < M; ik++) {
				f0[ik] = f0Old[ik];
				// a0_fx[ik] = a0Old_fx[ik];
				// bw0_fx[ik] = bw0Old_fx[ik];
			}
		}
	}

	private void estimateFrequency3_Fx(int ik) {
		boolean dontEstimate = false;
		if (dontEstimate) {
			return;
		}
		if (doConstantQForF3Faster) {
			int LL_Q = applyWindowConstantQ_Fx(ik, 0, f0cnt3Max);
			computeACF_Q_Fx(pp_fx, LL, 3, LL_Q);
		} else {
			applyWindow3_Fx(ik);
			computeACF_Fx(pp_fx, LL, 3);
		}
		levinson2_Fx(pAcf_fx);
		rootPolynomial3_Fx();
	}

	private void estimateFrequency3(int ik) {
		boolean dontEstimate = false;
		if (dontEstimate) {
			return;
		}
		if (doConstantQForF3Faster) {
			int LL_Q = applyWindowConstantQ(ik, 0, f0cnt3Max);
			computeACF_Q(pp, LL, 3, LL_Q);
		} else {
			applyWindow3(ik);
			computeACF(pp, LL, 3);
		}
		levinson2(pAcf);
		rootPolynomial3();
	}

	private void updateOldPars_Fx() {
		for (int ik = 0; ik < M; ik++) {
			f0Old_fx[ik] = f0_fx[ik];
			a0Old_fx[ik] = a0_fx[ik];
			bw0Old_fx[ik] = bw0_fx[ik];
		}
	}

	private void updateOldPars() {
		for (int ik = 0; ik < M; ik++) {
			f0Old[ik] = f0[ik];
			a0Old[ik] = a0[ik];
			bw0Old[ik] = bw0[ik];
		}
	}

	private void arraySort() {
		Collections.sort(mPar, new Comparator<ArrayList<Float>>() {

			public int compare(ArrayList<Float> floats, ArrayList<Float> otherFloats) {
				return floats.get(0).compareTo(otherFloats.get(0));
			}
		});
	}

	private void sortFrequencies_Fx() {
		if (mPar.size() == 0 || mPar.get(0).size() == 0) {
			return;
		}
		arraySort();
		for (int i = 0; i < M; i++) {
			f0_fx[i] = fp.ConvertFloat2S13(mPar.get(i).get(0));
			a0_fx[i] = fp.ConvertFloat2S13(mPar.get(i).get(1));
			bw0_fx[i] = fp.ConvertFloat2S13(mPar.get(i).get(2));
		}
	}

	private void sortFrequencies() {
		if (mPar.size() == 0 || mPar.get(0).size() == 0) {
			return;
		}
		arraySort();
		for (int i = 0; i < M; i++) {
			f0[i] = mPar.get(i).get(0);
			a0[i] = mPar.get(i).get(1);
			bw0[i] = mPar.get(i).get(2);
		}
	}

	private void shiftMatrix_fx(long[][] m, int ik) {
		for (int ik3 = 0; ik3 < m.length - 1; ik3++) {
			m[ik3][ik] = m[ik3 + 1][ik];
		}
	}

	private void shiftMatrix(float[][] m, int ik) {
		for (int ik3 = 0; ik3 < m.length - 1; ik3++) {
			m[ik3][ik] = m[ik3 + 1][ik];
		}
	}

	private void shiftMatrixS(short[][] m, int ik) {
		for (int ik3 = 0; ik3 < m.length - 1; ik3++) {
			m[ik3][ik] = m[ik3 + 1][ik];
		}
	}

	private void shiftVector(short[] m) {
		for (int ik = 0; ik < m.length - 1; ik++) {
			m[ik] = m[ik + 1];
		}
	}

	private void getNeighbouringFilterPars_Fx(int ik, boolean acquire) {
		if (acquire) {
			for (int ik2 = 0; ik2 < ik; ik2++) {
				fz_fx[ik2] = f0_fx[ik2];
				bz1_fx[ik2] = bz_fx[ik2];
			}
		} else {
			if (ik == 0) {
				for (int ik2 = 1; ik2 < M; ik2++) {
					fz_fx[ik2 - 1] = f0_fx[ik2];
					bz1_fx[ik2 - 1] = bz_fx[ik2];
				}
			} else if (ik == M - 1) {
				for (int ik2 = 0; ik2 < M - 1; ik2++) {
					fz_fx[ik2] = f0_fx[ik2];
					bz1_fx[ik2] = bz_fx[ik2];
				}
			} else {
				for (int ik2 = 0; ik2 < ik; ik2++) {
					fz_fx[ik2] = f0_fx[ik2];
					bz1_fx[ik2] = bz_fx[ik2];
				}
				for (int ik2 = ik + 1; ik2 < M; ik2++) {
					fz_fx[ik2 - 1] = f0_fx[ik2];
					bz1_fx[ik2 - 1] = bz_fx[ik2];
				}
			}
		}
	}

	private void getNeighbouringFilterPars(int ik, boolean acquire) {
		if (acquire) {
			for (int ik2 = 0; ik2 < ik; ik2++) {
				fz[ik2] = f0[ik2];
				bz1[ik2] = bz[ik2];
			}
		} else {
			if (ik == 0) {
				for (int ik2 = 1; ik2 < M; ik2++) {
					fz[ik2 - 1] = f0[ik2];
					bz1[ik2 - 1] = bz[ik2];
				}
			} else if (ik == M - 1) {
				for (int ik2 = 0; ik2 < M - 1; ik2++) {
					fz[ik2] = f0[ik2];
					bz1[ik2] = bz[ik2];
				}
			} else {
				for (int ik2 = 0; ik2 < ik; ik2++) {
					fz[ik2] = f0[ik2];
					bz1[ik2] = bz[ik2];
				}
				for (int ik2 = ik + 1; ik2 < M; ik2++) {
					fz[ik2 - 1] = f0[ik2];
					bz1[ik2 - 1] = bz[ik2];
				}
			}
		}
	}

	private long dtfFilter_Fx(long x, long yMinus1, long yMinus2, long gain, long bbr, long bp) {
		long term1 = fp.MpyS13ByS13RoundS13(x, gain);
		long term2 = fp.MpyS13ByS13RoundS13(fp.FIXED_2, bbr);
		term2 = fp.MpyS13ByS13RoundS13(term2, yMinus1);
		long term3 = fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, bp);
		term3 = fp.MpyS13ByS13RoundS13(term3, bp);
		term3 = fp.MpyS13ByS13RoundS13(term3, yMinus2);
		return term1 + term2 + term3;
	}

	private float dtfFilter(float x, float yMinus1, float yMinus2, float gain, float bbr, float bp) {
		float term1 = x * gain;
		float term2 = 2 * bbr;
		term2 = term2 * yMinus1;
		float term3 = -bp;
		term3 = term3 * bp;
		term3 = term3 * yMinus2;
		return term1 + term2 + term3;
	}

	private long azfFilter_Fx(long x, long xMinus1, long xMinus2, long bz1, long fz) {
		long term1 = fp.MpyS13ByS13RoundS13(fp.FIXED_2, bz1);
		long term2 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fz);
		float temp = (float) Math.cos(fp.ConvertS132Float(term2));
		term2 = fp.ConvertFloat2S13(temp);
		term2 = fp.MpyS13ByS13RoundS13(term1, term2);
		term2 = fp.MpyS13ByS13RoundS13(term2, xMinus1);
		term2 = fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, term2);
		long term3 = fp.MpyS13ByS13RoundS13(bz1, bz1);
		term3 = fp.MpyS13ByS13RoundS13(term3, xMinus2);
		return x + term2 + term3;
	}

	private float azfFilter(float x, float xMinus1, float xMinus2, float bz1, float fz) {
		float term1 = 2 * bz1;
		float term2 = (float) (2 * Math.PI * fz);
		float temp = (float) Math.cos(term2);
		term2 = temp;
		term2 = term1 * term2;
		term2 = term2 * xMinus1;
		term2 = -term2;
		float term3 = bz1 * bz1;
		term3 = term3 * xMinus2;
		return x + term2 + term3;
	}

	private long computeAzfGain_Fx(int ik, int count1) {
		long term1 = fp.FIXED_1;
		long term2 = fp.MpyS13ByS13RoundS13(bz1_fx[count1], bz1_fx[count1]);
		long term3 = fp.MpyS13ByS13RoundS13(fp.FIXED_MINUS1, fp.FIXED_2);
		term3 = fp.MpyS13ByS13RoundS13(term3, bz1_fx[count1]);
		long term4 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fz_fx[count1] + f0_fx[ik]);
		term4 = fp.ConvertFloat2S13((float) Math.cos(fp.ConvertS132Float(term4)));
		long term5 = fp.MpyS13ByS13RoundS13(term3, term4);
		float temp = (float) Math.sqrt(fp.ConvertS132Float(term1 + term2 + term5));
		term5 = fp.ConvertFloat2S13(temp);
		term4 = fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, fz_fx[count1] - f0_fx[ik]);
		term4 = fp.ConvertFloat2S13((float) Math.cos(fp.ConvertS132Float(term4)));
		term3 = fp.MpyS13ByS13RoundS13(term3, term4);
		temp = (float) Math.sqrt(fp.ConvertS132Float(term1 + term2 + term3));
		return fp.MpyS13ByS13RoundS13(term5, fp.ConvertFloat2S13(temp));
	}

	private float computeAzfGain(int ik, int count1) {
		float term1 = 1.0f;
		float term2 = bz1[count1] * bz1[count1];
		float term3 = -2.0f;
		term3 = term3 * bz1[count1];
		float term4 = (float) (2 * Math.PI * (fz[count1] + f0[ik]));
		term4 = (float) Math.cos(term4);
		float term5 = term3 * term4;
		float temp = (float) Math.sqrt(term1 + term2 + term5);
		term5 = temp;
		term4 = (float) (2 * Math.PI * (fz[count1] - f0[ik]));
		term4 = (float) Math.cos(term4);
		term3 = term3 * term4;
		temp = (float) Math.sqrt(term1 + term2 + term3);
		return term5 * temp;
	}

	private long computeDtfGain_Fx(int ik) {
		long term1 = fp.FIXED_1 - bp_fx[ik];
		long term2 = fp.FIXED_1 + fp.MpyS13ByS13RoundS13(bp_fx[ik], bp_fx[ik]);
		long term3 = fp.MpyS13ByS13RoundS13(-fp.FIXED_2, bp_fx[ik]);
		long term4 = fp.MpyS13ByS13RoundS13(fp.FIXED_2, fp.FIX13_2PI);
		term4 = fp.MpyS13ByS13RoundS13(term4, f0_fx[ik]);
		float temp = (float) Math.cos(fp.ConvertS132Float(term4));
		term4 = fp.ConvertFloat2S13(temp);
		term3 = fp.MpyS13ByS13RoundS13(term3, term4);
		temp = fp.ConvertS132Float(term2 + term3);
		temp = (float) Math.sqrt(temp);
		term2 = fp.ConvertFloat2S13(temp);
		return fp.MpyS13ByS13RoundS13(term1, term2);
		// return (1.0f - bp_fx[ik]) * ((float) Math.sqrt(1.0f + bp_fx[ik] * bp_fx[ik] -
		// 2.0f * bp_fx[ik] * ((float) Math.cos(2.0f * 2.0f * Math.PI * f0_fx[ik]))));
	}

	private float computeDtfGain(int ik) {
		float term1 = 1 - bp[ik];
		float term2 = 1 + bp[ik] * bp[ik];
		float term3 = -2 * bp[ik];
		float term4 = (float) (2 * 2 * Math.PI);
		term4 = term4 * f0[ik];
		float temp = (float) Math.cos(term4);
		term4 = temp;
		term3 = term3 * term4;
		temp = term2 + term3;
		temp = (float) Math.sqrt(temp);
		term2 = temp;
		return term1 * term2;
		// return (1.0f - bp_fx[ik]) * ((float) Math.sqrt(1.0f + bp_fx[ik] * bp_fx[ik] -
		// 2.0f * bp_fx[ik] * ((float) Math.cos(2.0f * 2.0f * Math.PI * f0_fx[ik]))));
	}

	private long computeCascadeGain_Fx(long azfGain, int ik) {
		// added this because o.w gain_fx may blow-up
		if (azfGain < fp.FIXED_HALF) {
			azfGain = fp.FIXED_HALF;
		}
		return fp.DvdS13ByS13RoundS13(computeDtfGain_Fx(ik), azfGain);
	}

	private float computeCascadeGain(float azfGain, int ik) {
		// added this because o.w gain_fx may blow-up
		if (azfGain < 0.5F) {
			azfGain = 0.5f;
		}
		return computeDtfGain(ik) / azfGain;
	}

	private long computeBbr_Fx(int ik) {
		float temp = (float) Math.cos(fp.ConvertS132Float(fp.MpyS13ByS13RoundS13(fp.FIX13_2PI, f0_fx[ik])));
		return fp.MpyS13ByS13RoundS13(bp_fx[ik], fp.ConvertFloat2S13(temp));
	}

	private float computeBbr(int ik) {
		float temp = (float) Math.cos(2 * Math.PI * f0[ik]);
		return bp[ik] * temp;
	}

	private long suppressNeighbours_Fx(int ik, boolean acquire) {
		if (ik == 0 && acquire) {
			return sp_fx;
		} else {
			getNeighbouringFilterPars_Fx(ik, acquire);
			int ik1 = (M - 1) * ik;
			int count1 = 0;
			shiftMatrix_fx(hp_fx, ik1);
			hp_fx[2][ik1] = azfFilter_Fx(sp_fx, spMinus1_fx, spMinus2_fx, bz1_fx[count1], fz_fx[count1]);
			ik1 = ik1 + 1;
			count1 = count1 + 1;
			if (acquire) {
				while (count1 < ik) {
					shiftMatrix_fx(hp_fx, ik1);
					hp_fx[2][ik1] = azfFilter_Fx(hp_fx[2][ik1 - 1], hp_fx[1][ik1 - 1], hp_fx[0][ik1 - 1],
							bz1_fx[count1], fz_fx[count1]);
					count1 = count1 + 1;
					ik1 = ik1 + 1;
				}
			} else {
				while (count1 < M - 1) {
					shiftMatrix_fx(hp_fx, ik1);
					hp_fx[2][ik1] = azfFilter_Fx(hp_fx[2][ik1 - 1], hp_fx[1][ik1 - 1], hp_fx[0][ik1 - 1],
							bz1_fx[count1], fz_fx[count1]);
					count1 = count1 + 1;
					ik1 = ik1 + 1;
				}
			}
			return hp_fx[2][ik1 - 1];
		}
	}

	private float suppressNeighbours(int ik, boolean acquire) {
		if (ik == 0 && acquire) {
			return sp;
		} else {
			getNeighbouringFilterPars(ik, acquire);
			int ik1 = (M - 1) * ik;
			int count1 = 0;
			shiftMatrix(hp, ik1);
			hp[2][ik1] = azfFilter(sp, spMinus1, spMinus2, bz1[count1], fz[count1]);
			ik1 = ik1 + 1;
			count1 = count1 + 1;
			if (acquire) {
				while (count1 < ik) {
					shiftMatrix(hp, ik1);
					hp[2][ik1] = azfFilter(hp[2][ik1 - 1], hp[1][ik1 - 1], hp[0][ik1 - 1], bz1[count1], fz[count1]);
					count1 = count1 + 1;
					ik1 = ik1 + 1;
				}
			} else {
				while (count1 < M - 1) {
					shiftMatrix(hp, ik1);
					hp[2][ik1] = azfFilter(hp[2][ik1 - 1], hp[1][ik1 - 1], hp[0][ik1 - 1], bz1[count1], fz[count1]);
					count1 = count1 + 1;
					ik1 = ik1 + 1;
				}
			}
			return hp[2][ik1 - 1];
		}
	}

	private void computeFilterPars_Fx(int ik, boolean acquire) {
		if (ik == 0 && acquire) {
			gain_fx[ik] = computeDtfGain_Fx(ik);
			bbr_fx[ik] = computeBbr_Fx(ik);
		} else {
			getNeighbouringFilterPars_Fx(ik, acquire);
			int count1 = 0;
			gain_fx[ik] = computeAzfGain_Fx(ik, count1);
			count1 = count1 + 1;
			if (acquire) {
				while (count1 < ik) {
					gain_fx[ik] = fp.MpyS13ByS13RoundS13(gain_fx[ik], computeAzfGain_Fx(ik, count1));
					count1 = count1 + 1;
				}
			} else {
				while (count1 < M - 1) {
					gain_fx[ik] = fp.MpyS13ByS13RoundS13(gain_fx[ik], computeAzfGain_Fx(ik, count1));
					count1 = count1 + 1;
				}
			}
			gain_fx[ik] = computeCascadeGain_Fx(gain_fx[ik], ik);
			bbr_fx[ik] = computeBbr_Fx(ik);
		}
	}

	private void computeFilterPars(int ik, boolean acquire) {
		if (ik == 0 && acquire) {
			gain[ik] = computeDtfGain(ik);
			bbr[ik] = computeBbr(ik);
		} else {
			getNeighbouringFilterPars(ik, acquire);
			int count1 = 0;
			gain[ik] = computeAzfGain(ik, count1);
			count1 = count1 + 1;
			if (acquire) {
				while (count1 < ik) {
					gain[ik] = gain[ik] * computeAzfGain(ik, count1);
					count1 = count1 + 1;
				}
			} else {
				while (count1 < M - 1) {
					gain[ik] = gain[ik] * computeAzfGain(ik, count1);
					count1 = count1 + 1;
				}
			}
			gain[ik] = computeCascadeGain(gain[ik], ik);
			bbr[ik] = computeBbr(ik);
		}
	}

	private void storePreEmpSamples_Fx(long sp) {
		this.spMinus2_fx = this.spMinus1_fx;
		this.spMinus1_fx = sp;
	}

	private void storePreEmpSamples(float sp) {
		this.spMinus2 = this.spMinus1;
		this.spMinus1 = sp;
	}

	private long preEmp_Fx(short sk1, boolean acquire) {
		long sk;
		long y;
		sk = fp.ConvertInt2S13(sk1);
		if (this.rpre_fx > 0 && doPreemp) {
			y = sk - fp.MpyS13ByS13RoundS13(rpre_fx, skMinus1_fx);
			this.skMinus1_fx = sk;
		} else {
			y = sk;
		}
		return y;
	}

	private short preEmp(short sk1, boolean acquire) {
		short sk;
		short y;
		sk = sk1;
		if (this.rPre > 0 && doPreemp) {
			y = (short) (sk - rPre * skMinus1);
			this.skMinus1 = sk;
		} else {
			y = sk;
		}
		return y;
	}

	private void isLowFrequency_Fx(long f) {
		if ((f > LOW_FREQ_THR_MIN_FX) && (f < LOW_FREQ_THR_MAX_FX)) {
			++fLowFreqCounter;
		}
	}

	private void isLowFrequency(float f) {
		if ((f > LOW_FREQ_THR_MIN) && (f < LOW_FREQ_THR_MAX)) {
			++fLowFreqCounter;
		}
	}

	private void checkMinFreqUsingThr_FxFl(boolean doThis) {
		if (!doThis) {
			return;
		}
		if (doFixedPoint) {
			if (f0_fx[0] < LOW_FREQ_THR_MAX_FX) {
				f0_fx[0] = LOW_FREQ_THR_MAX_FX;
				// because u moved f0_fx[0] up recheck all
				for (int ik = 1; ik < M; ik++) {
					if (f0_fx[ik] <= f0_fx[ik - 1]) {
						f0_fx[ik] = f0_fx[ik - 1] + LOW_FREQ_THR_MAX_FX;
					}
				}
			}
		} else {
			if (f0[0] < LOW_FREQ_THR_MAX) {
				f0[0] = LOW_FREQ_THR_MAX;
				// because u moved f0[0] up recheck all
				for (int ik = 1; ik < M; ik++) {
					if (f0[ik] <= f0[ik - 1]) {
						f0[ik] = f0[ik - 1] + LOW_FREQ_THR_MAX;
					}
				}
			}
		}
	}

	private void doNonlinearMasking_FxFl() {
		/*
		 * make sure freqs are sorted. Pick max across channels for all that are
		 * not-masked, find freq of that channel, check if left and right are < thr, if
		 * so set masking to 0 and repeat
		 * 
		 * create mask vector and set it to 1 create a new vector of a0_fx int set up
		 * loop
		 */
		// debug9("Entered:nTime:" + nTime + " -------------");
		// Create aFHz
		int Freq_Thr_For_Masking = 250;
		int[] aFHz = new int[M];
		for (int j = 0; j < M; j++) {
			int fHz = 0;
			if (doFixedPoint) {
				fHz = Math.round(fp.ConvertS132Float(fp.MpyS13ByS13RoundS13(f0_fx[j], SAMPLING_FREQUENCY_FX)));
			} else {
				fHz = Math.round(f0[j] * SAMPLING_FREQUENCY);
			}
			aFHz[j] = fHz;
		}
		// Init mask ( 1 is not masked and 0 means masked
		checkMinFreqUsingThr_FxFl(false);
		ArrayList<Integer> mask = new ArrayList<Integer>();
		for (int ik = 0; ik < M; ik++) {
			mask.add(1);
		}
		// Create masker and unmaskedA
		ArrayList<Integer> masker = new ArrayList<Integer>();
		ArrayList<Integer> unMaskedA = new ArrayList<Integer>();
		ArrayList<Integer> intUnMaskedA = new ArrayList<Integer>();
		for (int ik = 0; ik < M; ik++) {
			if (mask.get(ik) == 1) {
				int ampInt = 0;
				if (doFixedPoint) {
					ampInt = Math.round(fp.ConvertS132Float(a0_fx[ik]));
				} else {
					ampInt = Math.round(a0[ik]);
				}
				// int aDb = StarsDecoder.convertToDB(ampInt, 0, 200);
				unMaskedA.add(ampInt);
				intUnMaskedA.add(ik);
			}
		}
		int nUMA = unMaskedA.size();
		// Iterate till you find valid maxA0
		while (nUMA > 0) {
			// debug9("nUMA:" + nUMA);
			// Find max in a0_fx
			int kA0Max = 0;
			int a0Max = 0;
			for (int ik = 0; ik < nUMA; ik++) {
				int iA0 = intUnMaskedA.get(ik);
				int a0 = unMaskedA.get(ik);
				if (ik == 0 || (a0 > a0Max)) {
					a0Max = a0;
					kA0Max = iA0;
				}
			}
			// set the masker so u dont find its max in next iter
			masker.add(kA0Max);
			// mask right
			if (((kA0Max + 1) < M) && ((aFHz[kA0Max + 1] - aFHz[kA0Max]) < Freq_Thr_For_Masking)) {
				// if (((kA0Max + 1) < M) && ((f0_fx[kA0Max + 1] - f0_fx[kA0Max]) <
				// FREQ_THR_FX)) {
				if (doFixedPoint) {
					f0_fx[kA0Max + 1] = f0Old_fx[kA0Max + 1];
				} else {
					f0[kA0Max + 1] = f0Old[kA0Max + 1];
				}
				mask.set(kA0Max + 1, 0);
			}
			// mask left
			if (((kA0Max - 1) >= 0) && ((aFHz[kA0Max] - aFHz[kA0Max - 1]) < Freq_Thr_For_Masking)) {
				// if (((kA0Max - 1) >= 0) && ((f0_fx[kA0Max] - f0_fx[kA0Max - 1]) <
				// FREQ_THR_FX)) {
				if (doFixedPoint) {
					f0_fx[kA0Max - 1] = f0Old_fx[kA0Max - 1];
				} else {
					f0[kA0Max - 1] = f0Old[kA0Max - 1];
				}
				mask.set(kA0Max - 1, 0);
			}
			// debug9("kA0Max:" + kA0Max + " mask:" + mask + " masker:" + masker);
			// for next iter
			unMaskedA = new ArrayList<Integer>();
			intUnMaskedA = new ArrayList<Integer>();
			for (int ik = 0; ik < M; ik++) {
				if (!masker.contains(ik) && mask.get(ik) == 1) {
					int ampInt = Math.round(fp.ConvertS132Float(a0_fx[ik]));
					// int aDb = StarsDecoder.convertToDB(ampInt, 0, 200);
					unMaskedA.add(ampInt);
					intUnMaskedA.add(ik);
				}
			}
			nUMA = unMaskedA.size();
			// debug9("nUMA:" + nUMA);
		}
		if (mask.contains(0)) {
			debug("mask:" + mask + " nTime:" + nTime);
		}
		// store in maskArray for future use, plot etc
	}

	private void checkAmplitudeLow_Fx() {
		long aMax = a0_fx[0];
		for (int ik = 1; ik < (M - 1); ik++) {
			if (a0_fx[ik] > aMax) {
				aMax = a0_fx[ik];
			}
		}
		if (aMax == 0) {
			return;
		}
		for (int ik = 0; ik < (M - 1); ik++) {
			long aRatio = fp.DvdS13ByS13RoundS13(a0_fx[ik], aMax);
			if (aRatio < AMP_THR_FX) {
				debug("checkAmplitudeLow:Amp:ik:" + ik + " :" + a0_fx[0] + " " + a0_fx[1] + " " + a0_fx[2] + " "
						+ a0_fx[3]);
				f0_fx[ik] = f0Old_fx[ik];
				a0_fx[ik] = a0Old_fx[ik];
				bw0_fx[ik] = bw0Old_fx[ik];
			}
		}
	}

	private void checkAmplitudeLow() {
		float aMax = a0[0];
		for (int ik = 1; ik < (M - 1); ik++) {
			if (a0[ik] > aMax) {
				aMax = a0[ik];
			}
		}
		if (aMax == 0) {
			return;
		}
		for (int ik = 0; ik < (M - 1); ik++) {
			float aRatio = a0[ik] / aMax;
			if (aRatio < AMP_THR) {
				debug("checkAmplitudeLow:Amp:ik:" + ik + " :" + a0[0] + " " + a0[1] + " " + a0[2] + " " + a0[3]);
				f0[ik] = f0Old[ik];
				a0[ik] = a0Old[ik];
				bw0[ik] = bw0Old[ik];
			}
		}
	}

	private long setInitialFrequency_Fx(int ik) {
		long term1 = fp.DvdS13ByS13RoundS13(SAMPLING_FREQUENCY_FX, fp.FIXED_2);
		long term2 = term1 - fp.ConvertFloat2S13(200);
		long term3 = fp.DvdS13ByS13RoundS13(term2, M_Fx);
		long term4 = fp.MpyS13ByS13RoundS13(term3, fp.ConvertInt2S13(ik + 1));
		return fp.DvdS13ByS13RoundS13(term4, SAMPLING_FREQUENCY_FX);
	}

	private float setInitialFrequency(int ik) {
		float term1 = SAMPLING_FREQUENCY / 2.0f;
		float term2 = term1 - 200.0f;
		float term3 = term2 / M;
		float term4 = term3 * (ik + 1);
		return term4 / SAMPLING_FREQUENCY;
	}

	private long setInitialFrequencyBasedOnFormants_Fx(int ik) {
		float f = fmtInit[ik];
		return fp.ConvertFloat2S13(f);
	}

	private float setInitialFrequencyBasedOnFormants(int ik) {
		float f = fmtInit[ik];
		return f;
	}

	private void initDSFData() {
		for (int ik = 0; ik < M; ik++) {
			for (int k = 0; k < PitchWindow; k++) {
				dsfOut[k][ik] = 0;
			}
		}
	}

	private void initMelData() {
		for (int k = 0; k < PitchWindow; k++) {
			melIn[k] = 0;
		}
	}

	private void insertInDSFData_Fx(long x, int ik, boolean doShift, boolean doLogEnv) {
		/*
		 * Output of the Dynamic Suppression Filter for each channel...to compute
		 * log-env and pitch
		 * 
		 * We shift the matrix, convert the azfout into short and then insert for each
		 * channel
		 */
		if (doShift) {
			shiftMatrixS(dsfOut, ik);
		}
		short sX = ((short) fp.ConvertS132Int(x));
		if (doLogEnv) {
			short lESx = (short) Math.log10(sX);
			dsfOut[PitchWindow - 1][ik] = lESx;
		} else {
			dsfOut[PitchWindow - 1][ik] = sX;
		}
	}

	private void insertInDSFData(float x, int ik, boolean doShift, boolean doLogEnv) {
		/*
		 * Output of the Dynamic Suppression Filter for each channel...to compute
		 * log-env and pitch
		 * 
		 * We shift the matrix, convert the azfout into short and then insert for each
		 * channel
		 */
		if (doShift) {
			shiftMatrixS(dsfOut, ik);
		}
		short sX = ((short) x);
		if (doLogEnv) {
			short lESx = (short) Math.log10(sX);
			dsfOut[PitchWindow - 1][ik] = lESx;
		} else {
			dsfOut[PitchWindow - 1][ik] = sX;
		}
	}

	private void insertDataForMel(short x) {
		/*
		 */
		shiftVector(melIn);
		melIn[PitchWindow - 1] = x;
	}

	private short[] getDataForHarFb(int ik, int LL1) {
		short[] pOut = new short[LL1];
		for (int i = 0; i < LL1; i++) {
			pOut[i] = dsfOut[i][ik];
		}
		return pOut;
	}

	private short[] getDataForMag(short[] s) {
		short[] px = null;
		px = new short[PitchWindow];
		for (int i = 0; i < PitchWindow; i++) {
			if (UsePreEmphDataForPitch) {
				px[i] = pOut.get(pOut.size() - PitchWindow + i);
			} else {
				px[i] = s[s.length - PitchWindow + i];
			}
		}
		return px;
	}

	private void debug(String message) {
		// Keying.debug(message, getClass().getName());
	}
}
