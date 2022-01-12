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
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;

/**
 * The wizard to create new MBS C Project.
 */
public class CProjectWizard extends CDTCommonProjectWizard {

	public CProjectWizard() {
		super(Messages.NewModelProjectWizard_4, Messages.NewModelProjectWizard_5);
	}

	@Override
	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID };
	}

	@Override
	protected IProject continueCreation(IProject prj) {
		SubMonitor subMonitor = SubMonitor.convert(continueCreationMonitor, Messages.CProjectWizard_0, 1);
		try {
			CProjectNature.addCNature(prj, subMonitor.split(1));
		} catch (CoreException e) {
		}
		return prj;
	}

	@Override
	public String[] getContentTypeIDs() {
		return new String[] { CCorePlugin.CONTENT_TYPE_CSOURCE, CCorePlugin.CONTENT_TYPE_CHEADER };
	}

}
