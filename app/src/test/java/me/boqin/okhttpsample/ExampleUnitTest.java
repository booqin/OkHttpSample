package me.boqin.okhttpsample;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void hashMap(){
        Map<String, String> map = new LinkedHashMap<>(5, 0.75f, true);
        map.put("aa", "aa");
        map.put("bb", "bb");
        map.put("cc", "cc");
        System.out.print(map.toString());
        map.get("bb");
        System.out.print(map.toString());
    }
}