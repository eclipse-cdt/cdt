package org.eclipse.cdt.dsf.gdb.internal.ui.launching.tests;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteTCPLaunchTargetProvider;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StandardBaudRates;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.tests.SWTBotTargetSelector;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.eclipse.launchbar.ui.tests.SWTBotTargetSelector.EditTargetDialog;
import org.eclipse.launchbar.ui.tests.SWTBotTargetSelector.NewTargetDialog;

public class GdbRemoteTargetWizardTest {

	private static SWTWorkbenchBot bot;

	@TempDir
	public static Path TEMP_DIR;

	@BeforeAll
	public static void beforeClass() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 15000;
		bot = new SWTWorkbenchBot();
	}

	@BeforeEach
	public void before() {
		bot.resetWorkbench();

		for (SWTBotView view : bot.views(withPartName("Welcome"))) {
			view.close();
		}
		SWTBotPerspective perspective = bot.perspectiveById("org.eclipse.cdt.ui.CPerspective");
		perspective.activate();
		bot.shell().activate();
		//enableLaunchBar();
	}

	@SuppressWarnings("restriction")
	public void enableLaunchBar() {
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);

		// Execute the command to show the LaunchBar
		try {
			handlerService.executeCommand("org.eclipse.launchbar.ui.commands.showLaunchBar", null);
			IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
			store.setValue("alwaysShowTargetSelector", true);
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			e.printStackTrace();
		}

	}

	private IProject createCMakeProject(String name) throws Exception {
		// Create a plain Eclipse project
		IProject project = ResourceHelper.createProject(name);
		// Add C/C++ and CMake natures to make it a CMake project
		IProjectDescription description = project.getDescription();
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, CMakeNature.ID });
		project.setDescription(description, null);
		return project;
	}

	/**
	 * Test creating a GDB Remote TCP launch target via the wizard and
	 * verify that the settings are correctly visible when editing the target.
	 * @throws Exception
	 */
	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	void testGdbRemoteTCPTargetWizard() throws Exception {

		IProject project = createCMakeProject("TestProject");

		String testHost = "localhost";
		String testPort = "2001";

		// Open the New Launch Target dialog
		//		NewTargetDialog dialog = new SWTBotTargetSelector(bot).newTargetDialog();
		//		dialog.setType("GDB Remote TCP").next();
		//		dialog.bot().textWithLabel("Hostname or IP:").setText(testHost);
		//		dialog.bot().textWithLabel("Port:").setText(testPort);
		//		dialog.finish();

		//		// Or this code.

		// Open the New Launch Target dialog
		bot.labelWithTooltip("Launch Target: OK").click();
		bot.label("New Launch Target...").click();
		SWTBotShell shell = bot.activeShell();
		shell.setFocus();
		// Fill in the dialog fields
		bot.table().select("GDB Remote TCP");
		bot.button("Next >").click();
		bot.textWithLabel("Hostname or IP:").setText(testHost);
		bot.textWithLabel("Port:").setText(testPort);
		// Finish the wizard
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell));

		// Edit the launch target to verify settings
		// FIXME: Next line hangs and test ends up timing out.
		//EditTargetDialog editDialog = new SWTBotTargetSelector(bot).editTCPTargetDialog();
		//String host = editDialog.bot().textWithLabel("Hostname or IP:").getText();
		//String port = editDialog.bot().textWithLabel("Port:").getText();
		//editDialog.finish();

		// Edit the launch target to verify settings
		SWTBotTargetSelector selector = new SWTBotTargetSelector(bot);
		selector.select(testHost);
		selector.clickEdit();
		bot.waitUntil(Conditions.shellIsActive("Edit TCP Launch Target"));
		SWTBotShell shell2 = bot.activeShell();
		shell2.setFocus();
		// Getting the wrong shell "Property Pages", now and then. Need to debug further.
		String host = bot.textWithLabel("Hostname or IP:").getText();
		String port = bot.textWithLabel("Port:").getText();
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell2));

		assertThat(host, is(testHost));
		assertThat(port, is(testPort));

		bot.closeAllShells();
	}

	/**
	 * Test creating a GDB Remote Serial launch target via the wizard and
	 * verify that the settings are correctly visible when editing the target.
	 * @throws Exception
	 */
	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	void testGdbRemoteSerialTargetWizard() throws Exception {

		IProject project = createCMakeProject("TestProject");

		String[] serialPorts;
		try {
			serialPorts = SerialPort.list();

		} catch (IOException e) {
			GdbUIPlugin.log(e);
			serialPorts = new String[0];
		}

		if (serialPorts.length == 0) {
			// Skip the test if no serial ports are available
			return;
		}

		String testPort = serialPorts[0];
		String testRate = String.valueOf(StandardBaudRates.getDefault());

		// Open the New Launch Target dialog
		//		bot.waitUntil(new ICondition() {
		//
		//			@Override
		//			public void init(SWTBot bot) {
		//				NewTargetDialog dialog = new SWTBotTargetSelector(bot).newTargetDialog();
		//				dialog.setType("GDB Remote Serial").next();
		//				dialog.bot().comboBoxWithLabel("Serial port:").setText(testPort);
		//				dialog.bot().comboBoxWithLabel("Baud rate:").setText(testRate);
		//				dialog.finish();
		//				System.out.println("INIT done");
		//			}
		//
		//			@Override
		//			public boolean test() throws Exception {
		//
		////				System.out.println("A");
		////		        // Test hangs here.
		////				EditTargetDialog editDialog = new SWTBotTargetSelector(bot).editSerialTargetDialog();
		////				System.out.println("B");
		////				String host = editDialog.bot().comboBoxWithLabel("Serial port:").getText();
		////				String port = editDialog.bot().comboBoxWithLabel("Baud rate:").getText();
		////				System.out.println("D");
		////				editDialog.finish();
		////				System.out.println("Host: " + host + ", Port: " + port);
		//
		//              // OR use the code below to verify settings
		//
		//				// Edit the launch target to verify settings
		//				SWTBotTargetSelector selector = new SWTBotTargetSelector(bot);
		//				selector.select(testPort);
		//				selector.clickEdit();
		//				bot.waitUntil(Conditions.shellIsActive("Edit Serial Launch Target"));
		//				SWTBotShell shell = bot.activeShell();
		//				shell.setFocus();
		//		        // Often not correct shell here.
		//				String port = bot.comboBoxWithLabel("Serial port:").getText();
		//				System.out.println("B");
		//				String rate = bot.comboBoxWithLabel("Baud rate:").getText();
		//				bot.button("Finish").click();
		//				bot.waitUntil(Conditions.shellCloses(shell));
		//
		//				assertThat(port, is(testPort));
		//				assertThat(rate, is(testRate));
		//
		//				return true;
		//			}
		//
		//			@Override
		//			public String getFailureMessage() {
		//				return "Failed to create GDB Remote Serial launch target";
		//			}
		//
		//		});

		NewTargetDialog dialog = new SWTBotTargetSelector(bot).newTargetDialog();
		dialog.setType("GDB Remote Serial").next();
		dialog.bot().comboBoxWithLabel("Serial port:").setText(testPort);
		dialog.bot().comboBoxWithLabel("Baud rate:").setText(testRate);
		dialog.finish();
		bot.waitUntil(Conditions.shellCloses(dialog));

		//		EditTargetDialog editDialog = new SWTBotTargetSelector(bot).editSerialTargetDialog();
		//		System.out.println("B");
		//		String host = editDialog.bot().comboBoxWithLabel("Serial port:").getText();
		//		String port = editDialog.bot().comboBoxWithLabel("Baud rate:").getText();
		//		//editDialog.bot().wait(5000);
		//		System.out.println("D");
		//		editDialog.finish();
		//		System.out.println("Host: " + host + ", Port: " + port);

		// Edit the launch target to verify settings
		System.out.println("A");
		SWTBotTargetSelector selector = new SWTBotTargetSelector(bot);
		System.out.println("AA");
		//selector.setFocus();
		System.out.println("AB");
		selector.select(testPort);
		selector.clickEdit();
		System.out.println("B");
		// Often not correct shell here. This can wait till timeout.
		bot.waitUntil(Conditions.shellIsActive("Edit Serial Launch Target"));
		SWTBotShell[] shells = bot.shells();
		for (SWTBotShell s : shells) {
			System.out.println("Shell txt: " + s.getText());
			System.out.println("Shell id : " + s.getId());
			System.out.println("Shell tip: " + s.getToolTipText());
		}
		SWTBotShell shell = bot.activeShell();
		shell.setFocus();
		System.out.println("C");
		// Getting the wrong shell "Property Pages", test hangs on next line.
		// Sometimes works though.
		String port = bot.comboBoxWithLabel("Serial port:").getText();
		System.out.println("D");
		String rate = bot.comboBoxWithLabel("Baud rate:").getText();
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell));

		assertThat(port, is(testPort));
		assertThat(rate, is(testRate));

		bot.closeAllShells();
	}

	/**
	 * Creating a GDB Remote TCP launch target programmatically and
	 * verify that the settings are correctly visible when editing the target.
	 * @throws Exception
	 */
	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	void testEditGdbRemoteTCPTargetWizard() throws Exception {

		IProject project = createCMakeProject("TestProject");

		String testHost = "localhost";
		String testPort = "2001";

		// Open the New Launch Target dialog
		bot.waitUntil(new ICondition() {

			@Override
			public void init(SWTBot bot) {
				ILaunchTargetManager manager = GdbUIPlugin.getService(ILaunchTargetManager.class);

				ILaunchTarget target = manager.addLaunchTarget(GDBRemoteTCPLaunchTargetProvider.TYPE_ID, testHost);

				ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
				wc.setId(testHost);
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, testHost);
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, testPort);
				wc.save();
			}

			@Override
			public boolean test() throws Exception {

				//				System.out.println("A");
				//		        // Test hangs here.
				// Getting the wrong shell "Property Pages". Need to debug further.
				//				EditTargetDialog editDialog = new SWTBotTargetSelector(bot).editTCPTargetDialog();
				//				System.out.println("B");
				//				String host = editDialog.bot().comboBoxWithLabel("Serial port:").getText();
				//				String port = editDialog.bot().comboBoxWithLabel("Baud rate:").getText();
				//				//editDialog.bot().wait(5000);
				//				System.out.println("D");
				//				editDialog.finish();
				//				System.out.println("Host: " + host + ", Port: " + port);

				// Edit the launch target to verify settings
				SWTBotTargetSelector selector = new SWTBotTargetSelector(bot);
				selector.select(testHost);
				selector.clickEdit();
				bot.waitUntil(Conditions.shellIsActive("Edit TCP Launch Target"));
				SWTBotShell shell2 = bot.activeShell();
				shell2.setFocus();
				String host = bot.textWithLabel("Hostname or IP:").getText();
				String port = bot.textWithLabel("Port:").getText();
				bot.button("Finish").click();
				bot.waitUntil(Conditions.shellCloses(shell2));

				assertThat(port, is(testPort));
				assertThat(host, is(testHost));

				return true;
			}

			@Override
			public String getFailureMessage() {
				return "Failed to edit GDB Remote TCP launch target";
			}
		});

	}
}
