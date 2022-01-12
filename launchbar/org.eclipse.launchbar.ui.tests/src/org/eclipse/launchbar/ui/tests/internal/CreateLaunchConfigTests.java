/*******************************************************************************
 * Copyright (c) 2017, 2021 QNX Software Systems and others.
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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector.EditConfigDialog;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector.NewConfigDialog;
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
}
