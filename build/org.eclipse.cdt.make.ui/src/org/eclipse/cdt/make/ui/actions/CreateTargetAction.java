/*
 * Created on 25-Jul-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

public class CreateTargetAction extends AbstractTargetAction {

	public void run(IAction action) {
		if ( getSelectedContainer() != null ) {
			MakeTargetDialog dialog;
			try {
				dialog = new MakeTargetDialog(getShell(), getSelectedContainer());
				dialog.open();
			} catch (CoreException e) {
			}
		}
	}
}
