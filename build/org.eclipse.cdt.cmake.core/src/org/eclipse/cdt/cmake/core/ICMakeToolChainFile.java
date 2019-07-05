/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

/**
 * A toolchain file.
 *
 * @noimplement
 * @noextend
 */
public interface ICMakeToolChainFile {

	/**
	 * Return the path to the toolchain file.
	 *
	 * @return path to the toolchain file
	 */
	Path getPath();

	/**
	 * Return the value for a property.
	 *
	 * @param key key of the property
	 * @return the value
	 */
	String getProperty(String key);

	/**
	 * Set a property.
	 *
	 * @param key key of the property
	 * @param value value for the property
	 */
	void setProperty(String key, String value);

	/**
	 * Return the toolchain that this toolchain file enables.
	 *
	 * @return the toolchain for the file
	 * @throws CoreException
	 */
	IToolChain getToolChain() throws CoreException;

}
