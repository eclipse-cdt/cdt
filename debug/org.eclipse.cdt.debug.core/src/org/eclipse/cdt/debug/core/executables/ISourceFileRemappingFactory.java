/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
