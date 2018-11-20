/*******************************************************************************
 * Copyright (c) 2009, 2016 Institute for Software, HSR Hochschule fuer Technik
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
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

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
public class ExtractConstantRefactoringDescriptor extends CRefactoringDescriptor {
	protected static final String NAME = "name"; //$NON-NLS-1$
	protected static final String VISIBILITY = "visibility"; //$NON-NLS-1$
	protected static final String REPLACE_ALL = "replaceAll"; //$NON-NLS-1$

	protected ExtractConstantRefactoringDescriptor(String project, String description, String comment,
			Map<String, String> arguments) {
		super(ExtractConstantRefactoring.ID, project, description, comment, RefactoringDescriptor.MULTI_CHANGE,
				arguments);
	}

	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		ISelection selection = getSelection();
		ICProject project = getCProject();
		ExtractConstantRefactoring refactoring = new ExtractConstantRefactoring(getTranslationUnit(), selection,
				project);
		ExtractConstantInfo info = refactoring.getRefactoringInfo();
		info.setName(arguments.get(NAME));
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(arguments.get(VISIBILITY)));
		info.setReplaceAllLiterals(Boolean.parseBoolean(arguments.get(REPLACE_ALL)));
		return refactoring;
	}
}
