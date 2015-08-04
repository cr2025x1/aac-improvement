package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 8/4/15.
 */
abstract class RunnableWithResult<T> implements Runnable {
    T result = null;
    T param = null;

    public void setParam(T param) {
        this.param = param;
    }

    public T getParam() {
        return param;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
