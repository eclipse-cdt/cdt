/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.core.resources.IProject;

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
