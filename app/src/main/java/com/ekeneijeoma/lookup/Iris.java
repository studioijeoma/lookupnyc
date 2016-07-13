package com.ekeneijeoma.lookup;

import com.ijeoma.Parallel;
import com.ijeoma.Tween;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import processing.core.PApplet;
import processing.data.FloatList;

class Iris {
    PApplet p;

    float x = 0;
    float y = 0;

    float d1 = 0;
    float d2 = 0;

    float r1 = 0;
    float r2 = 0;

    float lineW = 0;
    float lineR1 = 0;
    float lineR2 = 0;

    int c1 = 0;
    int c2 = 0;

    float a = 0;

    boolean loading = false;

    ArrayList<FloatList> lengths = new ArrayList<FloatList>();
    ConcurrentLinkedQueue ringLines = new ConcurrentLinkedQueue();

    Parallel radiateRingLines = new Parallel();
    Parallel unradiateRingLines = new Parallel();

    Iris(PApplet p, float x, float y, float d1, float d2, int c1, int c2,
         int lineW) {
        this.p = p;
        this.x = x;
        this.y = y;

        this.d1 = d1;
        this.d2 = d2;

        this.r1 = this.lineR1 = d1 * .5f;
        this.r2 = this.lineR2 = d2 * .5f;

        this.c1 = c1;
        this.c2 = c2;

        this.lineW = lineW;
    }

    void draw() {
        p.pushMatrix();
        p.translate(x, y);

        p.noStroke();
        RadialGradientEllipse.draw(p, 0, 0, c1, 0, 0, c2, r2, r2, .15f);

        p.rotate(PApplet.TWO_PI - a);
        drawRings();

        p.popMatrix();
    }

    void c(int c1, int c2) {
        this.c1 = c1;
        this.c2 = c2;

        Iterator<RadialLine> it = ringLines.iterator();

        while (it.hasNext()) {
            RadialLine il = it.next();
            il.c1 = c2;
            il.c2 = c1;
        }
    }

    void rotate(float a) {
        this.a = a;
    }

    void radiate() {
        radiateRingLines.play();
    }

    void unradiate() {
        unradiateRingLines.play();
    }

    void setupRingLines() {
        boolean loading = true;

        Iterator<RadialLine> it = ringLines.iterator();

        while (it.hasNext())
            it.next().remove = true;

        radiateRingLines.removeAll();
        unradiateRingLines.removeAll();

        float size = (lengths.size() == 1) ? (lineR2 - lineR1)
                : (lineR2 - lineR1) / lengths.size();

        for (int i = 0; i < lengths.size(); i++) {
            FloatList ringData = lengths.get(i);
            float angleStep = PApplet.TWO_PI / ringData.size();
            float angleOffest = (float) (PApplet.PI * Math.random());

            float duration = 15;
            float delay1 = duration / 2;
            float delay2 = (lengths.size() == 1) ? duration : i
                    * (duration / lengths.size());

            for (int j = 0; j < ringData.size(); j++) {
                // float l = 1;// ringData.get(j);
                float l = ringData.get(j);
                float a = j * (angleStep) + angleOffest;
                float lr1 = lineR1 + i * size;
                float lr2 = (l * size);

                RadialLine il = new RadialLine(p, 0, lr1, a, lr2, lineW, c1, c2);
                ringLines.add(il);

                radiateRingLines.add(new Tween(il)
                        .fromTo("l", 0, il.l, duration).delay(delay1 + delay2)
                        .easing("bounceIn"));

                unradiateRingLines.add(new Tween(il)
                        .fromTo("l", il.l, 0, duration).delay(0)
                        .easing("bounceIn"));

                il.l = 0;
            }
        }

        loading = false;
    }

    void randomRingLines(int rings) {
        lengths.clear();

        int max = 40;
        int step = (int) ((rings == 1) ? max : (float) max / rings);

        for (int i = 1; i <= rings; i++) {
            int count = i * step;

            FloatList d = new FloatList();

            p.randomSeed(p.millis());

            for (int j = 0; j < count; j++)
                d.append(p.random(0.1f, 1f));

            lengths.add(d);
        }

        setupRingLines();
    }

    void drawRings() {
        if (!loading) {
            Iterator<RadialLine> it = ringLines.iterator();

            while (it.hasNext()) {
                RadialLine il = it.next();
                il.draw();

                if (il.remove) {
                    it.remove();
                }
            }
        }
    }

    class RadialLine {
        PApplet p;

        float x = 0;
        float y = 0;

        float angle = 0;

        float l = 0;
        float w = 5;

        int c1 = 0;
        int c2 = 0;

        public boolean remove = false;

        RadialLine(PApplet p, float x, float y, float angle, float l, float w,
                   int c1, int c2) {
            this.p = p;

            this.x = x;
            this.y = y;

            this.angle = angle;
            this.l = l;
            this.w = w;

            this.c1 = c1;
            this.c2 = c2;
        }

        void draw() {
            p.pushMatrix();
            p.rotate(angle);
            p.translate(x, y);
            p.beginShape();
            p.fill(c1);
            p.vertex(0, 0);
            p.vertex(w, 0);
            p.fill(c2);
            p.vertex(w, l);
            p.vertex(0, l);
            p.endShape();
            p.popMatrix();
        }
    }
}