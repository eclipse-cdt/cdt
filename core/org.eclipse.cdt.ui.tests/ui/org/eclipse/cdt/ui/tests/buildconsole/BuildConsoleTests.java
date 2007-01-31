/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.buildconsole;

import java.io.IOException;

import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.tests.text.DisplayHelper;

/**
 * BuildConsoleTests.
 */
public class BuildConsoleTests extends BaseUITestCase {

	private ICProject fCProject;

	public BuildConsoleTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(BuildConsoleTests.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject(getName(), "unused", IPDOMManager.ID_FAST_INDEXER);
	}

	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
		fCProject= null;
		super.tearDown();
	}

	public void testSecondaryBuildConsole() throws IOException, CoreException {
		IBuildConsoleManager mgr= CUIPlugin.getDefault().getConsoleManager("My Other Console", "cdt.ui.testConsole");
		IConsole console= mgr.getConsole(fCProject.getProject());
		String stdoutText = "This is stdout\n";
		console.getOutputStream().write(stdoutText.getBytes());
		String stderrText = "This is stderr\n";
		console.getErrorStream().write(stderrText.getBytes());
		DisplayHelper.sleep(CUIPlugin.getStandardDisplay(), 200);
		IDocument doc= mgr.getConsoleDocument(fCProject.getProject());
		assertEquals(stdoutText+stderrText, doc.get());
	}
}
