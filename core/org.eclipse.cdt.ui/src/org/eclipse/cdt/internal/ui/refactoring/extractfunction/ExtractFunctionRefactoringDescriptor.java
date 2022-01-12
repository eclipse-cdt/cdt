/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Emanuel Graf IFS
 */
public class ExtractFunctionRefactoringDescriptor extends CRefactoringDescriptor {
	protected static final String NAME = "name"; //$NON-NLS-1$
	protected static final String VISIBILITY = "visibility"; //$NON-NLS-1$
	protected static final String REPLACE_DUPLICATES = "replaceDuplicates"; //$NON-NLS-1$

	public ExtractFunctionRefactoringDescriptor(String project, String description, String comment,
			Map<String, String> arguments) {
		super(ExtractFunctionRefactoring.ID, project, description, comment, RefactoringDescriptor.MULTI_CHANGE,
				arguments);
	}

	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		ISelection selection = getSelection();
		ICProject project = getCProject();
		ExtractFunctionRefactoring refactoring = new ExtractFunctionRefactoring(getTranslationUnit(), selection,
				project);
		ExtractFunctionInformation info = refactoring.getRefactoringInfo();
		info.setMethodName(arguments.get(NAME));
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(arguments.get(VISIBILITY)));
		info.setReplaceDuplicates(Boolean.parseBoolean(arguments.get(REPLACE_DUPLICATES)));
		return refactoring;
	}
}
