/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Michael Scharf (Wind River) - [172483] Adapted from org.eclipse.ui.console/ShowConsoleAction
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.tm.internal.terminal.view.ITerminalViewConnection;
import org.eclipse.tm.internal.terminal.view.ITerminalViewConnectionManager;

/**
 * Shows a specific connection in the terminal view
 */
public class ShowTerminalConnectionAction extends Action {
	
	private final ITerminalViewConnection fConnection;
	private final ITerminalViewConnectionManager fConnectionManager;

	/**
	 * Constructs an action to display the given terminal.
	 * 
	 * @param manager the terminal multi-view in which the given terminal connection is contained
	 * @param connection the terminal view connection
	 */
	public ShowTerminalConnectionAction(ITerminalViewConnectionManager manager, ITerminalViewConnection connection) {
		super(quoteName(buildName(manager,connection)), AS_RADIO_BUTTON);
		fConnection = connection;
		fConnectionManager = manager;
		setImageDescriptor(connection.getImageDescriptor());
	}
	/**
	 * the tab at the end quotes '@' chars?!? see 
	 * {@link #setText(String)}
	 * @param name
	 * @return a quoted sting
	 */
	private static String quoteName(String name) {
		return name+"\t"; //$NON-NLS-1$
	}
	/**
	 * Builds the name. It uses the summary. If the connections have different
	 * partNames (the names showed in the view title) then this name is prefixed.
	 * @param m the connection manager
	 * @param connection the connection for which the name should me extracted
	 * @return The name to be displayed
	 */
	private static String buildName(ITerminalViewConnectionManager m,ITerminalViewConnection connection) {
		String name = connection.getFullSummary();
		if(!checkIfAllPartNamesTheSame(m))
			name=connection.getPartName()+" - " +name; //$NON-NLS-1$
		return name;
	}
	/**
	 * @param m the connection manager
	 * @return true if the part names of all connections are the same
	 */
	private static boolean checkIfAllPartNamesTheSame(ITerminalViewConnectionManager m) {
		ITerminalViewConnection[] connections = m.getConnections();
		if(connections.length>1) {
			String partName=connections[0].getPartName();
			for (int i = 1; i < connections.length; i++) {
				if(!partName.equals(connections[i].getPartName())) {
					return false;
				}			
			}
			
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fConnectionManager.setActiveConnection(fConnection);
	}
}
