/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

/**
 * @author Emanuel Graf
 * 
 */
public abstract class RefactoringTest extends RefactoringBaseTest {

	private static final String CONFIG_FILE_NAME = ".config"; //$NON-NLS-1$

	protected String fileName;
	
	public RefactoringTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
		initializeConfiguration(files);
		for (TestSourceFile file : files) {
			fileMap.put(file.getName(), file);
		}
	}

	protected abstract void configureRefactoring(Properties refactoringProperties);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		CTestPlugin.getDefault().getLog().addLogListener(this);
		CCorePlugin.getIndexManager().reindex(cproject);
		boolean joined = CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, NULL_PROGRESS_MONITOR);
		assertTrue(joined);
	}

	private void initializeConfiguration(Vector<TestSourceFile> files) {
		TestSourceFile configFile = null;

		for (TestSourceFile currentFile : files) {
			if (currentFile.getName().equals(CONFIG_FILE_NAME)) {
				configFile = currentFile;
			}
		}

		Properties refactoringProperties = new Properties();

		try {
			if(configFile != null) {
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
		assertTrue(conditions.isOK() ? "OK" : "Error or Warning in Conditions: " + conditions.getEntries()[0].getMessage() //$NON-NLS-1$ //$NON-NLS-2$
		, conditions.isOK());
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
			assertTrue("Info in Condition expected", status.hasInfo()); //$NON-NLS-1$
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
			assertTrue("Error in Condition expected", status.hasError()); //$NON-NLS-1$
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
