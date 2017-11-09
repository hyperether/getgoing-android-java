package com.hyperether.getgoing.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nikola on 20.10.17..
 */
public class ConversionTest {
    @Test
    public void getDurationString() throws Exception {
        long input = 3600;
        String output;
        String expected = "01 : 00 : 00";
        output = Conversion.getDurationString(input);
        assertEquals(expected,output);
    }

    @Test
    public void twoDigitString() throws Exception {
        long input = 0;
        long input1 = 10;
        String output;
        String output2;
        String expected = "00";
        output = Conversion.twoDigitString(input);
        assertEquals(expected,output);
        for(int i = 0;i<input1;i++){
            output2 = Conversion.twoDigitString(i);
            assertEquals("0"+i,output2);
        }
    }

}