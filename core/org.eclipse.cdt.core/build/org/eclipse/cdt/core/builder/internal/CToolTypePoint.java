/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.internal;

import org.eclipse.cdt.core.builder.ICToolTypePoint;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Simple wrapper for the data associated with an instance of
 * a CToolType extension point.
 */
public class CToolTypePoint
	extends ACExtensionPoint
	implements ICToolTypePoint {

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the tool type.
	 */
	public CToolTypePoint(IConfigurationElement element) {
		super(element);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolType#getId()
	 */
	public String getId() {
		return getField(FIELD_ID);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolType#getName()
	 */
	public String getName() {
		return getField(FIELD_NAME);
	}

}
