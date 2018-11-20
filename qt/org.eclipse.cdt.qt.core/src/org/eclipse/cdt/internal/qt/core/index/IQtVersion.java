/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

/**
 * Represents a Qt version in form of major and minor number.
 */
public interface IQtVersion {

	/**
	 * Returns major version number.
	 * @return the major version number
	 */
	int getMajor();

	/**
	 * Returns minor version number.
	 * @return the minor version number
	 */
	int getMinor();

}
