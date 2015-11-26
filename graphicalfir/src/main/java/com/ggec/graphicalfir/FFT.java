package com.ggec.graphicalfir;

/**
 * User: ysy
 * Date: 2015/9/17
 * Time: 14:07
 */
public class FFT {

    public static void rfft(double x[], int n) {
        int i, j, k, m, n1, n2, n4, i1, i2, i3, i4;
        double a, e, ca, sa, t, tr1, ti1, tr2, ti2;

        m = 1;
        for (i = 1, j = 1; i < 32; i++) {
            m = i;
            j = 2 * j;
            if (j == n) break;
        }

        n1 = n - 1;
        for (i = 0, j = 0; i < n1; i++) {
            if (i < j) {
                t = x[i];
                x[i] = x[j];
                x[j] = t;
            }
            k = n / 2;
            while (j >= k) {
                j = j - k;
                k = k / 2;
            }
            j = j + k;
        }

        for (i = 0; i < n; i += 2) {
            t = x[i];
            x[i] = x[i] + x[i + 1];
            x[i + 1] = t - x[i + 1];
        }
        n2 = 1;
        for (k = 2; k <= m; k++) {
            n4 = n2;
            n2 = n4 * 2;
            n1 = n2 * 2;
            e = 6.283185307179586 / n1;
            for (i = 0; i < n; i += n1) {
                t = x[i];
                x[i] = x[i] + x[i + n2];
                x[i + n2] = t - x[i + n2];
                x[i + n2 + n4] = -x[i + n2 + n4];
                a = e;
                for (j = 1; j < n4; j++) {
                    i1 = i + j;
                    i2 = i + n2 - j;
                    i3 = i + j + n2;
                    i4 = i + n1 - j;
                    ca = Math.cos(a);
                    sa = -Math.sin(a);
                    a += e;
                    tr1 = x[i3] * ca - x[i4] * sa;
                    ti1 = x[i3] * sa + x[i4] * ca;
                    tr2 = -x[i3] * ca + x[i4] * sa;
                    ti2 = x[i3] * sa + x[i4] * ca;
                    x[i4] = x[i2] + ti1;
                    x[i3] = -x[i2] + ti2;
                    x[i2] = x[i1] + tr2;
                    x[i1] = x[i1] + tr1;
                }
            }
        }
    }


    public static void irfft(double x[], int n) {
        int i, j, k, m, n2, n4, n8, i1, i2, i3, i4, i5, i6, i7, i8, is, id;
        double e, a, cc1, ss1, cc2, ss2, t, t1, t2, t3, t4;
        m = 1;
        for (i = 1, j = 1; i < 32; i++) {
            m = i;
            j = j * 2;
            if (j == n) break;
        }

        n2 = n * 2;
        for (k = 1; k < m; k++) {
            is = 0;
            id = n2;
            n2 = n2 / 2;
            n4 = n2 / 4;
            n8 = n4 / 2;
            e = 6.283185307179586 / n2;
            do {
                for (i = is; i < n; i += id) {
                    i1 = i;
                    i2 = i1 + n4;
                    i3 = i2 + n4;
                    i4 = i3 + n4;
                    t = x[i1] - x[i3];
                    x[i1] = x[i1] + x[i3];
                    x[i2] = 2 * x[i2];
                    x[i3] = t - 2 * x[i4];
                    x[i4] = t + 2 * x[i4];
                    if (n4 == 1) continue;
                    i1 += n8;
                    i2 += n8;
                    i3 += n8;
                    i4 += n8;
                    t1 = (x[i1] - x[i2]) / Math.sqrt(2.0);
                    t2 = (x[i3] + x[i4]) / Math.sqrt(2.0);
                    x[i1] = x[i1] + x[i2];
                    x[i2] = x[i4] - x[i3];
                    x[i3] = 2 * (t1 - t2);
                    x[i4] = -2 * (t1 + t2);
                }
                is = 2 * id - n2;
                id = 4 * id;
            } while (is < n - 1);//while(is<n);

            a = e;
            for (j = 1; j < n8; j++) {
                cc1 = Math.cos(a);
                ss1 = Math.sin(a);
                cc2 = Math.cos(3 * a);
                ss2 = Math.sin(3 * a);
                a = (j + 1) * e;
                is = 0;
                id = 2 * n2;
                do {
                    for (i = is; i < n; i += id) {
                        i1 = i + j;
                        i2 = i1 + n4;
                        i3 = i2 + n4;
                        i4 = i3 + n4;
                        i5 = i + n4 - j;
                        i6 = i5 + n4;
                        i7 = i6 + n4;
                        i8 = i7 + n4;
                        t1 = x[i1] - x[i6];
                        x[i1] = x[i1] + x[i6];
                        t2 = x[i2] - x[i5];
                        x[i5] = x[i2] + x[i5];
                        t3 = x[i3] + x[i8];
                        x[i6] = x[i8] - x[i3];
                        t4 = x[i4] + x[i7];
                        x[i2] = x[i4] - x[i7];
                        x[i3] = (t1 - t4) * cc1 - (t2 + t3) * ss1;
                        x[i4] = (t1 + t4) * cc2 - (t3 - t2) * ss2;
                        x[i7] = (t1 - t4) * ss1 + (t2 + t3) * cc1;
                        x[i8] = (t1 + t4) * ss2 + (t3 - t2) * cc2;
                    }
                    is = 2 * id - n2;
                    id = 4 * id;
                } while (is < n - 1);//while (is<n);
            }
        }

        is = 0;
        id = 4;
        do {
            for (i = is; i < n; i += id) {
                t = x[i];
                x[i] = t + x[i + 1];
                x[i + 1] = t - x[i + 1];
            }
            is = 2 * id - 2;
            id = 4 * id;
        } while (is < n - 1);//while (is<n);

        for (i = 0, j = 0; i < n - 1; i++) {
            if (i < j) {
                t = x[i];
                x[i] = x[j];
                x[j] = t;
            }
            k = n / 2;
            while (j >= k) {
                j -= k;
                k /= 2;
            }
            j += k;
        }

        for (i = 0; i < n; i++)
            x[i] /= n;
    }
}
