/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.core.resources.IResource;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard;

public class CResourceRenameRefactoringWizard extends RenameResourceWizard {

	public CResourceRenameRefactoringWizard(IResource resource) {
		super(resource);
	}

	@Override
	protected void addUserInputPages() {
		RenameResourceProcessor processor = getRefactoring().getAdapter(RenameResourceProcessor.class);
		addPage(new CResourceRenameRefactoringInputPage(processor));
	}

}
