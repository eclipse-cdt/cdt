/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;

/**
 * Simple IStatus implementation to avoid using internal classes.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CDTStatusInfo implements IStatus {
	private String text;
	private int code;

	public CDTStatusInfo() {
		this(OK, null);
	}

	public CDTStatusInfo(int _code, String _text) {
		text = _text;
		code = _code;
	}

	@Override
	public IStatus[] getChildren() {
		return new IStatus[0];
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public Throwable getException() {
		return null;
	}

	@Override
	public String getMessage() {
		return text;
	}

	@Override
	public String getPlugin() {
		return CUIPlugin.PLUGIN_ID;
	}

	@Override
	public int getSeverity() {
		return code;
	}

	@Override
	public boolean isMultiStatus() {
		return false;
	}

	@Override
	public boolean isOK() {
		return (code == OK);
	}

	@Override
	public boolean matches(int mask) {
		return (code & mask) != 0;
	}
}
