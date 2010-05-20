/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import org.eclipse.osgi.util.NLS;

/**
 *  @since 2.1
 */
public final class TracepointsMessages extends NLS {

	private TracepointsMessages() {
		// Do not instantiate
	}

	public static String TraceControlView_action_Refresh_label;
	public static String TraceControlView_trace_view_content_updated_label;
	public static String TraceControlView_action_trace_variable_details;
	public static String TraceControlView_trace_variable_invalid_value;
	public static String TraceControlView_trace_variable_tracing_unavailable;
	public static String TraceControlView_trace_variable_details_dialog_title;
	public static String TraceControlView_trace_variable_details_column_name;
	public static String TraceControlView_trace_variable_details_column_init_value;
	public static String TraceControlView_trace_variable_details_column_curr_value;
	public static String TraceControlView_trace_variable_details_refresh_button;
	public static String TraceControlView_trace_variable_details_create_button;
	public static String TraceControlView_trace_variable_details_name_label;
	public static String TraceControlView_trace_variable_details_value_label;
	public static String TraceControlView_create_variable_error;
	public static String TraceControlView_create_variable_empty_name_error;
	public static String TraceControlView_action_exit_visualization_mode;
	
	static {
		NLS.initializeMessages(TracepointsMessages.class.getName(), TracepointsMessages.class);
	}
}
