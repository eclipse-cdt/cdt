/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 ********************************************************************************/

package org.eclipse.rse.files.ui.widgets;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.widgets.SystemHostCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/**
 * A connection combo widget for universal connections. Includes remote Linux, Windows and Unix connections,
 * and Local connections.
 */
public class SystemRemoteConnectionCombo extends SystemHostCombo {
	
	private static final String[] SYSTEM_TYPE_IDS = {
		IRSESystemType.SYSTEMTYPE_LINUX_ID,
		IRSESystemType.SYSTEMTYPE_LOCAL_ID,
		IRSESystemType.SYSTEMTYPE_UNIX_ID,
		IRSESystemType.SYSTEMTYPE_WINDOWS_ID
	};

	/**
	 * Constructor when you want to set the style.
	 * @param parent Parent composite.
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL.
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite.
	 */
	public SystemRemoteConnectionCombo(Composite parent, int style, IHost defaultConnection, boolean showNewButton) {
		super(parent, style, SystemWidgetHelpers.getValidSystemTypes(SYSTEM_TYPE_IDS), defaultConnection, showNewButton);
	}
	
	/**
	 * Constructor when you don't care about the style. Defaults to SWT.NULL.
	 * @param parent Parent composite.
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite.
	 */
	public SystemRemoteConnectionCombo(Composite parent, IHost defaultConnection, boolean showNewButton) {
		this(parent, SWT.NULL, defaultConnection, showNewButton);
	}

	/**
	 * Constructor when you don't care about the style or default connection, and do want to show the New button.
	 * This is the most common case.
	 * @param parent Parent composite
	 */
	public SystemRemoteConnectionCombo(Composite parent) {
		this(parent, null, true);
	}
}