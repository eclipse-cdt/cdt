/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
