package org.eclipse.cdt.internal.ui.buildconsole;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;

import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Clears the output of the selected launches
 */
public class ClearConsoleAction extends Action {


	private BuildConsoleView fConsoleView;


	public ClearConsoleAction(BuildConsoleView view) {
		super(CUIPlugin.getResourceString("ClearOutputAction.label"));
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_CLEAR_CONSOLE);
		fConsoleView= view;
		setToolTipText(CUIPlugin.getResourceString("ClearOutputAction.tooltip"));
		WorkbenchHelp.setHelp(this, ICHelpContextIds.CLEAR_CONSOLE_ACTION);
	}
	
	/**
	 * @see Action
	 */
	public void run() {
		fConsoleView.clear();
	}
}
