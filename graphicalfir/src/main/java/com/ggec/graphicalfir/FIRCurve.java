package com.ggec.graphicalfir;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: ysy
 * Date: 2015/11/23
 * Time: 13:55
 */
public class FIRCurve extends View {

    private Paint mPaint;

    /**
     * 背景颜色
     */
    private int background;

    /**
     * 网格线之间的距离
     */
    private float x_interval;
    private float y_interval;

    /**
     * 横纵坐标网格数
     */
    private int xLine_count = 9;
    private int yLine_count = 6;

    /**
     * 坐标轴标签数组
     */
    private final float[] xIndex = new float[]
            {40f, 100f, 1000f, 10000f};
    private final float[] xindex_ratio = new float[]{0.0f, 0.132376f, 0.494197f,
            0.856017f};
    private final float[] yIndex = new float[]
            {-60f, -40f, -20f, 0f, 20f, 40f, 60f, 80f, 100f};

    /**
     * x坐标比例
     */
    private final float[] x_ratio = new float[]{0.132376f, 0.241295f, 0.305008f,
            0.350214f, 0.385278f, 0.413927f, 0.438150f, 0.459133f, 0.477641f, 0.494197f,
            0.603115f, 0.666829f, 0.712034f, 0.747098f, 0.775748f, 0.799970f, 0.820953f,
            0.839461f, 0.856017f, 0.964936f};

    /**
     * 坐标轴标签颜色
     */
    private int textColor;

    /**
     * 网格颜色
     */
    private int gridLineColor;
    /**
     * 原始曲线颜色
     */
    private int originCurveColor;

    /**
     * 调整曲线颜色
     */
    private int realCurveColor;

    /**
     * 是否绘画目标曲线
     */
    private boolean drawReal = true;

    /**
     * 标签字体大小
     */
    private int indexTextSize;

    /**
     * 存放两条线的数组
     */
    private double[] srcdB = new double[513];
    private double[] realdB = new double[513];

    public FIRCurve(Context context) {
        this(context, null);
    }

    public FIRCurve(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FIRCurve);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.FIRCurve_gridlinecolor) {
                gridLineColor = a.getColor(attr, Color.GRAY);

            } else if (attr == R.styleable.FIRCurve_textcolor) {
                textColor = a.getColor(attr, Color.BLACK);

            } else if (attr == R.styleable.FIRCurve_indexTextSize) {
                indexTextSize = a.getDimensionPixelSize(attr,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
                                getResources().getDisplayMetrics()));

            } else if (attr == R.styleable.FIRCurve_originCurveColor) {
                originCurveColor = a.getColor(attr, Color.BLUE);

            } else if (attr == R.styleable.FIRCurve_realCurveColor) {
                realCurveColor = a.getColor(attr, Color.RED);

            } else if (attr == R.styleable.FIRCurve_backgroundColor) {
                background = a.getColor(attr, Color.WHITE);

            }
        }


        a.recycle();
        //readSrcDB(context);
        // realdB = srcdB;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(getResources().getColor(R.color.blue));
    }

    private void readSrcDB(Context context) {

        srcdB = Read_Accesset(context, 513, "srcdB.txt");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);

        x_interval = width / (yLine_count + 1);
        y_interval = width / (xLine_count + 1);

        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(background);
        drawGridLine(canvas);
        drawText(canvas);
        drawCurve(canvas);

    }

    /*
   * 特性曲线
   * */
    private void drawCurve(Canvas canvas) {


        mPaint.setPathEffect(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(originCurveColor);
        double y = 0.0;
        double x = 0.0;
        double ratio = 0.0;
        double temp1 = Math.log(22050.0 / 512.0);

        /**
         * 原始曲线
         * */
        Path path = new Path();
        y = getHeight() - getHeight() / (xLine_count + 1)
                - ((srcdB[1] + 60.0) / 20.0) * (y_interval);
        ratio = ((Math.log10(22050.0 / 512.0)) - Math.log10(22050.0 / 512.0))
                / ((Math.log10(25000.0)) - Math.log10(22050.0 / 512.0));
        x = getWidth() / (yLine_count + 1) + (ratio) * (getWidth()
                - getWidth() * 2.0 / (yLine_count + 1));
        path.moveTo((float) x, (float) y);
        for (int i = 1, l = srcdB.length; i < l; i++) {
            y = getHeight() - getHeight() / (xLine_count + 1)
                    - ((srcdB[i] + 60.0) / 20.0) * (y_interval);
            ratio = ((Math.log10(i * 22050.0 / 512.0)) - Math.log10(22050.0 / 512.0))
                    / ((Math.log10(25000.0)) - Math.log10(22050.0 / 512.0));
            x = getWidth() / (yLine_count + 1) + (ratio) * (getWidth()
                    - getWidth() * 2.0 / (yLine_count + 1));
            path.lineTo((float) x, (float) y);
        }
        canvas.drawPath(path, mPaint);

        canvas.save();
        canvas.translate(x_interval, (float) (y_interval * (xLine_count - 1.5)));
        canvas.drawLine(0, 0, x_interval * 1.3f, 0, mPaint);
        canvas.restore();

        mPaint.setTextSize(indexTextSize * 2);
        canvas.save();
        canvas.translate(x_interval * 2.5f, (float) (y_interval * (xLine_count - 1.5)));
        canvas.drawText("原始曲线", 0, 0, mPaint);
        canvas.restore();

        /**
         * 目标曲线
         * */
        if (drawReal) {
            Path path1 = new Path();
            mPaint.setColor(realCurveColor);
            y = getHeight() - getHeight() / (xLine_count + 1)
                    - ((realdB[1] + 60.0) / 20.0) * (y_interval);
            ratio = ((Math.log10(22050.0 / 512.0)) - Math.log10(22050.0 / 512.0))
                    / ((Math.log10(25000.0)) - Math.log10(22050.0 / 512.0));
            x = getWidth() / (yLine_count + 1) + (ratio) * (getWidth()
                    - getWidth() * 2.0 / (yLine_count + 1));
            path1.moveTo((float) x, (float) y);
            for (int i = 1, l = realdB.length; i < l; i++) {
                y = getHeight() - getHeight() / (xLine_count + 1)
                        - ((realdB[i] + 60.0) / 20.0) * (y_interval);
                ratio = ((Math.log10(i * 22050.0 / 512.0)) - Math.log10(22050.0 / 512.0))
                        / ((Math.log10(25000.0)) - Math.log10(22050.0 / 512.0));
                x = getWidth() / (yLine_count + 1) + (ratio) * (getWidth()
                        - getWidth() * 2.0 / (yLine_count + 1));
                path1.lineTo((float) x, (float) y);
            }

            canvas.drawPath(path1, mPaint);

            canvas.save();
            canvas.translate(x_interval, (float) (y_interval * (xLine_count - 0.5)));
            canvas.drawLine(0, 0, x_interval * 1.3f, 0, mPaint);
            canvas.restore();

            mPaint.setTextSize(indexTextSize * 2);
            canvas.save();
            canvas.translate(x_interval * 2.5f, (float) (y_interval * (xLine_count - 0.5)));
            canvas.drawText("目标曲线", 0, 0, mPaint);
            canvas.restore();
        }
    }


    /*
   * 画网格线
   * */
    private void drawGridLine(Canvas canvas) {
        mPaint.setXfermode(null);
        PathEffect effect = new DashPathEffect(new float[]{1, 2, 4, 8}, 1);
        mPaint.setPathEffect(effect);
        float startX = 0;
        float startY = 0;
        float stopX = 0;
        float stopY = 0;
        mPaint.setColor(gridLineColor);

        startX = (getWidth() / (yLine_count + 1));
        startY = (getHeight() / (xLine_count + 1));
        stopX = (getWidth() / (yLine_count + 1));
        stopY = (getHeight() / (xLine_count + 1)) * (xLine_count);
        canvas.drawLine(startX, startY, stopX, stopY, mPaint);

        for (int i = 0; i < x_ratio.length; i++) {
            startX = (getWidth() / (yLine_count + 1)) +
                    (getWidth() / (yLine_count + 1)) * (yLine_count - 1) * x_ratio[i];
            startY = (getHeight() / (xLine_count + 1));
            stopX = (getWidth() / (yLine_count + 1)) +
                    (getWidth() / (yLine_count + 1)) * (yLine_count - 1) * x_ratio[i];
            stopY = (getHeight() / (xLine_count + 1)) * (xLine_count);
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }

        startX = (getWidth() / (yLine_count + 1)) * (yLine_count);
        startY = (getHeight() / (xLine_count + 1));
        stopX = (getWidth() / (yLine_count + 1)) * (yLine_count);
        stopY = (getHeight() / (xLine_count + 1)) * (xLine_count);
        canvas.drawLine(startX, startY, stopX, stopY, mPaint);

        for (int i = 0; i < xLine_count; i++) {
            startX = (getWidth() / (yLine_count + 1));
            startY = (getHeight() / (xLine_count + 1)) * (i + 1);
            stopX = (getWidth() / (yLine_count + 1)) * (yLine_count);
            stopY = (getHeight() / (xLine_count + 1)) * (i + 1);
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }

    }

    /*
    * 画坐标标签
    * */
    private void drawText(Canvas canvas) {
        mPaint.setPathEffect(null);
        mPaint.setColor(textColor);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(indexTextSize);
        mPaint.setTextSize(indexTextSize);
        canvas.save();
        canvas.translate(0, getHeight() - getHeight() / (xLine_count + 1) + y_interval/2.0f);

        for (int i = 0; i < xIndex.length; i++) {
            canvas.save();
            canvas.translate(xindex_ratio[i] * (getWidth() - (getWidth() * 2 / (yLine_count + 1))) + x_interval, 0);
            canvas.drawText("" + xIndex[i], 0, -20, mPaint);
            canvas.restore();
        }
        canvas.restore();

        canvas.save();
        canvas.translate(getWidth() / (xLine_count + 1) - indexTextSize * 2, getHeight());
        for (int i = 0; i < (xLine_count); i++) {
            canvas.translate(0, -(getHeight() / (xLine_count + 1)));
            canvas.drawText("" + yIndex[i], 20, 0, mPaint);
        }
        canvas.restore();
    }

    /**
     * the array length must be 513
     */
    public void setRealdB(double[] realdB) {
        this.realdB = realdB;
        invalidate();
    }

    /**
     * the array length must be 513
     */
    public void setSrcdB(double[] srcdB) {
        this.srcdB = srcdB;
        invalidate();
    }

    public void setDrawReal(boolean drawReal) {
        this.drawReal = drawReal;
    }

    public boolean isDrawReal() {
        return drawReal;
    }

    public double[] Read_Accesset(Context context, int length, String file_name) {
        byte[] rbyte_fir = new byte[length * 8];
        double[] rdouble_fir = new double[length];
        InputStream fis = null;
        try {
            fis = context.getAssets().open(file_name);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fis != null) {
            try {
                fis.read(rbyte_fir, 0, length * 8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fis != null) {

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] temp = new byte[8];
        for (int i = 0; i < length; i++) {

            for (int j = 0; j < 8; j++) {
                temp[j] = rbyte_fir[i * 8 + j];
            }

            rdouble_fir[i] = bytesToDouble(temp);

        }

        double[] fir_result = new double[length];
        System.arraycopy(rdouble_fir, 0, fir_result, 0, length);
        return fir_result;
    }

    //字节到浮点转换
    public static double bytesToDouble(byte[] readBuffer) {
        return Double.longBitsToDouble((((long) readBuffer[7] << 56) +
                        ((long) (readBuffer[6] & 255) << 48) +
                        ((long) (readBuffer[5] & 255) << 40) +
                        ((long) (readBuffer[4] & 255) << 32) +
                        ((long) (readBuffer[3] & 255) << 24) +
                        ((readBuffer[2] & 255) << 16) +
                        ((readBuffer[1] & 255) << 8) +
                        ((readBuffer[0] & 255) << 0))
        );
    }


}
