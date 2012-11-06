/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Michael Scharf (Wind River) - [172483] switch between connections
 *                               (Adapted from org.eclipse.ui.internal.console.ConsoleDropDownAction)
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.view.ITerminalViewConnection;
import org.eclipse.tm.internal.terminal.view.ITerminalViewConnectionManager;
import org.eclipse.tm.internal.terminal.view.ImageConsts;
import org.eclipse.tm.internal.terminal.view.TerminalViewPlugin;
import org.eclipse.tm.internal.terminal.view.ITerminalViewConnectionManager.ITerminalViewConnectionListener;

/**
 * Drop down action in the console to select the console to display.
 */
public class TerminalActionSelectionDropDown extends Action implements IMenuCreator, ITerminalViewConnectionListener {
	private ITerminalViewConnectionManager fConnections;
	private Menu fMenu;
	public TerminalActionSelectionDropDown(ITerminalViewConnectionManager view) {
		fConnections= view;
		setText(ActionMessages.ConsoleDropDownAction_0); 
		setToolTipText(ActionMessages.ConsoleDropDownAction_1); 
		setImageDescriptor(TerminalViewPlugin.getDefault().getImageRegistry().getDescriptor(ImageConsts.IMAGE_TERMINAL_VIEW));
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_DISPLAY_CONSOLE_ACTION);
		setMenuCreator(this);
		fConnections.addListener(this);
		connectionsChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fConnections.removeListener(this);
		fConnections= null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		
		fMenu= new Menu(parent);
		ITerminalViewConnection[] consoles= fConnections.getConnections();
		ITerminalViewConnection active = fConnections.getActiveConnection();
		for (int i = 0; i < consoles.length; i++) {
			ITerminalViewConnection console = consoles[i];
			Action action = new ShowTerminalConnectionAction(fConnections, console);
			action.setChecked(console.equals(active));
			addActionToMenu(fMenu, action, i + 1);
		}
		return fMenu;
	}
	
	private void addActionToMenu(Menu parent, Action action, int accelerator) {
	    if (accelerator < 10) {
		    StringBuffer label= new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fConnections.swapConnection();
	}

	public void connectionsChanged() {
		setEnabled(fConnections.size() > 1);
	}
}
