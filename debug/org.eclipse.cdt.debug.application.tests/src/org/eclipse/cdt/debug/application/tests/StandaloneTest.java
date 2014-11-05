package org.eclipse.cdt.debug.application.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;

public abstract class StandaloneTest {

	protected static SWTBot bot;
	protected static String projectName;
	protected static SWTBotShell mainShell;
	protected static SWTBotView projectExplorer;

	public static void init(String projectName) throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";

		Utilities.getDefault().buildProject(projectName);
		bot = new SWTBot();

		SWTBotShell executableShell = null;
		for (int i = 0, attempts = 100; i < attempts; i++) {
			for (SWTBotShell shell : bot.shells()) {
				if (shell.getText().contains("Debug New Executable")) {
					executableShell = shell;
					shell.setFocus();
					break;
				}
			}
			if (executableShell != null)
				break;
			bot.sleep(10);
		}

		IPath executablePath = Utilities.getDefault().getProjectPath(projectName).append("a.out"); // $NON-NLS-1$

		executableShell.bot().textWithLabel("Binary: ").typeText(executablePath.toOSString());
		bot.sleep(2000);

		executableShell.bot().button("OK").click();

		mainShell = null;
		for (int i = 0, attempts = 100; i < attempts; i++) {
			for (SWTBotShell shell : bot.shells()) {
				if (shell.getText().contains("C/C++ Stand-alone Debugger")) {
					mainShell = shell;
					shell.setFocus();
					break;
				}
			}
			if (mainShell != null)
				break;
			bot.sleep(10);
		}
		assertNotNull(mainShell);
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

}
