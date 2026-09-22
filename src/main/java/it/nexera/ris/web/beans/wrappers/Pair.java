package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
    private static final long serialVersionUID = 2257927537118386834L;

    private A first;

    private B second;

    public Pair(A first, B second) {
        super();
        this.first = first;
        this.second = second;
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

}
