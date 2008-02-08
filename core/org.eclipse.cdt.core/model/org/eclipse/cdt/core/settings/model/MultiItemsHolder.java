/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.internal.core.settings.model.MultiConfigDescription;

/**
 *
 *
 */
public abstract class MultiItemsHolder implements ICMultiItemsHolder {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#getItems()
	 */
	public abstract Object[] getItems();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#setStringListMode(int)
	 */
	/**
	 * This method is put here to prevent UI from 
	 * accessing constructors in "internal" dirs. 
	 * 
	 * Creates multiple configuration description.
	 * If there's 1 cfg.desc in array, 
	 * it's returned itself. 
	 * 
	 * @param rds - array of cfg.descs
	 *  
	 * @param mode - string list display and write mode
	 * @see DMODE_CONJUNCTION
	 * @see DMODE_EMPTY
	 * @see DMODE_ALL
	 * @see WMODE_DIFF
	 * @see WMODE_CURRENT
	 *
	 * @return multiple cfg.description or single cfg.desc.
	 */
	public static ICConfigurationDescription createCDescription(ICConfigurationDescription[] rds) {
		if (rds == null || rds.length == 0)
			return null;
		else if (rds.length == 1)
			return rds[0];
		else
			return new MultiConfigDescription(rds);
	}
}
