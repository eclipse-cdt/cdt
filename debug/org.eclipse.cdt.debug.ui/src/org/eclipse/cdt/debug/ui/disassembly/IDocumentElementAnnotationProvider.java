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
 * Provides context sensitive annotations for source elements.
 *
 * This interface is experimental.
 */
public interface IDocumentElementAnnotationProvider {

	/**
	 * Updates the annotations of the specified source elements.
	 *
	 * @param updates each update specifies the element and context
	 * for which annotations are requested and stores the results
	 */
	public void update(IDocumentElementAnnotationUpdate[] updates);
}
