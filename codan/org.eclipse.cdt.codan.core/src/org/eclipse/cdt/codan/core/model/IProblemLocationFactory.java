/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IFile;

/**
 * Factory interface that allows to create problem locations.
 *
 * Clients may implement and extend this interface.
 */
public interface IProblemLocationFactory {
	/**
	 * Create and return instance of IProblemLocation
	 *
	 * @param file
	 *        - file where problem is found
	 * @param line
	 *        - line number where problem is found, starts with 1
	 * @return instance of IProblemLocation
	 */
	public IProblemLocation createProblemLocation(IFile file, int line);

	/**
	 * Create and return instance of IProblemLocation
	 *
	 * @param astFile - file where problem is found
	 * @param startChar - start char of the problem in the file, is
	 *        zero-relative
	 * @param endChar - end char of the problem in the file, is zero-relative
	 *        and exclusive.
	 *
	 * @param line
	 *        - start line number (for visualization purposes)
	 * @return instance of IProblemLocation
	 */
	public IProblemLocation createProblemLocation(IFile astFile, int startChar, int endChar, int line);
}