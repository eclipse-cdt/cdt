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
package org.eclipse.cdt.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractCOptionPage implements ICOptionPage {

	private String fErrorMessage;
	private String fMessage;
	private boolean bIsValid = true;
	private Control fControl;
	private ICOptionContainer fContainer;

	public void setContainer(ICOptionContainer container) {
		fContainer = container;
	}
	
	protected ICOptionContainer getContainer() {
		return fContainer;
	}

	public Image getImage() {
		return null;
	}

	public abstract void createControl(Composite parent);

	public abstract String getLabel();

	public Control getControl() {
		return fControl;
	}

	protected void setControl(Control control) {
		fControl = control;
	}
	
	protected void setValid(boolean isValid) {
		bIsValid = isValid;
	}
	
	public boolean isValid() {
		return bIsValid;
	}

	public String getMessage() {
		return fMessage;
	}
	
	protected void setMessage(String message) {
		fMessage = message;
	}

	public String getErrorMessage() {
		return fErrorMessage;
	}

	protected void setErrorMessage(String message) {
		fErrorMessage = message;
	}
	
	public void setVisible(boolean visible) {
	}
	
	public abstract void performApply(IProgressMonitor monitor) throws CoreException;

	public void performDefaults() {
	}

}
