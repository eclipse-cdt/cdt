/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.externaltool.ICommandLauncher;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.cdt.codan.core.externaltool.SingleConfigurationSetting;
import org.eclipse.cdt.codan.core.externaltool.SpaceDelimitedArgsSeparator;
import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.core.test.CodanTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Tests for <code>{@link ExternalToolInvoker}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
@SuppressWarnings("nls")
public class ExternalToolInvokerTest extends CodanTestCase {
	private static final String EXTERNAL_TOOL_NAME = "TestTool";

	private ConfigurationSettings configurationSettings;
	private IArgsSeparator argsSeparator;
	private List<AbstractOutputParser> parsers;
	private CommandLauncherStub commandLauncher;

	private ExternalToolInvoker externalToolInvoker;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		createConfigurationSettings();
		argsSeparator = new SpaceDelimitedArgsSeparator();
		parsers = new ArrayList<AbstractOutputParser>();
		commandLauncher = new CommandLauncherStub();
		externalToolInvoker = new ExternalToolInvoker(commandLauncher);
	}

	private void createConfigurationSettings() {
		configurationSettings = new ConfigurationSettings(EXTERNAL_TOOL_NAME, new File("testtool"),
				"", false);
		// Update current value of ConfigurationSettings from preferences.
		MapProblemPreference preferences = createPreferences(new File("usr/local/testtool"),
				"--debug=true --include=all", true);
		configurationSettings.updateValuesFrom(preferences);
	}

	private MapProblemPreference createPreferences(File path, String args,
			boolean shouldDisplayOutput) {
		MapProblemPreference preferences = new MapProblemPreference();
		preferences.addChildDescriptor(createPreference(PathSetting.KEY, path));
		preferences.addChildDescriptor(createPreference(ArgsSetting.KEY, args));
		preferences.addChildDescriptor(createPreference(ShouldDisplayOutputSetting.KEY,
				shouldDisplayOutput));
		return preferences;
	}

	private IProblemPreference createPreference(String key, Object value) {
		BasicProblemPreference preference = new BasicProblemPreference(key, "");
		preference.setValue(value);
		return preference;
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	// class C {
	// };
	public void testCommandLauncherGetsCalledCorrectly() throws Throwable {
		loadcode(getAboveComment());
		InvocationParameters parameters = new InvocationParameters(currentIFile, currentIFile,
				currentIFile.getLocation().toOSString(), cproject.getProject().getLocation());
		externalToolInvoker.invoke(parameters, configurationSettings, argsSeparator, parsers);
		commandLauncher.assertThatReceivedProject(cproject.getProject());
		commandLauncher.assertThatReceivedExternalToolName(EXTERNAL_TOOL_NAME);
		commandLauncher.assertThatReceivedExecutablePath(configurationSettings.getPath());
		commandLauncher.assertThatReceivedArgs(configurationSettings.getArgs());
		commandLauncher.assertThatReceivedWorkingDirectory(parameters.getWorkingDirectory());
		commandLauncher.assertThatReceivedShouldDisplayOutput(
				configurationSettings.getShouldDisplayOutput());
		commandLauncher.assetThatReceivedOutputParsers(parsers);
	}

	private static class CommandLauncherStub implements ICommandLauncher {
		private IProject project;
		private String externalToolName;
		private IPath executablePath;
		private String[] args;
		private IPath workingDirectory;
		private boolean shouldDisplayOutput;
		private List<AbstractOutputParser> parsers;

		@Override
		public void buildAndLaunchCommand(IProject project, String externalToolName,
				IPath executablePath, String[] args, IPath workingDirectory,
				boolean shouldDisplayOutput, List<AbstractOutputParser> parsers) throws InvocationFailure,
				Throwable {
			this.project = project;
			this.externalToolName = externalToolName;
			this.executablePath = executablePath;
			this.args = args;
			this.workingDirectory = workingDirectory;
			this.shouldDisplayOutput = shouldDisplayOutput;
			this.parsers = parsers;
		}

		void assertThatReceivedProject(IProject expected) {
			assertEquals(expected, project);
		}

		void assertThatReceivedExternalToolName(String expected) {
			assertEquals(expected, externalToolName);
		}

		void assertThatReceivedExecutablePath(SingleConfigurationSetting<File> expected) {
			String expectedPath = expected.getValue().toString();
			assertEquals(expectedPath, executablePath.toOSString());
		}

		void assertThatReceivedArgs(SingleConfigurationSetting<String> expected) {
			String[] expectedArgs = expected.getValue().split("\\s+");
			assertArrayEquals(expectedArgs, args);
		}

		void assertThatReceivedWorkingDirectory(IPath expected) {
			assertSame(expected, workingDirectory);
		}

		void assertThatReceivedShouldDisplayOutput(SingleConfigurationSetting<Boolean> expected) {
			assertEquals(expected.getValue().booleanValue(), shouldDisplayOutput);
		}

		void assetThatReceivedOutputParsers(List<AbstractOutputParser> expected) {
			assertSame(expected, parsers);
		}
	}
}
