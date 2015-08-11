/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

/**
 * Terminal listener allowing to listen to terminal selection changes.
 */
public interface ITerminalListener2 extends ITerminalListener {

	/**
	 * selection has been changed internally e.g. select all
	 * clients might want to react on that
	 * NOTE: this does not include mouse selections
	 * those are handled in separate MouseListeners
	 * TODO should be unified
	 *
	 * @since 4.1
	 */
	void setTerminalSelectionChanged();
}
