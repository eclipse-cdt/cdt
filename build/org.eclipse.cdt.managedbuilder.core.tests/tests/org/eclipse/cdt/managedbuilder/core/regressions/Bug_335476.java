/*******************************************************************************
 * Copyright (c) 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Broadcom Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.regressions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;

/**
 * This tests that an environment variable, which is part of the build
 * (in this case referenced by a -I), makes it through to makefile
 * correctly when it changes.
 */
public class Bug_335476 extends AbstractBuilderTest {

	private final String VAR_NAME = "INC";
	IProject app;
	IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
	ICdtVariableManager buildMacroManager = CCorePlugin.getDefault().getCdtVariableManager();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setWorkspace("regressions");
		app = loadProject("bug_335476");
		// Ensure Debug is the active configuration
		setActiveConfigurationByName(app, "Debug");
	}

	/**
	 * Build the project a few times, changing the value of the environment variable each time
	 * @param build_kind
	 * @throws Exception
	 */
	public void runTest(int build_kind) throws Exception {
		// Environment containingg the "Lala" environment variable
		final IFile lala = app.getFile(new Path(".settings/org.eclipse.cdt.core.prefs.lala"));
		// Environment containing the "Foo" environment variable
		final IFile foo = app.getFile(new Path(".settings/org.eclipse.cdt.core.prefs.foo"));

		final IFile env = app.getFile(new Path(".settings/org.eclipse.cdt.core.prefs"));

		IFile current = foo;
		for (int i = 0; i < 5; i++) {
			// Actual expected value
			final String expected = current == foo ? "foo" : "lala";
			// Update the environment to reflect the new value.
			env.setContents(current.getContents(), IResource.NONE, null);

			// Ask for a full build
			app.build(build_kind, null);

			// Check the makefile for the correct environment
			IFile makefile = app.getFile("Debug/src/subdir.mk");
			BufferedReader reader = new BufferedReader(new InputStreamReader(makefile.getContents()));
			try {
				Pattern p = Pattern.compile(".*?-I.*?\"(.*?)\".*");
				boolean found = false;
				while (reader.ready()) {
					String line = reader.readLine();
					if (!line.contains("gcc"))
						continue;
					Matcher m = p.matcher(line);
					assertTrue(m.matches());
					String buildVar = m.group(1);

					// Check that the Environment manager + the build manager have the variable
					//  (which tells us how far the variable has got through the system...)
					String value = envManager.getVariable(VAR_NAME,
							CCorePlugin.getDefault().getProjectDescription(app, false).getActiveConfiguration(), false)
							.getValue();
					String value2 = buildMacroManager.resolveValue("${" + VAR_NAME + "}", "", ";",
							CCorePlugin.getDefault().getProjectDescription(app, false).getActiveConfiguration());

					assertTrue(i + " EnvManager " + expected + " exepected, but was: " + value, expected.equals(value));
					assertTrue(i + " CdtVarManager " + expected + " exepected, but was: " + value2,
							expected.equals(value2));
					assertTrue(i + " Makefile: " + expected + " exepected, but was: " + buildVar,
							expected.equals(buildVar));
					found = true;
				}
				// Check that we at least matched
				assertTrue(found);
			} finally {
				reader.close();
			}

			// Change environment
			if (current == lala)
				current = foo;
			else
				current = lala;
		}
	}

	public void testChangingEnvironmentBuildSystem_FULL_BUILD() throws Exception {
		runTest(IncrementalProjectBuilder.FULL_BUILD);
	}

	public void testChangingEnvironmentBuildSystem_INC_BUILD() throws Exception {
		runTest(IncrementalProjectBuilder.INCREMENTAL_BUILD);
	}

}
