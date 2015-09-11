package com.ysy.sweepmeasure.sweep;

/**
 * User: ysy
 * Date: 2015/9/6
 */
public class Convs {

    private SaveDataListener dataListener;

    public interface SaveDataListener {
        void savedata(double data);

    }

    public void setDataListener(SaveDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void convols(double[] BufA, double[] BufB, double[] BufC, int LA, int LB) {
        int LC = LA + LB - 1;
        int i, j, Ll, Lh;
        for (i = 0; i < LC; i++) {
            BufC[i] = 0;
            Ll = (i - LB + 1) < 0 ? 0 : i - LB + 1;
            Lh = i < (LA - 1) ? i : LA - 1;
            for (j = Ll; j <= Lh; j++) {
                BufC[i] += BufA[j] * BufB[i - j];
            }
            dataListener.savedata(BufC[i]);
        }
    }
}
