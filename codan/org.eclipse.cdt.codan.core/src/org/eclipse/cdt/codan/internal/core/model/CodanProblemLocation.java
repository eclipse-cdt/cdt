/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public CodanProblemLocation(IResource file, int startChar, int endChar,
			int line) {
		super(file, startChar, endChar);
		this.line = line;
	}

	/**
	 * @deprecated use {@link #CodanProblemLocation(IResource, int, int, int)}
	 *             otherwise no line number will be shown
	 * @param file
	 * @param startChar
	 * @param endChar
	 */
	@Deprecated
	public CodanProblemLocation(IFile file, int startChar, int endChar) {
		super(file, startChar, endChar);
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
