package com.example.tfbapp;

import java.util.ArrayList;
import java.util.Collections;

public class DspFunctions {

	protected int convertToDB(int x, int floorVal, int ceilVal) {
		/*
		 * int floorVal = 0; int ceilVal = 200;
		 */
		int xDb = (int) (10 * Math.log10(x));
		if (floorVal != -1 && xDb <= floorVal) {
			xDb = floorVal;
		}
		if (ceilVal != -1 && xDb >= ceilVal) {
			xDb = ceilVal;
		}
		return xDb;
	}

	protected int calculateMelFromHz(int f) {
		/*
		 * $f_k/(c_1f_k + c_2)$, where $c_1 = 0.0004, c_2=603$ for $f_k \le 1000$, and
		 * $c_1 = 0.000244, c_2=773$ for $f_k > 1000$. $b_k$ is
		 * 
		 * O'Shaughnessy's book: 2595 log10(1 + (f/700));
		 */
		boolean useUmeshFormula = false;
		if (useUmeshFormula) {
			double c1;
			double c2;
			if (f <= 1000) {
				c1 = 0.0004f;
				c2 = 0.603f;
			} else {
				c1 = 0.000244f;
				c2 = 0.773f;
			}
			int mF = (int) (f / (c1 * f + c2));
			return mF;
		} else {
			double c1 = 2595.0;
			double c2 = 700.0;
			int mF = (int) (c1 * (Math.log10(1 + (f / c2))));
			return mF;
		}
	}

	protected ArrayList<Integer> getAmplitudeRatiosdB(ArrayList<Integer> a, boolean normalizeByMax, boolean doAmpDB,
			boolean doCap4DB, boolean doCap4Norm) {
		if (!doAmpDB || a == null) {
			return a;
		}
		int floorValN = -200;
		int ceilValN = 200;
		int floorVal = 0;
		int ceilVal = 200;
		ArrayList<Integer> aRat = new ArrayList<Integer>();
		if (normalizeByMax) {
			float maxA = (float) Collections.max(a);
			if (maxA == 0) {
				return null;
			}
			for (int j = 0; j < a.size(); j++) {
				float rat = ((float) a.get(j)) / maxA;
				int aDb = (int) (10 * Math.log10(rat));
				if (doCap4Norm && aDb <= floorValN) {
					aDb = floorValN;
				}
				if (doCap4Norm && aDb >= ceilValN) {
					aDb = ceilValN;
				}
				aRat.add((int) (aDb));
			}
		} else {
			for (int j = 0; j < a.size(); j++) {
				int aDb = (int) (10 * Math.log10(a.get(j)));
				if (doCap4DB && aDb <= floorVal) {
					aDb = floorVal;
				}
				if (doCap4DB && aDb >= ceilVal) {
					aDb = ceilVal;
				}
				aRat.add(aDb);
			}
		}
		return aRat;
	}

	protected ArrayList<ArrayList<Integer>> transformFrequencies(ArrayList<ArrayList<Integer>> freq) {
		int M = freq.size();
		int num = freq.get(0).size();
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < num; j++) {
				float cFreq = freq.get(i).get(j);
				int melF;
				melF = calculateMelFromHz((int) cFreq);
				freq.get(i).set(j, melF);
			}
		}
		return freq;
	}

	protected ArrayList<Integer> transformFrequencyArray(ArrayList<Integer> freq) {
		int num = freq.size();
		for (int i = 0; i < num; i++) {
			float cFreq = freq.get(i);
			int melF;
			melF = calculateMelFromHz((int) cFreq);
			freq.set(i, melF);
		}
		return freq;
	}

	private void debug(String m) {
		// System.out.println("dD:" + m);
	}
}
