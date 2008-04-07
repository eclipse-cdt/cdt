/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [189150] setSelection(null) added to clear()
 * Kevin Doyle (IBM) - [194899] Remove All should do a full reset of the scratchpad
 *******************************************************************************/


package org.eclipse.rse.internal.ui.view.scratchpad;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;



public class ClearAction extends BrowseAction
{

    public ClearAction(SystemScratchpadView view)
	{
		super(view, SystemResources.ACTION_CLEAR_ALL_LABEL,
		        RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CLEAR_ALL_ID));

		setToolTipText(SystemResources.ACTION_CLEAR_ALL_TOOLTIP);
		// TODO DKM - get help for this!
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CLEAR_CONSOLE_ACTION);
	}

	public void checkEnabledState()
	{
	    setEnabled(_scratchPad.hasChildren());
	}

	public void run()
	{
		clear();
	}

	// clear contents of the current command viewer
	private void clear()
	{
		// Reset the SystemScratchpad
		_scratchPad.clearChildren();
		// Set the input of the view to SystemScratchpad if it has changed
		if (_view.getInput() != _scratchPad) {
			_view.setInput(_scratchPad);
		}
		// Refresh the Scratchpad and update action states
	    RSECorePlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(_scratchPad, ISystemResourceChangeEvents.EVENT_REFRESH, _scratchPad));
	    _view.setSelection(null);
	}
}
