/*******************************************************************************
 * Copyright (c) 2009, 2014 Wind River Systems, Inc. and others.
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
 *     Patrick Chuong (Texas Instruments) - Bug 315446: Invalid event breakpoint type (group) name
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support Register Groups (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String ToggleBreakpointsTargetFactory_description;
	public static String ToggleBreakpointsTargetFactory_name;
	public static String DsfUIStepIntoEditorSelection;
	public static String RegisterGroupInfo;
	public static String Information;
	public static String DefaultRegistersGroupName;
	public static String ProposeGroupNameRoot;
	public static String RegisterGroupConfirmRestoreTitle;
	public static String RegisterGroupConfirmRestoreMessage;
	public static String RegisterGroupRestore;
	public static String RegisterGroupRestoreCancel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
