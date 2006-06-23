/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.swt.graphics.Color;


public class BuildConsoleStream {
	private BuildConsole fConsole = null;
	
	private Color fColor = null;
	
	/**
	 * Constructs a new stream connected to the given console.
	 * 
	 */
	public BuildConsoleStream() {
	}

	public void setConsole(BuildConsole console) {
		fConsole = console;
	}

	/**
	 * Sets the color of this message stream
	 * 
	 * @param color color of this message stream, possibly <code>null</code>
	 */
	public void setColor(Color color) {
		Color old = fColor;
		fColor = color;
		if (fConsole != null) {
			fConsole.firePropertyChange(this, BuildConsole.P_STREAM_COLOR, old, color);
		}
	}
	
	/**
	 * Returns the color of this message stream, or <code>null</code>
	 * if default.
	 * 
	 * @return the color of this message stream, or <code>null</code>
	 */
	public Color getColor() {
		return fColor;
	}
	
	/**
	 * Returns the console this stream is connected to.
	 * 
	 * @return the console this stream is connected to
	 */
	public BuildConsole getConsole() {
		return fConsole;
	}
}
