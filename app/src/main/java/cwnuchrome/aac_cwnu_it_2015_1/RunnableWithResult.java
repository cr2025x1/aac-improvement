package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 8/4/15.
 *
 * 기존의 Runnable 클래스는 반환값을 전달할 수 없었기 때문에 이 클래스는 반환값을 전달할 수 있게 Runnable을 확장한 서브 클래스이다.
 */
abstract class RunnableWithResult<T> implements Runnable {
    T result = null;
    T param = null;

    // 파라미터를 설정한다.
    public void setParam(T param) {
        this.param = param;
    }

    // 파라미터를 읽는다.
    public T getParam() {
        return param;
    }

    // 결과를 읽는다.
    public T getResult() {
        return result;
    }

    // 결과를 설정한다.
    public void setResult(T result) {
        this.result = result;
    }
}
