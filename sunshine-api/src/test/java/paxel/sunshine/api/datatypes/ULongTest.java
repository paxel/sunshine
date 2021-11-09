package paxel.sunshine.api.datatypes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ULongTest {

    @Test
    public void fromUnsignedString() {
        ULong uLong = ULong.fromUnsignedString("17777777777777788899");
        assertThat(uLong.toString(), is("17777777777777788899"));
    }

    @Test
    public void getSignedValue() {
        ULong uLong = ULong.fromUnsignedString("17777777777777788899");
        assertThat(uLong.getSignedValue(), is(-668966295931762717L));
    }

    @Test
    public void compareTo() {
        ULong uLong = ULong.fromUnsignedString("17777777777777788899");

        List<ULong> list = new ArrayList<>();
        list.add(uLong);
        list.add(new ULong(0));
        list.add(new ULong(100));
        Collections.shuffle(list);
        Collections.sort(list);

        assertThat(list.get(0).toString(), is("0"));
        assertThat(list.get(1).toString(), is("100"));
        assertThat(list.get(2).toString(), is("17777777777777788899"));
    }


    @Test
    public void isSignedValueLessThanZero() {

        ULong uLong = ULong.fromUnsignedString("17777777777777788899");
        assertThat(uLong.isSignedValueLessThanZero(), is(true));
        assertThat(new ULong(0).isSignedValueLessThanZero(), is(false));
    }
}