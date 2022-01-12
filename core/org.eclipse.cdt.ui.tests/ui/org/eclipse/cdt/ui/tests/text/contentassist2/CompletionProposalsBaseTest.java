/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

/**
 * @author hamer
 *
 *	This abstract class is the base class for all completion proposals test cases.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public abstract class CompletionProposalsBaseTest extends AbstractContentAssistTest {
	private boolean fFailingTest;

	public CompletionProposalsBaseTest(String name) {
		super(name, true);
	}

	public CompletionProposalsBaseTest(String name, boolean isCpp) {
		super(name, isCpp);
	}

	@Override
	public String getName() {
		if (fFailingTest) {
			return "[Failing] " + super.getName();
		}
		return super.getName();
	}

	@Override
	public void setExpectFailure(int bugnumber) {
		super.setExpectFailure(bugnumber);
		fFailingTest = true;
	}

	/*
	 * Derived classes have to provide the file locations
	 */
	protected abstract String getFileName();

	protected abstract String getFileFullPath();

	protected abstract String getHeaderFileName();

	protected abstract String getHeaderFileFullPath();

	/*
	 * Derived classes have to provide these test parameters
	 */
	protected abstract int getCompletionPosition();

	protected abstract String getExpectedPrefix();

	protected abstract String[] getExpectedResultsValues();

	@Override
	protected IFile setUpProjectContent(IProject project) throws FileNotFoundException {
		IFile headerFile = project.getFile(getHeaderFileName());
		String fileName = getFileName();
		IFile bodyFile = project.getFile(fileName);
		if ((!bodyFile.exists()) && (!headerFile.exists())) {
			IProgressMonitor monitor = new NullProgressMonitor();
			try {
				FileInputStream headerFileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path(getHeaderFileFullPath())));
				headerFile.create(headerFileIn, false, monitor);
				FileInputStream bodyFileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path(getFileFullPath())));
				bodyFile.create(bodyFileIn, false, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return bodyFile;
	}

	protected final static int DEFAULT_FLAGS = AbstractContentAssistTest.DEFAULT_FLAGS | IS_COMPLETION;

	protected void assertCompletionResults(int offset, String[] expected, CompareType compareType) throws Exception {
		assertContentAssistResults(offset, expected, DEFAULT_FLAGS, compareType);
	}

	public void testCompletionProposals() throws Exception {
		String[] expected = getExpectedResultsValues();
		assertCompletionResults(getCompletionPosition(), expected, CompareType.DISPLAY);
	}
}