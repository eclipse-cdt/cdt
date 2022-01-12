/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import org.eclipse.cdt.codan.core.model.AbstractProblemLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Codan Problem Location, so far same as abstract class
 *
 */
public class CodanProblemLocation extends AbstractProblemLocation {
	/**
	 * @param file - resource
	 * @param startChar - start chart, absolute file offset starts with 0
	 * @param endChar - end char, absolute file offset, exclusive
	 * @param line - line number
	 */
	public CodanProblemLocation(IResource file, int startChar, int endChar, int line) {
		super(file, startChar, endChar);
		this.line = line;
	}

	/**
	 * @param file - resource
	 * @param startChar - start chart, absolute file offset starts with 0
	 * @param endChar - end char, absolute file offset, exclusive
	 * @param line - line number
	 */
	public CodanProblemLocation(IFile file, int startChar, int endChar, int line) {
		super(file, startChar, endChar);
		this.line = line;
	}

	protected CodanProblemLocation(IResource file, int line) {
		super(file, line);
	}
}
