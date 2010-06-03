/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import java.util.Collection;

/**
 * A source tag provider provides access to source tags.
 */
public interface ISourceTagProvider {

	/**
	 * Add a source tag listener to receive source tag changed notifications.
	 * @param listener
	 */
	public void addSourceTagListener(ISourceTagListener listener);

	/**
	 * Remove a source tag listener to stop receiving source tag changed notifications.
	 * @param listener
	 */
	public void removeSourceTagListener(ISourceTagListener listener);

	/**
	 * Retrieves all symbols of the current file.
	 */
	public void getSourceTags(Collection<ISourceTag> target);

	/**
	 * Get the time stamp of the current symbol content.
	 * @return the modification time of the source file or 0L if no symbols available.
	 */
	public long getSnapshotTime();

	/**
	 * Retrieves the active code positions of the current file. Null if the
	 * information cannot be obtained.
	 */
	public int[] getActiveCodePositions();
	
}
