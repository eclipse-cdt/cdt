/*
 * Created on 25-Jul-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class CreateBuildAction extends AbstractMakeBuilderAction implements IWorkbenchWindowActionDelegate {

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
	}

}
