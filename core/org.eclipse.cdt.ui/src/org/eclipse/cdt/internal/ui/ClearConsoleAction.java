package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;

import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Clears the output of the selected launches
 */
public class ClearConsoleAction extends Action {


	private BuildConsoleView fConsoleView;


	public ClearConsoleAction(BuildConsoleView view) {
		super(CPlugin.getResourceString("ClearOutputAction.label"));
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_CLEAR_CONSOLE);
		fConsoleView= view;
		setToolTipText(CPlugin.getResourceString("ClearOutputAction.tooltip"));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { ICHelpContextIds.CLEAR_CONSOLE_ACTION });
	}
	
	/**
	 * @see Action
	 */
	public void run() {
		fConsoleView.clear();
	}
}
