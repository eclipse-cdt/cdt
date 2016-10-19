package org.eclipse.cdt.dsf.gdb.internal.swtbot.tests;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.cdt.dsf.gdb.internal.swtbot.tests.utils.SWTBotDbgConstants;
import org.eclipse.cdt.dsf.gdb.internal.swtbot.tests.utils.SWTBotDbgUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author lmcalvs
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class BaseSWTBotTestCase {
    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static final String DEBUG_PROJECT_NAME = "MThreadPrj";
    private static final String DEBUG_PROJECT_FOLDER = "MThread";

    /**
     * @throws IOException
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        // Switch perspective
        SWTBotUtils.switchToPerspective(SWTBotDbgConstants.DEBUG_PERSPECTIVE);

        // open project explorer
        SWTBotUtils.openView(SWTBotDbgConstants.PROJECT_EXPLORER_VIEW);

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        // import test stub project
        SWTBotDbgUtils.importStubProject(fBot, DEBUG_PROJECT_FOLDER, null, new NullProgressMonitor());
        WaitUtils.waitForJobs();

        // Make sure the project has been successfully imported
        IProject project = SWTBotDbgUtils.getProject(DEBUG_PROJECT_NAME);
        assertNotNull(project);

        // launch project
        SWTBotDbgUtils.launchProject(fBot, DEBUG_PROJECT_NAME, new NullProgressMonitor());
        System.out.println("back from launching");

        // Wait until the launching is completed
        SWTBotDbgUtils.waitForJob(SWTBotDbgConstants.PROCESS_CONSOLE_JOB_NAME);
        System.out.println("Job found: " + SWTBotDbgConstants.PROCESS_CONSOLE_JOB_NAME);
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        // Terminate the session
        System.out.println("entering tearDown");
        SWTBotDbgUtils.stopProject(fBot, DEBUG_PROJECT_NAME, new NullProgressMonitor());
        SWTBotDbgUtils.waitForJobGone(SWTBotDbgConstants.PROCESS_CONSOLE_JOB_NAME);

        // Delete project and its resources
        SWTBotDbgUtils.deleteProject(DEBUG_PROJECT_NAME, true, fBot);
        System.out.println("jobs remaining at the end of teardown");
        WaitUtils.printJobs();

        fLogger.removeAllAppenders();
    }

    /**
     * Test tear down method.
     */
    @After
    public void afterTest() {
        System.out.println("After test, closing secondary shells");
        SWTBotUtils.closeSecondaryShells(fBot);
    }
}
