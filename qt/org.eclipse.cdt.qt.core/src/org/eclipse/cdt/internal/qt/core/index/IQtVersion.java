/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
