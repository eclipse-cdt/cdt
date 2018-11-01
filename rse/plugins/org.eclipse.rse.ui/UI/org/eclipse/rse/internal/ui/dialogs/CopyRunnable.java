/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Rupen Mardirossian (IBM) - [210693] Created class to run SystemCopyDialog for enhancement defect.  
 ********************************************************************************/

package org.eclipse.rse.internal.ui.dialogs;

import java.util.List;

public class CopyRunnable implements Runnable
{	
	private boolean _ok;
	private List _existingNames;
	private SystemCopyDialog dlg;
	
	public CopyRunnable(List existing)
	{
		_existingNames = existing;
	}
	
	public void run() {
		dlg = new SystemCopyDialog(null, _existingNames);
		dlg.open();
		if (!dlg.wasCancelled())
			_ok = true;
		else
			_ok = false;
	}
	public boolean getOk()
	{
		return _ok;
	}
}
