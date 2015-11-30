package com.jintin.DroidLane.test

import com.jintin.droidlane.utils.AESUtils
import junit.framework.TestCase

class AESUtilsTest extends TestCase {


    void testPrintMessage() {

        def text = "testabc"
        def key = "12345678"

        def result = AESUtils.encrypt(text, key)
        assertEquals(result, "e93059c29c5b62a9608187b1ef84789b")
        def text2 = AESUtils.decrypt(result, key)
        assertEquals(text2, text)
    }
}
