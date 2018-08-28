/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core.interfaces;

/**
 * Listener to implement and to register to get notified about
 * terminal tabs events, like the disposal of a terminal tab.
 */
public interface ITerminalTabListener {

	/**
	 * Invoked once a terminal tab got disposed. The source object is
	 * the disposed tab item and data is the custom data object associated
	 * with the disposed tab item.
	 *
	 * @param source The disposed tab item. Must not be <code>null</code>.
	 * @param data The custom data object associated with the disposed tab item or <code>null</code>.
	 */
	public void terminalTabDisposed(Object source, Object data);
}
