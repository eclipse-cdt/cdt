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

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * This class can be used to display SystemMessages via the Display.async 
 * and sync methods.
 */
public class DisplaySystemMessageAction implements Runnable {

	
	protected SystemMessage message;
	protected int rc;
	
	public DisplaySystemMessageAction(SystemMessage message) {
		this.message = message;
	}
	
	
	/**
	 * @see Runnable#run()
	 */
	public void run() {
		boolean finished = false;
			
		Shell[] shells = Display.getCurrent().getShells();
		for (int loop = 0; loop < shells.length && !finished; loop++) {
			if (shells[loop].isEnabled() && shells[loop].isVisible()) {
				SystemMessageDialog dialog = new SystemMessageDialog(shells[loop], message);
				dialog.open();
				rc = dialog.getButtonPressedId();
				finished = true;
			}
		}
	}
	
	/**
	 * Retrieve the return code from displaying the SystemMessageDialog
	 */
	public int getReturnCode() {
		return rc;
	}

}