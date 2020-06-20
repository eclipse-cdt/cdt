/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector;
import org.eclipse.launchbar.ui.tests.SWTBotConfigSelector.NewConfigDialog;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateLaunchConfigTests {

	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 10000;
		bot = new SWTWorkbenchBot();
	}

	@Before
	public void before() {
		bot.resetWorkbench();

		for (SWTBotView view : bot.views(withPartName("Welcome"))) {
			view.close();
		}
	}

	@Test
	public void createEclipseApplication() throws Exception {
		String configName = "Test Config";
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunchConfiguration config : launchManager.getLaunchConfigurations()) {
			if (config.getName().equals(configName)) {
				config.delete();
			}
		}

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

}
