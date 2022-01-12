/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application.tests;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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

	public static void init(String projectName) throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 20000;

		fLogger.removeAllAppenders();
		fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));

		bot = new SWTBot();
		Utilities.getDefault().buildProject(projectName);
		final IPath executablePath = Utilities.getDefault().getProjectPath(projectName).append("a.out"); //$NON-NLS-1$
		bot.waitUntil(new WaitForFileCondition(executablePath));

		bot.waitUntil(Conditions.shellIsActive(DEBUG_NEW_EXECUTABLE_TITLE));
		SWTBotShell executableShell = bot.shell(DEBUG_NEW_EXECUTABLE_TITLE);
		executableShell.setFocus();

		executableShell.bot().textWithLabel("Binary: ").typeText(executablePath.toOSString());
		executableShell.bot().button("OK").click();

		bot.waitUntil(Conditions.shellIsActive(C_C_STAND_ALONE_DEBUGGER_TITLE));
		mainShell = bot.shell(C_C_STAND_ALONE_DEBUGGER_TITLE);
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
