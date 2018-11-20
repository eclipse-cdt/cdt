/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *     Marc Khouzam (Ericsson) - Support for exited processes in the debug view (bug 407340)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

/**
 * @since 2.0
 */
public interface IGdbLaunchVMConstants {

	public static final String PROP_OS_ID = "os_id"; //$NON-NLS-1$

	/**
	 * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
	 */
	public static final String PROP_OS_ID_KNOWN = "os_id_known"; //$NON-NLS-1$

	public static final String PROP_CORES_ID = "cores_id"; //$NON-NLS-1$

	/**
	 * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
	 */
	public static final String PROP_CORES_ID_KNOWN = "cores_id_known"; //$NON-NLS-1$

	/**
	 * The context is pinned. Value <code>true</code> or <code>false</code>.
	 */
	public static final String PROP_PINNED_CONTEXT = "pinned_context"; //$NON-NLS-1$

	/**
	 * The pin color. One of the <code>IPinElementColorDescriptor</code> color value.
	 */
	public static final String PROP_PIN_COLOR = "pin_color"; //$NON-NLS-1$

	public static final String PROP_THREAD_SUMMARY_KNOWN = "thread_summary_known"; //$NON-NLS-1$
	public static final String PROP_THREAD_SUMMARY = "thread_summary"; //$NON-NLS-1$

	/**
	 * If this property is set, it indicates the process or thread should be shown as exited.
	 */
	public static final String PROP_THREAD_EXITED = "thread_exited"; //$NON-NLS-1$

	/**
	 * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
	 */
	public static final String PROP_EXIT_CODE_KNOWN = "exit_code_known"; //$NON-NLS-1$
	/**
	* If set, the value of the property indicates the exit code returned.
	*/
	public static final String PROP_EXIT_CODE = "exit_code"; //$NON-NLS-1$
}
