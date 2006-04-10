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

package org.eclipse.rse.ui.widgets;
/**
 * @author coulthar
 *
 * This interfaces defines the constants for the modes and states
 *  of the {@link org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine} 
 *  class.
 */
public interface ISystemEditPaneStates 
{
	
	/**
	 * MODE - UNSET. Nothing is selected so overall edit pane is hidden/disabled
	 */
	public static final int MODE_UNSET = 2;
	/**
	 * MODE - NEW. The user is creating a "new" thing
	 */
	public static final int MODE_NEW = 4;
	/**
	 * MODE - EDIT. The user is editing an existing thing
	 */
	public static final int MODE_EDIT = 8;

	/**
	 * STATE - NO CHANGES. No changes have been made by the user
	 */
	public static final int STATE_INITIAL = 128;
	/**
	 * STATE - CHANGES PENDING. User has made changes but they haven't been applied yet
	 */
	public static final int STATE_PENDING = 256;
	/**
	 * STATE - CHANGES MADE. User has made changes and applied them. None pending
	 */
	public static final int STATE_APPLIED = 512;
}