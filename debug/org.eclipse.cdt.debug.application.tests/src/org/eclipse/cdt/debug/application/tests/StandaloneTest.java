/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;

public abstract class StandaloneTest {

	private static final String C_C_STAND_ALONE_DEBUGGER_TITLE = "Eclipse C/C++ Stand-alone Debugger";
	private static final String DEBUG_NEW_EXECUTABLE_TITLE = "Debug New Executable";
	protected static SWTBot bot;
	protected static String projectName;
	protected static SWTBotShell mainShell;
	protected static SWTBotView projectExplorer;
	private static final Logger fLogger = Logger.getRootLogger();

    static class WaitForShell extends WaitForObjectCondition<Shell> {

        private Shell[] fShells;

        WaitForShell(Matcher<Shell> matcher) {
            super(matcher);
        }

        @Override
        public String getFailureMessage() {
            String shellTitles = UIThreadRunnable.syncExec(new Result<String>() {
                @Override
                public String run() {
                    return String.join(", ", Arrays.asList(fShells).stream().map(s -> "\"" + s.hashCode() + ": " + s.getText() + "\"").collect(Collectors.toList()));
                }
            });
            captureScreenshot("screenshots/fail.png");
            printJobs();
            return "Could not find shell matching: " + matcher + ". Found shell with titles: " + shellTitles; //$NON-NLS-1$
        }

        @Override
        protected List<Shell> findMatches() {
            fShells = findShells();
            ArrayList<Shell> matchingShells = new ArrayList<>();
            for (Shell shell : fShells) {
                if (!shell.isDisposed() && matcher.matches(shell)) {
                    matchingShells.add(shell);
                }
            }
            return matchingShells;
        }

        /**
         * Subclasses may override to find other shells.
         */
        Shell[] findShells() {
            return bot.getFinder().getShells();
        }

    }
    
    private static void printJobs() {
        Job[] jobs = Job.getJobManager().find(null);
        for (Job job : jobs) {
            System.err.println(job.toString() + " state: " + jobStateToString(job.getState())); //$NON-NLS-1$
            Thread thread = job.getThread();
            if (thread != null) {
                for (StackTraceElement stractTraceElement : thread.getStackTrace()) {
                    System.err.println("  " + stractTraceElement); //$NON-NLS-1$
                }
            }
            System.err.println();
        }
        
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Thread thread : allStackTraces.keySet()) {
            System.err.println(thread.toString() + " state: " + threadStateToString(thread.getState())); //$NON-NLS-1$
            if (thread != null) {
                for (StackTraceElement stractTraceElement : thread.getStackTrace()) {
                    System.err.println("  " + stractTraceElement); //$NON-NLS-1$
                }
            }
            System.err.println();
        }
    }
    
    private static String jobStateToString(int jobState) {
        switch (jobState) {
        case Job.RUNNING:
            return "RUNNING"; //$NON-NLS-1$
        case Job.WAITING:
            return "WAITING"; //$NON-NLS-1$
        case Job.SLEEPING:
            return "SLEEPING"; //$NON-NLS-1$
        case Job.NONE:
            return "NONE"; //$NON-NLS-1$
        default:
            return "UNKNOWN"; //$NON-NLS-1$
        }
    }
    
    private static String threadStateToString(Thread.State threadState) {
        switch (threadState) {
        case NEW:
            return "NEW"; //$NON-NLS-1$
        case RUNNABLE:
            return "RUNNABLE"; //$NON-NLS-1$
        case BLOCKED:
            return "BLOCKED"; //$NON-NLS-1$
        case WAITING:
            return "WAITING"; //$NON-NLS-1$
        case TIMED_WAITING:
            return "TIMED_WAITING"; //$NON-NLS-1$
        case TERMINATED:
            return "TERMINATED"; //$NON-NLS-1$
        default:
            return "UNKNOWN"; //$NON-NLS-1$
        }
    }
	
	public static void init(String projectName) throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 60000;

		fLogger.removeAllAppenders();
		fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));

		SWTBotUtils.initialize();

		bot = new SWTBot();
		Utilities.getDefault().buildProject(projectName);
		final IPath executablePath = Utilities.getDefault().getProjectPath(projectName).append("a.out"); //$NON-NLS-1$
		bot.waitUntil(new WaitForFileCondition(executablePath));

		captureScreenshot("screenshots/before.png");
		bot.waitUntil(new WaitForShell(WidgetMatcherFactory.withText(DEBUG_NEW_EXECUTABLE_TITLE)));
		SWTBotShell executableShell = bot.shell(DEBUG_NEW_EXECUTABLE_TITLE);
		executableShell.setFocus();
		bot.waitUntil(Conditions.shellIsActive(DEBUG_NEW_EXECUTABLE_TITLE));

		executableShell.bot().textWithLabel("Binary: ").typeText(executablePath.toOSString());
		executableShell.bot().button("OK").click();

		bot.waitUntil(new WaitForShell(WidgetMatcherFactory.withText(C_C_STAND_ALONE_DEBUGGER_TITLE)));
		mainShell = bot.shell(C_C_STAND_ALONE_DEBUGGER_TITLE);
		mainShell.setFocus();
		bot.waitUntil(Conditions.shellIsActive(C_C_STAND_ALONE_DEBUGGER_TITLE));
	}

	static void captureScreenshot(String pathname) {
		UIThreadRunnable.syncExec(() ->  {
			try {
				ImageHelper.grabImage(Display.getDefault().getBounds()).writePng(new File(pathname));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	@After
	public void cleanUp() {
		//		SWTBotShell[] shells = bot.shells();
		//		for (final SWTBotShell shell : shells) {
		//			if (!shell.equals(mainShell)) {
		//				String shellTitle = shell.getText();
		//				if (shellTitle.length() > 0
		//						&& !shellTitle.startsWith("Quick Access")) {
		//					UIThreadRunnable.syncExec(new VoidResult() {
		//						@Override
		//						public void run() {
		//							if (shell.widget.getParent() != null
		//									&& !shell.isOpen()) {
		//								shell.close();
		//							}
		//						}
		//					});
		//				}
		//			}
		//		}
		//		mainShell.activate();
	}

	/**
	 * Test class tear down method.
	 */
	@AfterClass
	public static void tearDown() {
		fLogger.removeAllAppenders();
	}

	private static final class WaitForFileCondition extends DefaultCondition {
		private final IPath executablePath;

		private WaitForFileCondition(IPath executablePath) {
			this.executablePath = executablePath;
		}

		@Override
		public boolean test() throws Exception {
			return executablePath.toFile().exists();
		}

		@Override
		public String getFailureMessage() {
			return "Could not find executable after build.";
		}
	}
}
