package com.ekeneijeoma.lookup;

import processing.core.PApplet;
import processing.core.PConstants;

class Face {
	PApplet p;

	float y = 0;
	float w = 0;

	Eye eye1, eye2;

	float eyeS;
	float eyeGap;
	int eyeC1, eyeC2;

	boolean visible = false;

 	String randomHues = "";

	Face(PApplet p, float y, float eyeS, int eyeC1, int eyeC2, int irisC1,
		 int irisC2, int irisLineW, int pupilC) {
		this.p = p;

		this.y = y;

		this.eyeS = eyeS;

		this.eyeC1 = eyeC1;
		this.eyeC2 = eyeC2;

		this.w = eyeGap * 2 + eyeS;

		eye1 = new Eye(p, 0, y, eyeS, eyeC1, eyeC2, irisC1, irisC2, irisLineW,
				pupilC);
		eye2 = new Eye(p, p.width, y, eyeS, eyeC1, eyeC2, irisC1, irisC2,
				irisLineW, pupilC);
		eye2.close.onStop(this, "hide");
	}

	void draw() {
		if (visible) {
			eye1.draw();
			eye2.draw();
		}
	}

	void open() {
		show();

		randomLook(15, 0);

		eye1.open();
		eye2.open();
	}

	void close() {
		look(new float[] { 0, 0 }, 15, 0);

		eye1.close();
		eye2.close();
	}

	void show() {
		visible = true;
	}

	void hide() {
		visible = false;
	}

	void look(float[] d, float duration, float delay) {
		eye1.look(d, duration, delay);
		eye2.look(d, duration, delay);
	}

	void randomLook(float duration, float delay) {
		eye1.look(new float[] { p.random(0, 1), p.random(-1, 1) }, duration,
				delay);
		eye2.look(new float[] { p.random(-1, 0), p.random(-1, 1) }, duration,
				delay);
	}

	void c(int c1, int c2) {
		eyeC1 = eye1.c1 = eye2.c1 = c1;
		eyeC2 = eye1.c2 = eye2.c2 = c2;
	}

	void randomC() {
		p.pushStyle();
		p.colorMode(PConstants.HSB, 360, 100, 100);

		int c1 = randomHSB();
		int h1 = (int) p.hue(c1);

		int c2 = randomHSB();
		int h2 = (int) p.hue(c2);
		float d = 0;

		int i = 0;
		int[] hs = p.parseInt(p.split(randomHues, ","));

		i = 0;

		do {
			c2 = randomHSB();
			h2 = (int) p.hue(c2);

			d = PApplet.abs(h1 - h2);

			i++;
		} while (d < 45);

		c(c1, c2);

		p.popStyle();

	}

	void irisScore(int score) {
		eye1.iris.randomRingLines(score);
		eye2.iris.randomRingLines(score);
	}

	int randomHSB() {
		return p.color(p.random(20, 360), 60, 80);
	}
}