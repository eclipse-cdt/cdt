/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
 
/**
 * DisplayDialogAction can be used to display a JFace Dialog when
 * not running on the UI thread and no shell is availble.  For example:
 * <code>
 * Display.getDefault().syncExec(new DisplayDialogAction(myDialog));
 * </code>
 */
public class DisplayDialogAction implements Runnable {


	private Dialog _dialog;
	
	/**
	 * Constructor for DisplayDialogAction.
	 * 
	 * @param dialog The dialog to be displayed.
	 */
	public DisplayDialogAction(Dialog dialog) {
		_dialog = dialog;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean finished = false;
			
		Shell[] shells = Display.getCurrent().getShells();
		for (int loop = 0; loop < shells.length && !finished; loop++) {
			if (shells[loop].isEnabled()) 
			{
				_dialog.open();
				finished = true;
			}
		}
	}

}