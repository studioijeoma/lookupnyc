package com.ekeneijeoma.lookup;

import processing.core.PApplet;

public class RadialGradientEllipse {
    static void draw(PApplet p, float x1, float y1, int c1, float x2,
                                          float y2, int c2, float w, float h, float cstop) {
        float detail = 50;

        float dx = PApplet.abs(w - (w * cstop));
        float dy = PApplet.abs(h - (h * cstop));

        x2 = PApplet.constrain(x2, x1 - dx, x1 + dx);
        y2 = PApplet.constrain(y2, y1 - dy, y1 + dy);

        int count = (cstop < 1) ? 2 : 1;

        p.beginShape(PApplet.QUADS);
        for (int i = 0; i < count; i++) {
            float qt1 = cstop * i;
            float qt2 = (i == 1) ? 1 : cstop * (i + 1);

            int qc1 = (i == 1) ? c1 : c1;
            int qc2 = (i == 1) ? c2 : c1;

            for (int j = 0; j < detail; j++) {
                float x3 = (i == 0 && count == 2) ? x2 : x1;
                float y3 = (i == 0 && count == 2) ? y2 : y1;

                float qa1 = (j / detail) * PApplet.TWO_PI;
                float ta2 = ((j + 1) / detail) * PApplet.TWO_PI;

                float qxa1 = PApplet.cos(qa1);
                float qya1 = PApplet.sin(qa1);

                float qxa2 = PApplet.cos(ta2);
                float qya2 = PApplet.sin(ta2);

                float qx1 = x2 + qt1 * w * qxa1;
                float qy1 = y2 + qt1 * h * qya1;

                float qx2 = x3 + qt2 * w * qxa1;
                float qy2 = y3 + qt2 * h * qya1;

                float qx3 = x3 + qt2 * w * qxa2;
                float qy3 = y3 + qt2 * h * qya2;

                float qx4 = x2 + qt1 * w * qxa2;
                float qy4 = y2 + qt1 * h * qya2;

                p.fill(qc1);
                p.vertex(qx1, qy1);
                p.fill(qc2);
                p.vertex(qx2, qy2);
                p.fill(qc2);
                p.vertex(qx3, qy3);
                p.fill(qc1);
                p.vertex(qx4, qy4);
            }
        }
        p.endShape();
    }
}
