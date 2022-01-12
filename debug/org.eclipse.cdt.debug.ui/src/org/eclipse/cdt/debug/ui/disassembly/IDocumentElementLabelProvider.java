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
 * Provides context sensitive labels for source elements.
 *
 * This interface is experimental.
 */
public interface IDocumentElementLabelProvider {

	/**
	 * Updates the specified labels.
	 *
	 * @param updates each update specifies the element and context
	 * for which a label is requested and stores the label attributes
	 */
	public void update(IDocumentElementLabelUpdate[] updates);
}
