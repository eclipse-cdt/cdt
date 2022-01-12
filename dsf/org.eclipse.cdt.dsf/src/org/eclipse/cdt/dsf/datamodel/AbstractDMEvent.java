/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.datamodel;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * Base implementation of the IDMEvent interface. Sub-classes should contain
 * specific information about the event, while this base class only identifies
 * the DM Context that is affected.
 *
 * @since 1.0
 */
@Immutable
abstract public class AbstractDMEvent<V extends IDMContext> implements IDMEvent<V> {

	private final V fModelContext;

	public AbstractDMEvent(V context) {
		fModelContext = context;
	}

	@Override
	public V getDMContext() {
		return fModelContext;
	}

}
