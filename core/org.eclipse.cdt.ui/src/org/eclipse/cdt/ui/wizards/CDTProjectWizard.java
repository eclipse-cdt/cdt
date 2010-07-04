/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.core.resources.IProject;

import org.eclipse.cdt.internal.ui.newui.Messages;

public class CDTProjectWizard extends CDTCommonProjectWizard {

	public CDTProjectWizard() {
		super(Messages.NewModelProjectWizard_0, Messages.NewModelProjectWizard_1); 
	}
	
	@Override
	public String[] getNatures() {
		return new String[0];
	}

	@Override
	protected IProject continueCreation(IProject prj) {
		return prj;
	}

}
