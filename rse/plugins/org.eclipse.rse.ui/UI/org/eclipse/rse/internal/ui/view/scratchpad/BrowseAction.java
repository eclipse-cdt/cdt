/********************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split SystemRegistryUI from SystemRegistry implementation
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view.scratchpad;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.ui.internal.model.SystemRegistryUI;
import org.eclipse.rse.ui.internal.model.SystemScratchpad;



class BrowseAction extends Action
{
   protected SystemScratchpadView _view;
   	protected SystemScratchpad _scratchPad;
	public BrowseAction(SystemScratchpadView view, String label, ImageDescriptor des)
	{
		super(label, des);
        _view = view;
		setImageDescriptor(des);
		setToolTipText(label);
		_scratchPad = SystemRegistryUI.getInstance().getSystemScratchPad();
	}

	public void checkEnabledState()
	{
		if (_view.getInput() != null)
		{
			setEnabled(true);
		}
		else
		{
			setEnabled(false);
		}
	}

	public void run()
	{
	}
}