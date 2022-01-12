/*******************************************************************************
 * Copyright (c) 2004, 2014 Wind River Systems, Inc. and others.
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
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.core.resources.IFile;

/**
 * A refactoring match initially is a plain text match. In the course of refactoring
 * it will be classified with a location (comment, code, ...) and with the information
 * whether it has been verified via AST or not.
 */
public class CRefactoringMatch {
	public static final int POTENTIAL = 0;
	public static final int AST_REFERENCE = 1;
	public static final int AST_REFERENCE_OTHER = 2;
	public static final int AST_REFEREENCE_CONFLICTING = 3;
	public static final int IN_COMMENT = 4;

	private static String[] LABELS = { RenameMessages.CRefactoringMatch_label_potentialOccurrences,
			RenameMessages.CRefactoringMatch_label_occurrences, "", //$NON-NLS-1$
			RenameMessages.CRefactoringMatch_label_potentialOccurrences,
			RenameMessages.CRefactoringMatch_label_inComment };

	private IFile fFile;
	private int fOffset;
	private int fLength;
	private int fLocation;
	private int fAstInformation;

	public int getAstInformation() {
		return fAstInformation;
	}

	public CRefactoringMatch(IFile file, int offset, int length, int location) {
		fFile = file;
		fOffset = offset;
		fLength = length;
		fLocation = location;
	}

	public int getOffset() {
		return fOffset;
	}

	public void setLocation(int location) {
		fLocation = location;
	}

	public int getLocation() {
		return fLocation;
	}

	public int getLength() {
		return fLength;
	}

	public IFile getFile() {
		return fFile;
	}

	public void setASTInformation(int val) {
		switch (fAstInformation) {
		case AST_REFERENCE:
		case AST_REFERENCE_OTHER:
		case AST_REFEREENCE_CONFLICTING:
			if (val != fAstInformation) {
				fAstInformation = AST_REFEREENCE_CONFLICTING;
			}
			break;
		default:
			fAstInformation = val;
			break;
		}
	}

	public String getLabel() {
		if (fAstInformation == AST_REFERENCE) {
			return LABELS[AST_REFERENCE];
		}
		if (isInComment()) {
			return LABELS[IN_COMMENT];
		}
		return LABELS[POTENTIAL];
	}

	public boolean isInComment() {
		return (fLocation & CRefactory.OPTION_IN_COMMENT) != 0;
	}
}
