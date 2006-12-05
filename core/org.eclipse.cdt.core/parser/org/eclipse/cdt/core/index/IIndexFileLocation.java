/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.index;

import java.net.URI;

/**
 * Files in the index are (conceptually) partitioned into workspace and non-workspace (external) files.
 * Clients can obtain instances of IIndexFileLocation implementations from {@link IndexLocationFactory}
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexFileLocation {
	/**
	 * The URI of the indexed file
	 * @return the URI of the indexed file (non-null)
	 */
	public URI getURI();
	
	/**
	 * Return the workspace relative path of the indexed file or null if the file
	 * is not in the workspace 
	 * @return the workspace relative path of the file in the index, or null if the
     * file is not in the workspace
	 */
	public String getFullPath();
}
