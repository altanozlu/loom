/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package MyPackage;

/**
 * @test
 * @summary Verifies JVMTI support for Continuations
 * @compile ContinuationTest.java
 * @run main/othervm/native -agentlib:ContinuationTest MyPackage.ContinuationTest
 */

public class ContinuationTest {
    private static final String agentLib = "ContinuationTest";
    private static final ContinuationScope FOO = new ContinuationScope() {};

    static void log(String str) { System.out.println(str); }
    static native void enableEvents(Thread thread);
    static native boolean check();

    public static void main(String[] args) throws Exception {
        try {
            System.loadLibrary(agentLib);
        } catch (UnsatisfiedLinkError ex) {
            log("Failed to load " + agentLib + " lib");
            log("java.library.path: " + System.getProperty("java.library.path"));
            throw ex;
        }
        log("\n####### main: started ######\n");
        enableEvents(Thread.currentThread());

        ContinuationTest obj = new ContinuationTest();
        obj.runTest();

        check();
        log("\n###### main: finished ######\n");
    }

    public void runTest() {
        Continuation cont = new Continuation(FOO, ()-> { 
            double dval = 0;
            for (int k = 1; k < 3; k++) {
                int ival = 3;
                String str = "abc";

                log("\n<<<< runTest: before foo(): " + ival + ", " + str + ", " + dval + " <<<<"); 
                dval += foo(k);
	        log(  ">>>> runTest:  after foo(): " + ival + ", " + str + ", " + dval + " >>>>"); 
            }
        });
        int i = 0;
        while (!cont.isDone()) {
            log("\n#### runTest: iteration #" + (i++));
            cont.run();
/*
            StackTraceElement[] stes = Thread.currentThread().getStackTrace();
            log("--- getStackTrace: after cont.run()");
            log(java.util.Arrays.toString(stes));
*/
            System.gc();
        }
    }

    static double foo(int iarg) {
        long lval = 8;
        String str1 = "yyy";

        log("foo: before bar(): " + lval + ", " + str1 + ", " + iarg);
        String str2 = bar(iarg + 1);
	log("foo:  after bar(): " + lval + ", " + str1 + ", " + str2); 

        return Integer.parseInt(str2) + 1;
    }

    static String bar(long larg) {
        double dval = 9.99;
        String str = "zzz";

        log("bar: before yield(): " + dval + ", " + str + ", " + larg); 
/*
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        log("--- getStackTrace: before yield()");
        log(java.util.Arrays.toString(stes));
*/
        Continuation.yield(FOO);

        long lval = larg + 1;
        log("bar:  after yield(): " + dval + ", " + str + ", " + lval); 

        return "" + lval;
    }
}
