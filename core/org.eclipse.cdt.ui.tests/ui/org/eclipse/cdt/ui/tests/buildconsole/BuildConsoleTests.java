/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.buildconsole;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsole;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePage;
import org.eclipse.cdt.internal.ui.buildconsole.ConsoleMessages;
import org.eclipse.cdt.internal.ui.buildconsole.GlobalBuildConsoleManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.cdt.ui.testplugin.Accessor;
import org.eclipse.cdt.ui.testplugin.DisplayHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.console.ConsolePlugin;

import junit.framework.TestSuite;

/**
 * BuildConsoleTests.
 */
public class BuildConsoleTests extends BaseUITestCase {

	public BuildConsoleTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(BuildConsoleTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp(getName());
		super.tearDown();
	}

	public void testSecondaryBuildConsole() throws IOException, CoreException {
		IProject project = ResourceHelper.createCDTProject(getName());
		IBuildConsoleManager mgr = CUIPlugin.getDefault().getConsoleManager("My Other Console", "cdt.ui.testConsole");
		IConsole console = mgr.getConsole(project);
		String stdoutText = "This is stdout\n";
		console.getOutputStream().write(stdoutText.getBytes());
		String stderrText = "This is stderr\n";
		console.getErrorStream().write(stderrText.getBytes());
		DisplayHelper.sleep(CUIPlugin.getStandardDisplay(), 200);
		IDocument doc = mgr.getConsoleDocument(project);
		assertEquals(stdoutText + stderrText, doc.get());
	}

	public void testShowConsoleForNonCDTProject_bug306945() throws IOException, CoreException {
		IProject simpleProject = ResourceHelper.createProject("non_c_project");

		IBuildConsoleManager mgr = CUIPlugin.getDefault().getConsoleManager();
		IConsole console = mgr.getConsole(simpleProject);

		// show the console view
		org.eclipse.ui.console.IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		BuildConsole buildConsole = null;
		for (org.eclipse.ui.console.IConsole next : consoles) {
			if (next instanceof BuildConsole) {
				buildConsole = (BuildConsole) next;
				if (buildConsole.getName().contains(simpleProject.getName()))
					break;
			}
		}
		assertNotNull("Couldn't find the build console", buildConsole);

		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(buildConsole);
		buildConsole.activate(); // force activation

		// verify that the text was correctly written
		String stdoutText = "This is stdout\n";
		console.getOutputStream().write(stdoutText.getBytes());
		String stderrText = "This is stderr\n";
		console.getErrorStream().write(stderrText.getBytes());
		DisplayHelper.sleep(CUIPlugin.getStandardDisplay(), 200);

		IDocument doc = mgr.getConsoleDocument(simpleProject);
		assertEquals("Text not written to console", stdoutText + stderrText, doc.get());

		// verify that the Console view can show the console to the user
		BuildConsolePage page = (BuildConsolePage) new Accessor(BuildConsole.class).invoke("getCurrentPage");
		assertNotNull("Couldn't get the build console page", page);

		page.selectionChanged(null, new StructuredSelection(simpleProject));
		DisplayHelper.sleep(CUIPlugin.getStandardDisplay(), 200);

		buildConsole = (BuildConsole) new Accessor(page).invoke("getConsole");
		assertTrue("Project console not selected", buildConsole.getName().contains(simpleProject.getName()));
	}

	public void testGlobalCdtConsole() throws IOException, CoreException {
		IConsole globalConsole = GlobalBuildConsoleManager.getGlobalConsole();
		assertNotNull(globalConsole);

		// the console view
		org.eclipse.ui.console.IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		boolean isConsoleFound = false;
		for (org.eclipse.ui.console.IConsole console : consoles) {
			isConsoleFound = console.getName().equals(ConsoleMessages.BuildConsole_GlobalConsole);
			if (isConsoleFound)
				break;
		}
		assertTrue("Global CDT Console is not found", isConsoleFound);
	}

	public void testDynamicBuildConsole() throws IOException, CoreException {
		String id = this.getName();
		String consoleName = "Test " + this.getName();
		IConsole testConsole = CCorePlugin.getDefault().getBuildConsole(id, consoleName, null);
		assertNotNull(testConsole);

		// the console view
		org.eclipse.ui.console.IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		org.eclipse.ui.console.IConsole uiConsole = null;
		for (org.eclipse.ui.console.IConsole console : consoles) {
			boolean isConsoleFound = console.getName().equals(consoleName);
			if (isConsoleFound) {
				uiConsole = console;
				break;
			}
		}
		assertNotNull("Build Console is not found", uiConsole);
		assertTrue(uiConsole instanceof BuildConsole);
	}
}
