/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

/**
 * A listener to build console events.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see IBuildConsoleEvent
 */
public interface IBuildConsoleListener {
	void consoleChange(IBuildConsoleEvent event);
}
