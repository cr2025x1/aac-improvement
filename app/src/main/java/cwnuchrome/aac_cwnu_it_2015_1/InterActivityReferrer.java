package cwnuchrome.aac_cwnu_it_2015_1;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Chrome on 7/14/15.
 *
 * 액티비티간 객체 교환을 목적으로 만들어진 클래스.
 */

@SuppressWarnings("unused")
public class InterActivityReferrer <T> {
    HashMap<Integer, T> map;
    ArrayList<Integer> reserve;

    public InterActivityReferrer() {
        map = new HashMap<>();
        reserve = new ArrayList<>();
    }

    public synchronized int attach(@NonNull T object) {
        int id;
        if (reserve.isEmpty()) {
            id = reserve.size();
        }
        else {
            id = reserve.remove(reserve.size() - 1);
        }

        map.put(id, object);
        return id;
    }

    public synchronized T get(int id) {
        return map.get(id);
    }

    public synchronized T detach(int id) {
        T object = map.remove(id);
        if (object != null) reserve.add(id);
        return object;
    }
}
