package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;

public class Triple<A, B, C> implements Serializable {
    private static final long serialVersionUID = -2977745838583844336L;

    private A first;

    private B second;

    private C third;

    public Triple(A first, B second, C third) {
        super();
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    public C getThird() {
        return third;
    }

    public void setThird(C third) {
        this.third = third;
    }
}
