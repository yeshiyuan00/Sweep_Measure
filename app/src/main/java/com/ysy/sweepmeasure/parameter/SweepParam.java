package com.ysy.sweepmeasure.parameter;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: ysy
 * Date: 2015/8/19
 */
public class SweepParam implements Parcelable {
    private double fs, f1, f2, T, A;

    public void setFs(double fs) {
        this.fs = fs;
    }

    public double getFs() {
        return this.fs;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF1() {
        return this.f1;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF2() {
        return this.f2;
    }

    public void setT(double T) {
        this.T = T;
    }

    public double getT() {
        return this.T;
    }

    public void setA(double A) {
        this.T = A;
    }

    public double getA() {
        return this.A;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(fs);
        dest.writeDouble(f1);
        dest.writeDouble(f2);
        dest.writeDouble(T);
        dest.writeDouble(A);
    }

    public static final Parcelable.Creator<SweepParam> CREATOR = new Creator<SweepParam>() {
        @Override
        public SweepParam createFromParcel(Parcel in) {
            SweepParam sweepParam = new SweepParam();
            sweepParam.fs = in.readDouble();
            sweepParam.f1 = in.readDouble();
            sweepParam.f2 = in.readDouble();
            sweepParam.T = in.readDouble();
            sweepParam.A = in.readDouble();
            return sweepParam;
        }

        @Override
        public SweepParam[] newArray(int size) {
            return new SweepParam[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        return o instanceof SweepParam;
    }
}
