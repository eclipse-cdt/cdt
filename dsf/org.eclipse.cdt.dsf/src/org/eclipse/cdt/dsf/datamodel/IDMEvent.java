/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
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

/**
 * Base interface for events that signify changes in the data model. The only
 * thing all such events must have in common is that they reference an
 * {@link IDMContext}
 *
 * @param <V>
 *            Data Model context type that is affected by this event.
 *
 * @since 1.0
 */
public interface IDMEvent<V extends IDMContext> {
	V getDMContext();
}
