/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.managedbuilder.ui.newui.Messages;
import org.eclipse.core.resources.IProject;

public class CDTProjectWizard extends NewModelProjectWizard {

	public CDTProjectWizard() {
		super(Messages.getString("NewModelProjectWizard.0"), Messages.getString("NewModelProjectWizard.1")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected String[] getNatures() {
		return new String[0];
	}

	protected IProject continueCreation(IProject prj) {
		return prj;
	}

}
