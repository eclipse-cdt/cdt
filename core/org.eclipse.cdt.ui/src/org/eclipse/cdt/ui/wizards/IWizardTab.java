package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public interface IWizardTab {
	
	public String getLabel();

	public Image getImage();
	
	public Composite getControl(Composite parent);

	public boolean isValid();
	
	public void setVisible(boolean visible);

	public void doRun(IProject project, IProgressMonitor monitor);
}
