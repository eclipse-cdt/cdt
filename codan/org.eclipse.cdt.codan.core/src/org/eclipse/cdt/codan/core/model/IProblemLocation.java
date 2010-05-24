/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;

/**
 * Interface to describe problem location. Usually contains file and linenumber,
 * also supports character positions for sophisticated errors.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemLocation {
	/**
	 * 
	 * @return File for the problem - absolute full paths
	 */
	IResource getFile();

	/**
	 * 
	 * @return Primary line for the problem, lines start with 1 for file. If -1
	 *         char position would be used.
	 */
	int getLineNumber();

	/**
	 * 
	 * @return character position where problem starts within file, first char
	 *         is 0, inclusive, tab count as one. If unknown return -1.
	 */
	int getStartingChar();

	/**
	 * 
	 * @return character position where problem ends within file, first char is
	 *         0, exclusive, tab count as one. If unknown return -1.
	 */
	int getEndingChar();

	/**
	 * 
	 * @return extra data for problem location, checker specific, can be
	 *         backtrace for example
	 */
	Object getData();
}
