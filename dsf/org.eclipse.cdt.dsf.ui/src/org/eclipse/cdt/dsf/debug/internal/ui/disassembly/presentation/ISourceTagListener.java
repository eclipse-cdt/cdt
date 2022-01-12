/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

/**
 * ISourceTagListener
 */
public interface ISourceTagListener {

	/**
	 * Notifies this listener that the source tags have changed.
	 * @param provider the provider generating this event.
	 */
	void sourceTagsChanged(ISourceTagProvider provider);

}
