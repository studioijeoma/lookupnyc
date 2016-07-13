package com.ekeneijeoma.lookup;

import com.ijeoma.Easing;
import com.ijeoma.Ijeoma;
import com.ijeoma.Parallel;
import com.ijeoma.Tween;

import processing.core.PApplet;
import processing.core.PConstants;

class Eye {
	public float eyelidH = 0;
	PApplet p;

	float x = 0;
	float y = 0;

	float d = 100;

	int c1 = 0;
	int c2 = 0;

	float smin = .5f;
	float smax = 1f;

	Iris iris;

	float pupilF = 0;
	float pupilD = d * .6f;
	int pupilC = 0;

	float lookX = 0;
	float lookY = 0;

	Tween lookXTween, lookYTween;

	Parallel open, close;

	float durationStep = 15;

	Eye(PApplet p, float x, float y, float r, int c1, int c2, int irisC1,
		int irisC2, int irisLineW, int pupilC) {
		this.p = p;
		this.x = x;
		this.y = y;
		this.d = 0;
		this.c1 = c1;
		this.c2 = c2;

		this.pupilF = .10f;
		this.pupilD = r * pupilF;
		this.pupilC = pupilC;

		float irisD1 = r * pupilF;
		float irisD2 = r * .65f;

		this.iris = new Iris(p, 0, 0, irisD1, irisD2, irisC1, irisC2, irisLineW);
		this.iris.randomRingLines(1);

		this.eyelidH = 0;

		open = Ijeoma.startParallel();
		Ijeoma.fromTo(this, "d", 0, r, durationStep).delay(0).easing("backOut"); // eyeOpen
		Ijeoma.fromTo(this, "pupilD", 0, pupilD, durationStep).delay(0)
				.easing("circOut"); // pupilOpen
		Ijeoma.fromTo(iris, "r2", 0, iris.r2, durationStep)
				.delay(durationStep / 2).easing("backOut"); // irisOpen
//		Ijeoma.add(iris.radiateRingLines);
		Ijeoma.endParallel();

		close = Ijeoma.startParallel();
		Ijeoma.fromTo(iris, "r2", iris.r2, 0, durationStep)
				.delay(durationStep / 2).easing("backIn"); // irisClose
		Ijeoma.fromTo(this, "d", r, 0, durationStep).delay(0).easing("backIn"); // eyeClose
		Ijeoma.fromTo(this, "pupilD", pupilD, 0, durationStep).delay(0)
				.easing("circIn"); // pupilClose
//		Ijeoma.add(iris.unradiateRingLines);
		Ijeoma.endParallel();

		lookXTween = new Tween(this);
		lookYTween = new Tween(this);
	}

	void draw() {
		smin = .5f;
		smax = 1f;

		float sd = PApplet.constrain(PApplet.dist(lookX, lookY, 0, 0), 0, 1);

		float sl = (d - iris.d2) * sd;
		float offset = PApplet.map(PApplet.abs(sd), 0, 1, 0, sl);

		float a = PApplet.atan2(lookY, lookX);
		float s = PApplet.map(Easing.Quart.Out(1 - sd), 0, 1, smin, smax);

		float cstop = .5f;

		p.pushMatrix();
		p.translate(x, y);
		p.rotate(a);

		p.pushStyle();
		p.colorMode(PConstants.HSB, 360, 100, 100);
		p.noStroke();
		RadialGradientEllipse.draw(p, 0, 0, c1, offset, 0, c2, d / 2, d / 2,
				cstop);
		p.popStyle();

		p.pushMatrix();
		p.translate(offset, 0);
		p.scale(s, 1);

		iris.rotate(a);
		iris.draw();

		p.noStroke();
		p.fill(pupilC);
		p.ellipse(0, 0, pupilD, pupilD);
		p.popMatrix();

		p.popMatrix();
	}

	void open() {
		open.play();
		 iris.radiate();
	}

	void close() {
		 iris.unradiate();
		close.play();
	}

	Tween look(float[] d, float duration, float delay) {
		lookXTween.to("lookX", d[0], duration).delay(delay).play()
				.easing("circOut");
		lookYTween.to("lookY", d[1], duration).delay(delay).play()
				.easing("circOut");

		return lookYTween;
	}
}