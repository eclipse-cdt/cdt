/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Represents a Binary file, for example an ELF executable.
 * An ELF parser will inspect the binary.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBinary extends ICElement, IParent, IOpenable {
	/**
	 * Return whether the file was compiling with debug symbols.
	 */
	public boolean hasDebug();

	public boolean isExecutable();

	public boolean isObject();

	public boolean isSharedLib();

	public boolean isCore();

	public String[] getNeededSharedLibs();

	public String getSoname();

	public String getCPU();

	public long getText();

	public long getData();

	public long getBSS();

	public boolean isLittleEndian();

	/**
	 * Determines whether this binary is part of the binary container. The binary container collects
	 * binaries from a project. This is typically used to presents the executables of a project under
	 * a common node in the CView or ProjectNavigator.
	 */
	public boolean showInBinaryContainer();
	//public IAddressFactory getAddressFactory();

}
