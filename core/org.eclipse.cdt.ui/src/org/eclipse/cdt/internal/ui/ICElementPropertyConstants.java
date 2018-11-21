/*******************************************************************************
 * Copyright (c) 2003, 2008 QNX Software Systems and others.
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
package org.eclipse.cdt.internal.ui;

/**
 * This interface documents the property constants used by the ICElement
 * property source.
 */

public interface ICElementPropertyConstants {
	/**
	 * The <code>ICElement</code> property key for elf cpu.
	 */
	public static final String P_ELF_CPU = "elf_cpu"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf text.
	 */
	public static final String P_ELF_TEXT = "text"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf data.
	 */
	public static final String P_ELF_DATA = "data"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf bss.
	 */
	public static final String P_ELF_BSS = "bss"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf bss.
	 */
	public static final String P_ELF_HAS_DEBUG = "debug"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf soname.
	 */
	public static final String P_ELF_SONAME = "soname"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf type.
	 */
	public static final String P_ELF_TYPE = "type"; //$NON-NLS-1$

	/**
	 * The <code>ICElement</code> property key for elf type.
	 */
	public static final String P_ELF_NEEDED = "needed"; //$NON-NLS-1$

	public static final String P_BINARY_FILE_CATEGORY = CUIMessages.ICElementPropertyConstants_catagory;

}
