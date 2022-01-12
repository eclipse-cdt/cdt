/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.CoreException;

/**
 * @deprecated
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ICOwner {
	public void configure(ICDescriptor cproject) throws CoreException;

	public void update(ICDescriptor cproject, String extensionID) throws CoreException;
}
