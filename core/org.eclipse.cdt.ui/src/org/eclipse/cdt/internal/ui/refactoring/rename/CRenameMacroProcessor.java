/*******************************************************************************
 * Copyright (c) 2005, 2010 Wind River Systems, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google) 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.IBinding;


/**
 * Rename processor that sets up the input page for renaming a global entity.
 */
public class CRenameMacroProcessor extends CRenameGlobalProcessor {

    public CRenameMacroProcessor(CRenameProcessor processor, String name) {
        super(processor, name);
        setAvailableOptions(
        		CRefactory.OPTION_IN_CODE_REFERENCES |
                CRefactory.OPTION_IN_COMMENT | 
                CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE |
                CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
    }

    @Override
	protected int getAcceptedLocations(int selectedOptions) {
        return selectedOptions | CRefactory.OPTION_IN_MACRO_DEFINITION;
    }

	@Override
	protected void analyzeTextMatches(IBinding[] renameBindings, Collection<CRefactoringMatch> matches,
			IProgressMonitor monitor, RefactoringStatus status) {
        for (CRefactoringMatch m : matches) {
            if ((m.getLocation() & CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE) != 0) {
                m.setASTInformation(CRefactoringMatch.AST_REFERENCE);
            }
        }
        super.analyzeTextMatches(renameBindings, matches, monitor, status);
    }
}
