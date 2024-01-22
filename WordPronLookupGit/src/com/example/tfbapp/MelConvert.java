package com.example.tfbapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class MelConvert {

	int Fs = 4000;
	int closestF;
	ArrayList<Integer> hScale = new ArrayList<Integer>();
	HashMap<Integer, Integer> hToMScale = new HashMap<Integer, Integer>();

	public MelConvert(boolean doMapping) {
		if (doMapping) {
			// loadhScale();
			loadhScaleUmesh();
		}
	}

	public int calculateMelFromHz(int f) {
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

	public int convertHzToMel(int hz) {
		boolean doQuantize = false;
		if (!doQuantize) {
			return calculateMelFromHz(hz);
		}
		debug("convertHzToMel:" + hz + ":" + closestF);
		int minError = 150000000;
		for (int i = 0; i < hToMScale.size(); i++) {
			int t = Math.abs(hScale.get(i) - hz);
			if (t < minError) {
				minError = t;
				closestF = hScale.get(i);
			}
		}
		debug("convertHzToMel:closestF:" + closestF);
		int melF = hToMScale.get(closestF);
		return melF;
	}

	private void loadhScaleUmesh() {
		closestF = 15000;
		hScale.add(0);
		hScale.add(40);
		hScale.add(161);
		hScale.add(200);
		hScale.add(404);
		hScale.add(693);
		hScale.add(867);
		hScale.add(1000);
		hScale.add(2022);
		hScale.add(3000);
		hScale.add(3393);
		hScale.add(4000);
		hScale.add(4109);
		hScale.add(5526);
		hScale.add(6500);
		hScale.add(7743);
		hScale.add(12000);
		hToMScale.put(hScale.get(0), 0);
		hToMScale.put(hScale.get(1), 43);
		hToMScale.put(hScale.get(2), 257);
		hToMScale.put(hScale.get(3), 300);
		hToMScale.put(hScale.get(4), 514);
		hToMScale.put(hScale.get(5), 771);
		hToMScale.put(hScale.get(6), 928);
		hToMScale.put(hScale.get(7), 1000);
		hToMScale.put(hScale.get(8), 1542);
		hToMScale.put(hScale.get(9), 2000);
		hToMScale.put(hScale.get(10), 2142);
		hToMScale.put(hScale.get(11), 2287);
		if (Fs > 4000) {
			hToMScale.put(hScale.get(12), 2314);
			hToMScale.put(hScale.get(13), 2600);
			hToMScale.put(hScale.get(14), 2771);
			hToMScale.put(hScale.get(15), 2914);
			hToMScale.put(hScale.get(16), 3228);
		}
	}

	private void loadhScale() {
		closestF = 15000;
		hScale.add(0);
		hScale.add(40);
		hScale.add(100);
		hScale.add(161);
		hScale.add(200);
		hScale.add(300);
		hScale.add(404);
		hScale.add(500);
		hScale.add(600);
		hScale.add(693);
		hScale.add(800);
		hScale.add(867);
		hScale.add(1000);
		hScale.add(1500);
		hScale.add(2022);
		hScale.add(2500);
		hScale.add(3000);
		hScale.add(3393);
		hScale.add(3700);
		hScale.add(4000);
		hScale.add(4109);
		hScale.add(5526);
		hScale.add(6500);
		hScale.add(7743);
		hScale.add(12000);
		for (int i = 0; i < hScale.size(); i++) {
			int hF = hScale.get(i);
			int mF = calculateMelFromHz(hF);
			hToMScale.put(hF, mF);
		}
	}

	private void mapToPhone() {
		// Phoneme Example Translation
	}

	private void loadPhoneTable() {
		/*
		 * AA odd AA D AE at AE T AH hut HH AH T AO ought AO T AW cow K AW AY hide HH AY
		 * D B be B IY CH cheese CH IY Z D dee D IY DH thee DH IY EH Ed EH D ER hurt HH
		 * ER T EY ate EY T F fee F IY G green G R IY N HH he HH IY IH it IH T IY eat IY
		 * T JH gee JH IY K key K IY L lee L IY M me M IY N knee N IY NG ping P IH NG OW
		 * oat OW T OY toy T OY P pee P IY R read R IY D S sea S IY SH she SH IY T tea T
		 * IY TH theta TH EY T AH UH hood HH UH D UW two T UW V vee V IY W we W IY Y
		 * yield Y IY L D Z zee Z IY ZH seizure S IY ZH ER
		 */
	}

	private void debug(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) throws IOException {
		MelConvert f = new MelConvert(true);
		int hz = 866;
		int mel = f.convertHzToMel(hz);
		System.out.println("F-Mel Map:" + hz + ":" + mel);
	}
}
