/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactoringArgument;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRenameProcessor;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRenameRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.rename.TextSearchWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

/**
 * @author markus.schorn@windriver.com
 */
public class RenameTestBase extends RefactoringTests {
	private static final IProgressMonitor NPM = new NullProgressMonitor();

	protected RenameTestBase(String name) {
		super(name);
	}

	protected RenameTestBase() {
	}

	/**
	 * @param element the CElement to rename
	 * @param newName the new name for the element
	 * @return the change produced by refactoring
	 * @throws Exception
	 */
	protected Change getRefactorChanges(IFile file, int offset, String newName) throws Exception {
		CRenameRefactoring refactoring = createRefactoring(file, offset, newName);

		refactoring.getProcessor().lockIndex();
		try {
			RefactoringStatus rs = checkConditions(refactoring);
			if (!rs.hasError()) {
				Change change = refactoring.createChange(new NullProgressMonitor());
				return change;
			}

			fail("Input check on " + newName + " failed. " + rs.getEntryMatchingSeverity(RefactoringStatus.ERROR));
			// rs.getFirstMessage(RefactoringStatus.ERROR) is not the message displayed in
			// the UI for renaming a method to a constructor, the first message which is only
			// a warning is shown in the UI. If you click preview, then the error and the warning
			// is shown.
			return null;
		} finally {
			refactoring.getProcessor().unlockIndex();
		}
	}

	protected CRenameRefactoring createRefactoring(IFile file, int offset, String newName) {
		CRefactoringArgument arg = new CRefactoringArgument(file, offset, 0);
		CRenameProcessor processor = new CRenameProcessor(CRefactory.getInstance(), arg);
		processor.setReplacementText(newName);
		processor.setSelectedOptions(0xFFFF & ~CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
		processor.setExhaustiveSearchScope(TextSearchWrapper.SCOPE_WORKSPACE);
		return new CRenameRefactoring(processor);
	}

	protected String[] getRefactorMessages(IFile file, int offset, String newName) throws Exception {
		String[] result;
		CRenameRefactoring refactoring = createRefactoring(file, offset, newName);
		refactoring.getProcessor().lockIndex();
		try {
			RefactoringStatus rs = checkConditions(refactoring);
			if (!rs.hasWarning()) {
				fail("Input check on " + newName + " passed. There should have been warnings or errors.");
				return null;
			}
			RefactoringStatusEntry[] rse = rs.getEntries();
			result = new String[rse.length];
			for (int i = 0; i < rse.length; i++) {
				RefactoringStatusEntry entry = rse[i];
				result[i] = entry.getMessage();

			}
			return result;
		} finally {
			refactoring.getProcessor().unlockIndex();
		}
	}

	protected RefactoringStatus checkConditions(IFile file, int offset, String newName) throws Exception {
		CRenameRefactoring refactoring = createRefactoring(file, offset, newName);
		refactoring.getProcessor().lockIndex();
		try {
			return checkConditions(refactoring);
		} finally {
			refactoring.getProcessor().unlockIndex();
		}
	}

	private RefactoringStatus checkConditions(CRenameRefactoring refactoring) throws CoreException {
		RefactoringStatus rs = refactoring.checkInitialConditions(new NullProgressMonitor());
		if (!rs.hasError()) {
			rs = refactoring.checkFinalConditions(new NullProgressMonitor());
		}
		return rs;
	}

	protected int getRefactorSeverity(IFile file, int offset, String newName) throws Exception {
		CRenameRefactoring refactoring = createRefactoring(file, offset, newName);
		refactoring.getProcessor().lockIndex();
		try {
			RefactoringStatus rs = checkConditions(refactoring);
			return rs.getSeverity();
		} finally {
			refactoring.getProcessor().unlockIndex();
		}
	}

	protected int countOccurrences(String contents, String lookup) {
		int idx = contents.indexOf(lookup);
		int count = 0;
		while (idx >= 0) {
			count++;
			idx = contents.indexOf(lookup, idx + lookup.length());
		}
		return count;
	}

	protected void waitForIndexer() throws InterruptedException {
		waitForIndexer(cproject);
	}

	@Override
	protected IFile importFile(String fileName, String contents) throws Exception {
		IFile result = super.importFile(fileName, contents);
		waitForIndexer();
		return result;
	}
}
