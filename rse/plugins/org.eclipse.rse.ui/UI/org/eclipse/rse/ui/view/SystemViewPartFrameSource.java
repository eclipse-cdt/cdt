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

package org.eclipse.rse.ui.view;

import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

/**
 * Enables frameset for the Remote Systems Explorer view part
 */
class SystemViewPartFrameSource extends TreeViewerFrameSource 
{

	// has same interface as org.eclipse.ui.views.navigator.NavigatorFrameSource
	
	private SystemViewPart fSystemViewPart;
	
	SystemViewPartFrameSource(SystemViewPart viewPart) 
	{
		super(viewPart.getSystemView());
		fSystemViewPart = viewPart;
	}

	protected TreeFrame createFrame(Object input) 
	{
		TreeFrame frame = super.createFrame(input);
	    frame.setName(fSystemViewPart.getFrameName(input));
		frame.setToolTipText(fSystemViewPart.getFrameToolTipText(input));
		return frame;
	}

	/**
	 * Also updates the title of the Remote Systems view part
	 */
	protected void frameChanged(TreeFrame frame) 
	{
		super.frameChanged(frame);
		fSystemViewPart.updateTitle();
	}

}