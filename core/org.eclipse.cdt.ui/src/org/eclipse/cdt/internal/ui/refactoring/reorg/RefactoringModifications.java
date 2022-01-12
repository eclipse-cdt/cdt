/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.reorg;

import org.eclipse.cdt.internal.corext.refactoring.participants.ResourceModifications;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

public abstract class RefactoringModifications {
	private ResourceModifications fResourceModifications;

	public RefactoringModifications() {
		fResourceModifications = new ResourceModifications();
	}

	public ResourceModifications getResourceModifications() {
		return fResourceModifications;
	}

	public abstract RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor owner,
			String[] natures, SharableParticipants shared);

	public abstract void buildDelta(IResourceChangeDescriptionFactory builder);

	/**
	 * Implementors add all resources that need a validate edit.
	 *
	 * @param checker the validate edit checker
	 */
	public void buildValidateEdits(ValidateEditChecker checker) {
		// Default implementation does nothing.
	}
}
