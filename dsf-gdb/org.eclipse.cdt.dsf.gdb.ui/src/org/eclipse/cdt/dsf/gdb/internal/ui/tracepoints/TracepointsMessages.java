/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Dmitry Kozlov (Mentor) - trace control view enhancements (Bug 390827)
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

	public static String TraceControlView_circular_buffer_on_tooltip;
	public static String TraceControlView_circular_buffer_off_tooltip;
	public static String TraceControlView_trace_buffer_size_dialog_title;
	public static String TraceControlView_trace_buffer_size_dialog_buffer_size_label;
	public static String TraceControlView_trace_buffer_size_dialog_circular_buffer_checkbox;
	public static String TraceControlView_buffer_number_label_circular;
	public static String TraceControlView_buffer_number_label_linear;
	public static String TraceControlView_trace_status_offline;
	public static String TraceControlView_trace_status_offline_details;
	public static String TraceControlView_tracing_stopped_at;
	public static String TraceControlView_tracing_stopped_user_request;
	public static String TraceControlView_tracing_stopped_passcount;
	public static String TraceControlView_tracing_stopped_tracepoint_number;
	public static String TraceControlView_tracing_stopped_buffer_full;
	public static String TraceControlView_tracing_stopped_disconnection;
	public static String TraceControlView_tracing_stopped_error;
	public static String TraceControlView_tracing_stopped_unknown;
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
	public static String TraceControlView_refresh_variable_error;
	public static String TraceControlView_trace_status_inactive;
	public static String TraceControlView_trace_user_label;
	public static String TraceControlView_trace_user_not_set;
	public static String TraceControlView_trace_user_edit;
	public static String TraceControlView_trace_user_save;
	public static String TraceControlView_trace_status_not_started;
	public static String TraceControlView_trace_status_was_started;
	public static String TraceControlView_frame_looking;
	public static String TraceControlView_frame_not_looking;
	public static String TraceControlView_trace_notes_label;
	public static String TraceControlView_trace_notes_edit_tooltip;
	public static String TraceControlView_trace_notes_save_tooltip;
	public static String TraceControlView_trace_notes_not_set;
	public static String TraceControlView_trace_stop_notes_label;
	public static String TraceControlView_trace_stop_notes_edit_tooltip;
	public static String TraceControlView_trace_stop_notes_save_tooltip;
	public static String TraceControlView_trace_stop_notes_not_set;
	
	static {
		NLS.initializeMessages(TracepointsMessages.class.getName(), TracepointsMessages.class);
	}
}
