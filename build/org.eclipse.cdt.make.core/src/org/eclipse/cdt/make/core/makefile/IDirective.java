/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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
package org.eclipse.cdt.make.core.makefile;

/**
 * A Makefile can contain rules, macro definitons and comments.
 * They are call directives.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDirective {

	/**
	 * @return the parent of this directive, null if none.
	 */
	IDirective getParent();

	/**
	 * @return the starting line number of this directive.
	 * The numbering starts at 1 .i.e the first line is not 0
	 */
	int getStartLine();

	/**
	 * @return the ending line number of this directive.
	 * The numbering starts at 1 .i.e the first line is not 0
	 */
	int getEndLine();

	/**
	 * Returns the makefile where the directive was found.
	 *
	 * @return <code>IMakefile</code>
	 */
	IMakefile getMakefile();

	@Override
	String toString();
}
