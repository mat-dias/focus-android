package com.example.focus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    public static class Fatia {
        public float valor;
        public int cor;
        public String label;

        public Fatia(float valor, int cor, String label) {
            this.valor = valor;
            this.cor   = cor;
            this.label = label;
        }
    }

    private final List<Fatia> fatias = new ArrayList<>();
    private final Paint paint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centro = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval   = new RectF();

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Círculo interno (furo do donut)
        centro.setColor(0xFF1A1A1A);
        centro.setStyle(Paint.Style.FILL);
    }

    public void setFatias(List<Fatia> lista) {
        fatias.clear();
        fatias.addAll(lista);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (fatias.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float raio = Math.min(cx, cy) - 4;

        oval.set(cx - raio, cy - raio, cx + raio, cy + raio);

        // Soma total
        float total = 0;
        for (Fatia f : fatias) total += f.valor;
        if (total == 0) return;

        // Desenha fatias
        float anguloAtual = -90f; // começa do topo
        for (Fatia f : fatias) {
            float sweep = (f.valor / total) * 360f;
            paint.setColor(f.cor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(oval, anguloAtual, sweep, true, paint);
            anguloAtual += sweep;
        }

        // Furo central (donut)
        float raioInterno = raio * 0.55f;
        canvas.drawCircle(cx, cy, raioInterno, centro);

        // Percentual da maior fatia no centro
        Fatia maior = fatias.get(0);
        for (Fatia f : fatias) if (f.valor > maior.valor) maior = f;

        Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(0xFFFFFFFF);
        txtPaint.setTextSize(raioInterno * 0.45f);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setFakeBoldText(true);

        int pct = Math.round((maior.valor / total) * 100);
        canvas.drawText(pct + "%", cx, cy + txtPaint.getTextSize() / 3, txtPaint);

        // Label menor abaixo do %
        Paint lblPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lblPaint.setColor(0xFF888888);
        lblPaint.setTextSize(raioInterno * 0.22f);
        lblPaint.setTextAlign(Paint.Align.CENTER);
        String label = maior.label.length() > 8
                ? "#" + maior.label.substring(0, 7) + "…"
                : "#" + maior.label;
        canvas.drawText(label, cx, cy + txtPaint.getTextSize() * 0.95f, lblPaint);
    }
}
