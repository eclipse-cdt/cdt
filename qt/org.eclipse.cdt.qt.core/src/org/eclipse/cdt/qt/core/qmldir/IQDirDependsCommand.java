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
 * An AST Node representing a Depends Command in a qmldir file.
 *
 * @see <a href="http://doc.qt.io/qt-5/qtqml-modules-qmldir.html">Module Definition qmldir Files</a>
 */
public interface IQDirDependsCommand extends IQDirCommand {
	/**
	 * Gets the <code>IQDirWord</code> representing the module identifier that this qmldir module depends on.
	 *
	 * @return the module identifier
	 */
	public IQDirWord getModuleIdentifier();

	/**
	 * Gets the <code>IQDirVersion</code> representing the initial version of the module that this qmldir module depends on.
	 *
	 * @return the initial version
	 */
	public IQDirVersion getInitialVersion();
}
