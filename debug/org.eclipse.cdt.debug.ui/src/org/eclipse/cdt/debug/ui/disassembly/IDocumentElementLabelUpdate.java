/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.disassembly;

/**
 * A label update request for a source viewer element.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 *
 *  This interface is experimental.
 */
public interface IDocumentElementLabelUpdate extends IDocumentUpdate {

	/**
	 * Sets the text of the label of the specified attribute.
	 *
	 * @param attribute the attribute name
	 * @param text the label text to set
	 */
	public void setLabel(String attribute, String text);
}
