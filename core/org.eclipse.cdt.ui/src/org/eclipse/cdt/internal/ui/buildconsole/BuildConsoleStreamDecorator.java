/*******************************************************************************
 * Copyright (c) 2009, 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IConsole;

public class BuildConsoleStreamDecorator implements IBuildConsoleStreamDecorator {
	private BuildConsole fConsole = null;

	private Color fColor = null;

	/**
	 * Constructs a new stream connected to the given console.
	 *
	 */
	public BuildConsoleStreamDecorator() {
	}

	public void setConsole(BuildConsole console) {
		fConsole = console;
	}

	/**
	 * Sets the color of this message stream
	 *
	 * @param color
	 *            color of this message stream, possibly <code>null</code>
	 */
	public void setColor(Color color) {
		Color old = fColor;
		fColor = color;
		if (fConsole != null) {
			fConsole.firePropertyChange(this, BuildConsole.P_STREAM_COLOR, old, color);
		}
	}

	@Override
	public Color getColor() {
		return fColor;
	}

	@Override
	public IConsole getConsole() {
		return fConsole;
	}
}
