/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
 * An event to signal the initial availability of the data model.
 *
 * @since 2.0
 */
public class DataModelInitializedEvent extends AbstractDMEvent<IDMContext> {

	/**
	 * Create an event for the given data model context.
	 * The context should represent the root of the data model hierarchy.
	 *
	 * <p>
	 * Clients may instantiate and subclass.
	 * </p>
	 *
	 * @param context  the data model context
	 */
	public DataModelInitializedEvent(IDMContext context) {
		super(context);
	}

}
