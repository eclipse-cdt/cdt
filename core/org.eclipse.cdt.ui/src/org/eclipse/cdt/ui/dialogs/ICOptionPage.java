package org.eclipse.cdt.ui.dialogs;
/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogPage;

public interface ICOptionPage extends IDialogPage {

	public void setContainer(ICOptionContainer container);
		
	public boolean isValid();
	
	public void performApply(IProgressMonitor monitor) throws CoreException;

	public void performDefaults();

}
