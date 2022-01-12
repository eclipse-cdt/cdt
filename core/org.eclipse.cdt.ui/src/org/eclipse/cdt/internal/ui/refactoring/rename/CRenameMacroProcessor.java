/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Rename processor that sets up the input page for renaming a global entity.
 */
public class CRenameMacroProcessor extends CRenameGlobalProcessor {

	public CRenameMacroProcessor(CRenameProcessor processor, String name) {
		super(processor, name);
		setAvailableOptions(CRefactory.OPTION_IN_CODE_REFERENCES | CRefactory.OPTION_IN_COMMENT
				| CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE | CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
	}

	@Override
	protected int getAcceptedLocations(int selectedOptions) {
		return selectedOptions | CRefactory.OPTION_IN_MACRO_DEFINITION;
	}

	@Override
	protected void analyzeTextMatches(IBinding[] renameBindings, Collection<CRefactoringMatch> matches,
			IProgressMonitor monitor, RefactoringStatus status) {
		for (CRefactoringMatch m : matches) {
			if ((m.getLocation() & CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE) != 0 ||
			// Occurrences in code are reliable only when exhaustive file search is not used.
			// TODO(sprigogin): Use index matches to endorse matches obtained from the file search.
					(getSelectedOptions() & CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH) == 0
							&& (m.getLocation() & CRefactory.OPTION_IN_CODE_REFERENCES) != 0) {
				m.setASTInformation(CRefactoringMatch.AST_REFERENCE);
			}
		}
		super.analyzeTextMatches(renameBindings, matches, monitor, status);
	}
}
