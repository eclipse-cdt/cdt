/*******************************************************************************
 * Copyright (c) 2017 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IConsole;

public interface IBuildConsoleStreamDecorator {

	/**
	 * Returns the color of this message stream, or <code>null</code> if
	 * default.
	 *
	 * @return the color of this message stream, or <code>null</code>
	 */
	Color getColor();

	/**
	 * Returns the console this stream is connected to.
	 *
	 * @return the console this stream is connected to
	 */
	IConsole getConsole();

}
