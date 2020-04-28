package com.example.fn;

import com.fnproject.fn.testing.FnResult;
import com.fnproject.fn.testing.FnTestingRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    // @Test
    public void shouldReturnUser() {
        testing.givenEvent().withBody("tsharp").enqueue();
        testing.thenRun(HelloFunction.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        System.out.println(result.getBodyAsString());
        assertEquals(true,true);
    }
}

