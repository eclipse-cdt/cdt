/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.autotools.core;

import org.eclipse.core.runtime.CoreException;

/**
 * @since 1.2
 */
public interface IAutotoolsOption {
	int CATEGORY = 0;
	int BIN = 1;
	int STRING = 2;
	int INTERNAL = 3;
	int MULTIARG = 4;
	int TOOL = 5;
	int FLAG = 6;
	int FLAGVALUE = 7;
	/**
	 * @since 2.0
	 */
	int ENVVAR = 8;

	int getType();

	boolean canUpdate();

	void setValue(String value) throws CoreException;

	String getValue();
}
