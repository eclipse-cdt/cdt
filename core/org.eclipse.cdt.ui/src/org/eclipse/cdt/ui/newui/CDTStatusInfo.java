/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Simple IStatus implementation to avoid using internal classes.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CDTStatusInfo implements IStatus {
	private String text;
	private int code;

	public CDTStatusInfo() {this(OK, null); }
	public CDTStatusInfo(int _code, String _text) {
		text= _text;
		code= _code;
	}
	@Override
	public IStatus[] getChildren() { return new IStatus[0];}
	@Override
	public int getCode() { return code; }
	@Override
	public Throwable getException() { return null; }
	@Override
	public String getMessage() { return text; }
	@Override
	public String getPlugin() { return CUIPlugin.PLUGIN_ID; }
	@Override
	public int getSeverity() { return code; }
	@Override
	public boolean isMultiStatus() { return false; }
	@Override
	public boolean isOK() { return (code == OK); }
	@Override
	public boolean matches(int mask) { return (code & mask) != 0; }
}
