/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmldir;

/**
 * An AST Node representing a set of characters that does not contain whitespace and does not start with a digit. This encompasses
 * the syntax for Identifiers, Qualified IDs, Paths, and File Names all in one parser rule.
 */
public interface IQDirWord extends IQDirASTNode {
	/**
	 * Gets the String representing this word as it appears in the qmldir file.<br>
	 * <br>
	 * <b>Note:</b> The text is not modified or validated in any way when it is parsed. It is necessary for the caller to perform
	 * semantic validation of the returned value to ensure it represents a valid identifier, filename, or path.
	 *
	 * @return a string representing this word
	 */
	public String getText();
}
