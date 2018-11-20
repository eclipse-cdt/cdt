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
 * Provides a content for a virtual source viewer.
 *
 * This interface is experimental.
 */
public interface IDocumentElementContentProvider {

	/**
	 * Updates the base element of the source viewer.
	 * This method is called when the viewer's input is changed.
	 *
	 * @param update the new input.
	 */
	public void updateInput(IDocumentBaseChangeUpdate update);

	/**
	 * Updates the source content as requested by the given update.
	 * This method is called when the viewer requires to update it's content.
	 *
	 * @param update specifies the lines to update and stores result
	 */
	public void updateContent(IDocumentElementContentUpdate update);
}
