/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
