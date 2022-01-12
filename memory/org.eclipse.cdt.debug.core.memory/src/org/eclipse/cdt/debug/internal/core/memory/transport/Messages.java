/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.memory.transport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.internal.core.memory.transport.messages"; //$NON-NLS-1$
	public static String FileExport_e_export_memory;
	public static String FileExport_e_read_target;
	public static String FileExport_e_write_file;
	public static String FileExport_sub_transferring;
	public static String FileExport_task_transferring;
	public static String FileImport_e_import_file;
	public static String FileImport_e_read_file;
	public static String FileImport_e_write_target;
	public static String FileImport_task_transferring;
	public static String PlainTextImport_e_invalid_format;
	public static String SRecordImport_e_checksum_failure;
	public static String SRecordImport_e_invalid_address;
	public static String SRecordImport_e_invalid_checksum_format;
	public static String SRecordImport_e_invalid_data;
	public static String SRecordImport_e_invalid_line_length;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
