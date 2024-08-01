/*******************************************************************************
 * Copyright (c) 2017, 2024 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.tests.internal;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector.EditConfigDialog;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector.NewConfigDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class CreateLaunchConfigTests {

	private static SWTWorkbenchBot bot;

	@BeforeAll
	public static void beforeClass() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 10000;
		bot = new SWTWorkbenchBot();
	}

	@BeforeEach
	public void before() {
		bot.resetWorkbench();

		for (SWTBotView view : bot.views(withPartName("Welcome"))) {
			view.close();
		}
	}

	@AfterEach
	public void after() {
		// Delete created launch configs after we are done with them
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		try {
			for (ILaunchConfiguration config : launchManager.getLaunchConfigurations()) {
				config.delete();
			}
		} catch (CoreException e) {
			fail(e);
		}
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	public void createNewLaunchConfig() throws Exception {
		String configName = "Test Config";

		// Create config with launchbar
		bot.waitUntil(new ICondition() {
			@Override
			public void init(SWTBot bot) {
				NewConfigDialog dialog = new SWTBotConfigSelector(bot).newConfigDialog();
				dialog.setMode("Debug").setType("Eclipse Application").next();
				dialog.bot().textWithLabel("Name:").setText(configName);
				dialog.finish();
			}

			@Override
			public boolean test() throws Exception {
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				for (ILaunchConfiguration config : launchManager.getLaunchConfigurations()) {
					if (config.getName().equals(configName)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getFailureMessage() {
				return "Test config not found";
			}
		});
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	public void editExistingLaunchConfig() throws Exception {
		// Create a launch config to edit
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType("org.eclipse.pde.ui.RuntimeWorkbench");
		ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "Test Config 2");
		wc.doSave();

		// Edit config with launchbar
		String configName = "Edited Config";
		bot.waitUntil(new ICondition() {
			@Override
			public void init(SWTBot bot) {
				EditConfigDialog dialog = new SWTBotConfigSelector(bot).editConfigDialog();
				dialog.bot().textWithLabel("Launch Configuration Name:").setText(configName);
				dialog.selectTab("Arguments");
				dialog.selectTab("My Custom Tab"); // See CustomLaunchConfigTab.java
				dialog.ok();
			}

			@Override
			public boolean test() throws Exception {
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				for (ILaunchConfiguration config : launchManager.getLaunchConfigurations()) {
					if (config.getName().equals(configName)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getFailureMessage() {
				return "Unable to edit test config";
			}
		});
	}

	/**
	 * Tests that when a new Launch Configuration is created, using the Launch Bar, it displays a
	 * warning icon/message in the message area.
	 * @see {@link WarningLaunchConfigTab}
	 * @see {@code org.eclipse.launchbar.ui.tests/plugin.xml}
	 */
	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	public void createNewLaunchConfigWithWarning() throws Exception {
		// Test message. Needs to be effectively final.
		AtomicReference<String> warningMessage = new AtomicReference<>();

		// Create config with launchbar
		bot.waitUntil(new ICondition() {
			@Override
			public void init(SWTBot bot) {
				/*
				 * Use the Launch Bar new Launch Config and create an Eclipse Application Launch Config.
				 * This will include a WarningLaunchConfigTab tab which has a warning set in it.
				 */
				NewConfigDialog dialog = new SWTBotConfigSelector(bot).newConfigDialog();
				dialog.setMode("Debug").setType("Eclipse Application").next();

				// Select the warning tab, which sets the Launch Config message area to contain a warning.
				dialog.bot().cTabItem(WarningLaunchConfigTab.TAB_NAME).activate();

				// Get the Launch Config message area and stash the warning message for testing later
				int indexOfErrorLabel = dialog.bot().widgets(widgetOfType(Text.class)).size() - 1;
				warningMessage.set(dialog.bot().text(indexOfErrorLabel).getText());

				dialog.finish();
			}

			@Override
			public boolean test() throws Exception {
				// The expected value has a space prefixed to account for an added space for the warning icon.
				return warningMessage.get() != null
						&& warningMessage.get().equals(" " + WarningLaunchConfigTab.WARNING_MESSAGE);
			}

			@Override
			public String getFailureMessage() {
				return String.format("Incorrect warning message; expected=%s, actual=%s",
						WarningLaunchConfigTab.WARNING_MESSAGE, warningMessage.get());
			}
		});
	}

	/**
	 * Tests that when editing an existing Launch Configuration, using the Launch Bar, it displays a
	 * warning icon/message in the message area.
	 * @see {@link WarningLaunchConfigTab}
	 * @see {@code org.eclipse.launchbar.ui.tests/plugin.xml}
	 */
	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	public void editExistingLaunchConfigWithWarning() throws Exception {
		// Test message. Needs to be effectively final.
		AtomicReference<String> warningMessage = new AtomicReference<>();

		// Create a launch config to edit (well to view)
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType("org.eclipse.pde.ui.RuntimeWorkbench");
		ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "Test Config 2");
		wc.doSave();

		// Edit config with launchbar
		bot.waitUntil(new ICondition() {
			@Override
			public void init(SWTBot bot) {
				// Open the launch config created above using the launch bar
				EditConfigDialog dialog = new SWTBotConfigSelector(bot).editConfigDialog();

				// Select the warning tab, which sets the Launch Config message area to contain a warning.
				dialog.selectTab(WarningLaunchConfigTab.TAB_NAME);

				// Get the Launch Config message area and stash the warning message for testing later
				int indexOfErrorLabel = dialog.bot().widgets(widgetOfType(Text.class)).size() - 1;
				warningMessage.set(dialog.bot().text(indexOfErrorLabel).getText());

				dialog.ok();
			}

			@Override
			public boolean test() throws Exception {
				// The expected value has a space prefixed to account for an added space for the warning icon.
				return warningMessage.get() != null
						&& warningMessage.get().equals(" " + WarningLaunchConfigTab.WARNING_MESSAGE);
			}

			@Override
			public String getFailureMessage() {
				return String.format("Incorrect warning message; expected=%s, actual=%s",
						WarningLaunchConfigTab.WARNING_MESSAGE, warningMessage.get());
			}
		});
	}
}
