/*******************************************************************************
 * Copyright (c) 2005, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
*******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.view.ITerminalView;
import org.eclipse.tm.internal.terminal.view.ImageConsts;

/**
 * UNDER CONSTRUCTION 
 *
 * @author Fran Litterio <francis.litterio@windriver.com>
 */
public class TerminalActionNewTerminal extends TerminalAction implements IMenuCreator {
	private Menu fMenu;
    public TerminalActionNewTerminal(ITerminalView target)
    {
        super(target, TerminalActionNewTerminal.class.getName());

        setupAction(ActionMessages.NEW_TERMINAL_CONNECTION,
                    ActionMessages.NEW_TERMINAL_CONNECTION,
                    ImageConsts.IMAGE_NEW_TERMINAL,
                    ImageConsts.IMAGE_NEW_TERMINAL,
                    ImageConsts.IMAGE_NEW_TERMINAL,
                    true);
		setMenuCreator(this);
    }
	public void run() {
		fTarget.onTerminalNewTerminal();
	}
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
	}
	public Menu getMenu(Control parent) {
		if(fMenu==null) {
			fMenu= new Menu(parent);
			addActionToMenu(fMenu,
			new Action(ActionMessages.NEW_TERMINAL_CONNECTION) {
				public void run() {
					fTarget.onTerminalNewTerminal();
				}
				
			});
			addActionToMenu(fMenu,
					new Action(ActionMessages.NEW_TERMINAL_VIEW) {
						public void run() {
							fTarget.onTerminalNewView();
						}
						
					});
		}
		return fMenu;
	}
	protected void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	public Menu getMenu(Menu parent) {
		return null;
	}
}
