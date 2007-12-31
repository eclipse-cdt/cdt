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
package org.eclipse.cdt.ui.wizards;

import java.net.URI;

import org.eclipse.jface.wizard.IWizard;

public interface IWizardWithMemory extends IWizard {
	// returns name of last-created project
	// or null if no projects were created
	public String getLastProjectName(); 

	public URI getLastProjectLocation(); 

}
