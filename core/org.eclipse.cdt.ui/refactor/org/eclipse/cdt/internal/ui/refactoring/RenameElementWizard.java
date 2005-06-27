/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringMessages;

public class RenameElementWizard extends RenameRefactoringWizard {
	public RenameElementWizard() {
		super(
			RefactoringMessages.getString("RenameTypeWizard.defaultPageTitle"), //$NON-NLS-1$
			RefactoringMessages.getString("RenameTypeWizard.inputPage.description"), //$NON-NLS-1$
			CPluginImages.DESC_WIZBAN_REFACTOR_TYPE,
			ICHelpContextIds.RENAME_TYPE_WIZARD_PAGE);
	}
}
