/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


/**
 * An IArchive represents a group of files combined  into  a
 * single file(the Archive), for example libxx.a.
 */
public interface IArchive extends ICElement, IParent, IOpenable {
	/**
	 * Return the binaries contain in the archive.
 	 * It does not actually extract the files.
	 */
	public IBinary[] getBinaries() throws CModelException;
}
