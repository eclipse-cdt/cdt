package org.eclipse.cdt.dsf.gdb.internal.swtbot.tests;

import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.Test;

/**
 *
 */
public class SampleTest extends BaseSWTBotTestCase {
    /**
     *
     */
    @Test
    public void test1_01_First_attempt() {
        System.out.println("entering test case, pending jobs:\n");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("jobs pending before finishing test case");
        WaitUtils.printJobs();
    }
}
