/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;

public class CConsole implements IConsole {

	/**
	 * Constructor for CConsole.
	 */
	public CConsole() {
		super();
	}

	/**
	 * @see org.eclipse.cdt.core.resources.IConsole#clear()
	 */
	public void clear() {
		CPlugin.getDefault().getConsole().clear();
	}

	/**
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	public ConsoleOutputStream getOutputStream() {
		return CPlugin.getDefault().getConsole().getOutputStream();
	}
}
