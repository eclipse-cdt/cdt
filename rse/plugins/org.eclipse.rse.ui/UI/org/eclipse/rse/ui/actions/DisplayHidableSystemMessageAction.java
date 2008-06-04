/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Berger (IBM) - Initial API and implementation
 * David McKnight (IBM) - [216596] determine whether to show yes/no or just okay
 *******************************************************************************/
package org.eclipse.rse.ui.actions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DisplayHidableSystemMessageAction extends
		DisplaySystemMessageAction
{
	protected IPreferenceStore _store;
	protected String _prefID;
	/**
	 * @since 3.0
	 */
	protected boolean _showYesNo = true;

	public DisplayHidableSystemMessageAction(SystemMessage message, IPreferenceStore prefStore, String prefID)
	{
		super(message);
		_store = prefStore;
		_prefID = prefID;
	}

	/**
	 * @since 3.0
	 */
	public DisplayHidableSystemMessageAction(SystemMessage message, IPreferenceStore prefStore, String prefID, boolean showYesNo)
	{
		super(message);
		_store = prefStore;
		_prefID = prefID;
		_showYesNo = showYesNo;
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
				dialog.setNoShowAgainOption(true, _store, _prefID, false);
				dialog.openQuestionNoException(_showYesNo);
				rc = dialog.getButtonPressedId();
				finished = true;
			}
		}
	}
}
