/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

/**
 * Interface representing a class that makes one or more
 * toolchains (collections of related tools) available to
 * the IDE.
 * <br>
 * Using a toolchain provider allows clients to implement
 * toolchain location logic in whatever manner suits them
 * best.  For example, a toolchain provider may locate a
 * toolchain by examining the local filesystem, reading
 * a configuration file, providing UI elements to allow
 * a user to specify particular executable to use as part
 * of a specialize toolchain, etc.
 * <p>
 * See also the <a href="../../../../../../CToolchain.html">CToolchain</a>
 * extension point documentation.
 */
public interface ICToolchainProvider {

	/**
	 * Return the ICToolchain instances managed by this provider.
	 * 
	 * @return toolchain instances managed by this provider.
	 */
	ICToolchain[] getToolchains();

	/**
	 * Return an ICToolchain instance managed by this provider.
	 * 
	 * @param id toolchain ID.
	 * @return toolchain instance, or <b>null</b> if the
	 * provider does not recognize the toolchain ID.
	 */
	ICToolchain getToolchain(String id);

}
