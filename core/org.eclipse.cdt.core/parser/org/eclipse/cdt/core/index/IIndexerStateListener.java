/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

/**
 * An indexer state listener is notified of changes to the state of the indexer.
 * <p>
 * Clients may implement this interface.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * @since 4.0
 */
public interface IIndexerStateListener {
	/**
	 * Notifies this listener that the state of the indexer has changed.
	 * <p>
	 * The supplied event provides the details. This event object is valid only for
	 * the duration of the invocation of this method.
	 * </p>
	 * <p>
	 * Note: This method is called by CDT; it is not intended
	 * to be called directly by clients.
	 *
	 * @param event the indexer state event
	 */
	public void indexChanged(IIndexerStateEvent event);
}
