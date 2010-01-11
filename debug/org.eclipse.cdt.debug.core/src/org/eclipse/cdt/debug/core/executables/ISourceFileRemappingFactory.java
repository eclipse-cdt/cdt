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
package org.eclipse.cdt.debug.core.executables;

import org.eclipse.cdt.core.model.IBinary;

/**
 * Factory that creates an ISourceFileRemapping instance for a particular binary
 * object
 * 
 * @since 7.0
 */
public interface ISourceFileRemappingFactory {
	public ISourceFileRemapping createRemapper(IBinary binary);
}
