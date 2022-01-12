/*******************************************************************************
 * Copyright (c) 2012, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;

import java.util.Map;

import org.eclipse.ui.IMemento;

/**
 * Terminal properties memento handler.
 */
public interface IMementoHandler {

	/**
	 * Saves the terminal properties in the given memento.
	 *
	 * @param memento The memento. Must not be <code>null</code>.
	 * @param properties The map containing the terminal properties to save. Must not be <code>null</code>.
	 */
	public void saveState(IMemento memento, Map<String, Object> properties);

	/**
	 * Restore the terminal properties from the given memento.
	 *
	 * @param memento The memento. Must not be <code>null</code>.
	 * @param properties The map receiving the restored terminal properties. Must not be <code>null</code>.
	 */
	public void restoreState(IMemento memento, Map<String, Object> properties);
}
