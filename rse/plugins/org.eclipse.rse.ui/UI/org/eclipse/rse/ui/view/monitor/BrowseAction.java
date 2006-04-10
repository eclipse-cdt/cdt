/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view.monitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

class BrowseAction extends Action
{
	protected final SystemMonitorViewPart	part;

	public BrowseAction(SystemMonitorViewPart part)
	{
		super();
		this.part = part;
	}
	
	public BrowseAction(SystemMonitorViewPart part, String label, ImageDescriptor des)
	{
		super(label, des);
		this.part = part;

		setToolTipText(label);
	}

	public void checkEnabledState()
	{
		if (this.part._folder != null && this.part._folder.getInput() != null)
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