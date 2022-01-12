/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

/**
 * A service abstraction for remote terminals.
 * 
 * @since 2.0
 */
public interface IRemoteProcessTerminalService extends IRemoteProcess.Service {
	/**
	 * Change the terminal window dimension interactively. Refer to RFC 4254 6.7. Window Dimension Change Message. The character/row
	 * dimensions override the pixel dimensions (when nonzero). Pixel dimensions refer to the drawable area of the window.
	 * 
	 * @param cols
	 *            terminal width in characters
	 * @param rows
	 *            terminal height in characters
	 * @param width
	 *            terminal width in pixels
	 * @param height
	 *            terminal height in pixels
	 */
	void setTerminalSize(int cols, int rows, int width, int height);
}
