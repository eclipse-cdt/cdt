package org.eclipse.cdt.ui;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface ICOptionPage {

	public void setContainer(ICOptionContainer container);
		
	public String getLabel();

	public Image getImage();
	
	public void createControl(Composite parent);

	public Control getControl();

	public boolean isValid();
	
	public String getMessage();

	public String getErrorMessage();
	
	public void setVisible(boolean visible);

	public void performApply(IProgressMonitor monitor) throws CoreException;

	public void performDefaults();

}
