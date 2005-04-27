/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Bogdan Gheorghe
 */
public class NullIndexerBlock extends AbstractIndexerPage {


	public void initialize(IProject currentProject) {}

	public void performApply(IProgressMonitor monitor) throws CoreException {}

	public void performDefaults() {}

	public void createControl(Composite parent) {
	    Composite comp = new Composite(parent, SWT.NULL);
        setControl(comp);
	}

	public void loadPreferences() {}

	public void removePreferences() {}

}
