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

import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.externaltool.ICommandLauncher;
import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.cdt.codan.core.externaltool.SpaceDelimitedArgsSeparator;
import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
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
		preferences.addChildDescriptor(new BasicProblemPreference("externalToolPath", "Path"));
		preferences.addChildDescriptor(new BasicProblemPreference("externalToolArgs", "Args"));
		preferences.addChildDescriptor(new BasicProblemPreference("externalToolShouldDisplayOutput",
				"Should Display Output"));
		preferences.setChildValue("externalToolPath", path);
		preferences.setChildValue("externalToolArgs", args);
		preferences.setChildValue("externalToolShouldDisplayOutput", shouldDisplayOutput);
		return preferences;
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
		assertSame(cproject.getProject(), commandLauncher.project);
		assertEquals(EXTERNAL_TOOL_NAME, commandLauncher.externalToolName);
		String expectedExecutablePath = configurationSettings.getPath().getValue().toString();
		assertEquals(expectedExecutablePath, commandLauncher.executablePath.toOSString());
		String[] expectedArgs = { parameters.getActualFilePath(), "--debug=true", "--include=all" };
		assertArrayEquals(expectedArgs, commandLauncher.args);
		assertEquals(parameters.getWorkingDirectory(), commandLauncher.workingDirectory);
		assertEquals(configurationSettings.getShouldDisplayOutput().getValue().booleanValue(),
				commandLauncher.shouldDisplayOutput);
		assertSame(parsers, commandLauncher.parsers);
	}

	private static class CommandLauncherStub implements ICommandLauncher {
		IProject project;
		String externalToolName;
		IPath executablePath;
		String[] args;
		IPath workingDirectory;
		boolean shouldDisplayOutput;
		List<AbstractOutputParser> parsers;

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
	}
}
