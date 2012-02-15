/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;

/**
 * Don't create new tests based on this class. Use RefactoringTestBase instead.
 * 
 * @author Emanuel Graf
 */
public abstract class RefactoringTest extends RefactoringBaseTest {
	private static final String CONFIG_FILE_NAME = ".config"; //$NON-NLS-1$

	protected String fileName;
	protected boolean fatalError;
	protected int initialErrors;
	protected int initialWarnings;
	protected int finalWarnings;
	protected int finalInfos;

	public RefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
		initializeConfiguration(files);
	}

	protected abstract void configureRefactoring(Properties refactoringProperties);

	protected void executeRefactoring(Refactoring refactoring) throws CoreException {
		RefactoringContext context = refactoring instanceof CRefactoring2 ?
				new CRefactoringContext((CRefactoring2) refactoring) :
				new RefactoringContext(refactoring);
		executeRefactoring(refactoring, context, true);
	}

	protected void executeRefactoring(Refactoring refactoring, boolean withUserInput) throws CoreException {
		RefactoringContext context = refactoring instanceof CRefactoring2 ?
				new CRefactoringContext((CRefactoring2) refactoring) :
				new RefactoringContext(refactoring);
		executeRefactoring(refactoring, context, withUserInput);
	}

	protected void executeRefactoring(Refactoring refactoring, RefactoringContext context,
			boolean withUserInput) throws CoreException {
		try {
			RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
			
			if (fatalError) {
				assertConditionsFatalError(checkInitialConditions);
				return;
			}
			if (initialErrors != 0) {
				assertConditionsError(checkInitialConditions, initialErrors);
			} else if (initialWarnings != 0) {
				assertConditionsFatalError(checkInitialConditions, initialWarnings);
			} else {
				assertConditionsOk(checkInitialConditions);
			}

			if (withUserInput)
				simulateUserInput();

			RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
			if (finalWarnings > 0) {
				assertConditionsWarning(finalConditions, finalWarnings);
			} else if (finalInfos > 0) {
				assertConditionsInfo(finalConditions, finalInfos);
			} else {
				assertConditionsOk(finalConditions);
			}
			Change change = refactoring.createChange(NULL_PROGRESS_MONITOR);
			change.perform(NULL_PROGRESS_MONITOR);
		} finally {
			if (context != null)
				context.dispose();
		}
	}

	/**
	 * Subclasses can override to simulate user input.
	 */
	protected void simulateUserInput() {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		CTestPlugin.getDefault().getLog().addLogListener(this);
		CCorePlugin.getIndexManager().reindex(cproject);
		boolean joined = CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, NULL_PROGRESS_MONITOR);
		assertTrue(joined);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void initializeConfiguration(Collection<TestSourceFile> files) {
		TestSourceFile configFile = null;

		for (TestSourceFile currentFile : files) {
			if (currentFile.getName().equals(CONFIG_FILE_NAME)) {
				configFile = currentFile;
			}
		}

		Properties refactoringProperties = new Properties();

		try {
			if (configFile != null) {
				refactoringProperties.load(new ByteArrayInputStream(configFile.getSource().getBytes()));
			}
		} catch (IOException e) {
			// Property initialization failed
		}

		initCommonFields(refactoringProperties);
		configureRefactoring(refactoringProperties);
		files.remove(configFile);
	}

	private void initCommonFields(Properties refactoringProperties) {
		fileName = refactoringProperties.getProperty("filename", "A.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void assertConditionsOk(RefactoringStatus conditions) {
		assertTrue(conditions.isOK() ? "OK" : "Error or Warning in Conditions: " + conditions.getEntries()[0].getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
		conditions.isOK());
	}

	protected void assertConditionsWarning(RefactoringStatus conditions, int number) {
		if (number > 0) {
			assertTrue("Warning in Condition expected", conditions.hasWarning()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = conditions.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isWarning()) {
				++count;
			}
		}
		assertEquals(number + " Warnings expected found " + count, count, number); //$NON-NLS-1$
	}

	protected void assertConditionsInfo(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Info in condition expected", status.hasInfo()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isInfo()) {
				++count;
			}
		}
		assertEquals(number + " Infos expected found " + count, number, count); //$NON-NLS-1$
	}

	protected void assertConditionsError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Error in condition expected", status.hasError()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isError()) {
				++count;
			}
		}
		assertEquals(number + " Errors expected found " + count, number, count); //$NON-NLS-1$
	}

	protected void assertConditionsFatalError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Fatal Error in Condition expected", status.hasFatalError()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isFatalError()) {
				++count;
			}
		}
		assertEquals(number + " Fatal Errors expected found " + count, number, count); //$NON-NLS-1$
	}

	protected void assertConditionsFatalError(RefactoringStatus conditions) {
		assertTrue("Fatal Error in Condition expected", conditions.hasFatalError()); //$NON-NLS-1$
	}
}
