/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.internal.core.settings.model.MultiConfigDescription;
import org.eclipse.cdt.internal.core.settings.model.MultiFileDescription;
import org.eclipse.cdt.internal.core.settings.model.MultiFolderDescription;

/**
 *
 *
 */
public abstract class MultiItemsHolder implements ICMultiItemsHolder {
	/** @since 5.2 */
	protected static final boolean DEBUG = false;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#getItems()
	 */
	@Override
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

	/**
	 * This method is put here to prevent UI from
	 * accessing constructors in "internal" dirs.
	 *
	 * Creates multiple resource description, it
	 * can be either MultiFile or MultiFolder.
	 * If there's 1 description in array,
	 * it's returned itself.
	 *
	 * @param rds - array of resource descs
	 *
	 * @return multiple res.description or single res.desc.
	 */
	public static ICResourceDescription createRDescription(ICResourceDescription[] rds) {
		if (rds == null || rds.length == 0)
			return null;
		else if (rds.length == 1)
			return rds[0];
		else if (rds[0] instanceof ICFolderDescription) {
			ICFolderDescription[] fds = new ICFolderDescription[rds.length];
			System.arraycopy(rds, 0, fds, 0, rds.length);
			return new MultiFolderDescription(fds);
		} else {
			ICFileDescription[] fds = new ICFileDescription[rds.length];
			System.arraycopy(rds, 0, fds, 0, rds.length);
			return new MultiFileDescription(fds);
		}
	}
}
