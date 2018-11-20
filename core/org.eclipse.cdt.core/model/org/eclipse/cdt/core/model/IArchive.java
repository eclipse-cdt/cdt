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
package org.eclipse.cdt.core.model;

/**
 * An IArchive represents a group of files combined  into  a
 * single file(the Archive), for example 'libXX.a'.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IArchive extends ICElement, IParent, IOpenable {
	/**
	 * Return the binaries contain in the archive.
	 * It does not actually extract the files.
	 */
	public IBinary[] getBinaries() throws CModelException;
}
