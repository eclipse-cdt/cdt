/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.cdt.core.build.IToolChain;

/**
 * The manager for Qt Installs. Qt Installs are Qt installation trees that are produced in a Qt
 * platform build. They have a spec which selects the os and cpu architecture and are managed by an
 * instance of the qmake builder.
 *
 * @noimplement
 */
public interface IQtInstallManager {

	/**
	 * Returns all the registered Qt installs.
	 *
	 * @return all Qt Installs
	 */
	public Collection<IQtInstall> getInstalls();

	/**
	 * Register a new Qt Install
	 *
	 * @param install
	 *            new Qt Install
	 */
	public void addInstall(IQtInstall install);

	/**
	 * Return a Qt install that is managed by the given qmake.
	 *
	 * @param qmakePath
	 *            path to qmake
	 * @return QT install managed by that qmake
	 */
	public IQtInstall getInstall(Path qmakePath);

	/**
	 * Returns the Qt installs that have the matching spec.
	 *
	 * @param spec
	 *            spec for the Qt installs, e.g. macosx-clang
	 * @return all Qt installs that have that spec
	 */
	public Collection<IQtInstall> getInstall(String spec);

	/**
	 * Deregister a given Qt install
	 *
	 * @param install
	 *            Qt install to deregister
	 */
	public void removeInstall(IQtInstall install);

	/**
	 * Check whether the given toolchain supports the given Qt Install. This is done by checking the
	 * OS and CPU arch of the toolchain and returning whether the Qt install supports that
	 * combination.
	 *
	 * @param install
	 *            Qt Install to check
	 * @param toolChain
	 *            Toolchain to check against
	 * @return whether the Qt install supports that toolchain
	 */
	public boolean supports(IQtInstall install, IToolChain toolChain);

	/**
	 * Add a listener for Qt install changes
	 *
	 * @param listener
	 *            listener to add
	 */
	public void addListener(IQtInstallListener listener);

	/**
	 * Remove a listener for Qt install changes
	 *
	 * @param listener
	 *            listener to remove
	 */
	public void removeListener(IQtInstallListener listener);

}
