/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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

import java.net.URI;

import org.eclipse.jface.wizard.IWizard;

public interface IWizardWithMemory extends IWizard {
	// returns name of last-created project
	// or null if no projects were created
	public String getLastProjectName();

	public URI getLastProjectLocation();

}
