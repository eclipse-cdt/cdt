/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.executables;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.executables.ISourceFileRemapping;
import org.eclipse.cdt.debug.core.executables.ISourceFileRemappingFactory;

public class StandardSourceFileRemappingFactory implements ISourceFileRemappingFactory{

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.ISourceFileRemappingFactory#createRemapper(org.eclipse.cdt.core.model.IBinary)
	 */
	@Override
	public ISourceFileRemapping createRemapper(IBinary binary) {
		return new StandardSourceFileRemapping(binary);
	}

}
