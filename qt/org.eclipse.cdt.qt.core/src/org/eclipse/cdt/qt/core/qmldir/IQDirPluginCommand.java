/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmldir;

/**
 * An AST Node representing a Plugin Command in a qmldir file.
 *
 * @see <a href="http://doc.qt.io/qt-5/qtqml-modules-qmldir.html">Module Definition qmldir Files</a>
 */
public interface IQDirPluginCommand extends IQDirCommand {
	/**
	 * Gets the <code>IQDirWord</code> representing the name of the plugin.
	 *
	 * @return the plugin name
	 */
	public IQDirWord getName();

	/**
	 * Gets the <code>IQDirWord</code> representing the path to the plugin if it was given.
	 *
	 * @return the path to the plugin or <code>null</code> if not available
	 */
	public IQDirWord getPath();
}
