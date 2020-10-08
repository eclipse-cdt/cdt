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

public interface ICMakeToolChainProvider {

	/**
	 * Allows the provider to add any automatic toolchain files so the user
	 * doesn't have to.
	 *
	 * @param manager the manager object used to add toolchain files
	 */
	void init(ICMakeToolChainManager manager);

}
