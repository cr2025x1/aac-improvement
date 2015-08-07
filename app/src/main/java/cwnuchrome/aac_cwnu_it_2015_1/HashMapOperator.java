package cwnuchrome.aac_cwnu_it_2015_1;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chrome on 8/7/15.
 */
public class HashMapOperator {

    // A - B
    // 키 값이 같은 매핑이 있으면 두 키의 대응값의 차를 그 키에 대응시킨다. 만일 그 차가 음수일 경우엔 키매핑 자체를 지운다.
    @NonNull
    public static <K> HashMap<K, Long> complement(@NonNull HashMap<K, Long> lhs, @NonNull HashMap<K, Long> rhs) {
        HashMap<K, Long> rsm = new HashMap<>(lhs);

        for (Map.Entry<K, Long> e : rhs.entrySet()) {
            K key = e.getKey();
            Long value = e.getValue();
            Long rsm_value;
            if ((rsm_value = rsm.get(key)) != null) {
                if (rsm_value > value) {
                    rsm.put(key, rsm_value - value);
                } else {
                    rsm.remove(key);
                }
            }
        }

        return rsm;
    }

    // A + B
    // 키 값이 같은 매핑이 있으면 두 키의 값을 합친 값을 다시 그 키에 대응시킨다.
    @NonNull
    public static <K> HashMap<K, Long> combine(@NonNull HashMap<K, Long> lhs, @NonNull HashMap<K, Long> rhs) {
        HashMap<K, Long> rsm = new HashMap<>(lhs);

        for (Map.Entry<K, Long> e : rhs.entrySet()) {
            K key = e.getKey();
            Long value = e.getValue();
            Long rsm_value;
            if ((rsm_value = rsm.get(key)) != null) {
                rsm.put(key, rsm_value + value);
            } else {
                rsm.put(key, value);
            }

        }

        return rsm;
    }
}
