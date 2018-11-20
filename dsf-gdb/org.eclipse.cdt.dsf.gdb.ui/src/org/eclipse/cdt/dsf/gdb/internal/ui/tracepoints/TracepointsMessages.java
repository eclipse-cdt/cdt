/*******************************************************************************
 * Copyright (c) 2010, 2014 Wind River Systems and others.
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
 *     Dmitry Kozlov (Mentor Graphics) - trace control view enhancements (Bug 390827)
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

	public static String TraceControlView_buffer_label;
	public static String TraceControlView_buffer_circular_button_label;
	public static String TraceControlView_buffer_circular_on_tooltip;
	public static String TraceControlView_buffer_circular_off_tooltip;
	public static String TraceControlView_buffer_frames_collected;
	public static String TraceControlView_trace_status_offline;
	public static String TraceControlView_tracing_stopped_user_request;
	public static String TraceControlView_tracing_stopped_passcount;
	public static String TraceControlView_tracing_stopped_tracepoint_number;
	public static String TraceControlView_tracing_stopped_buffer_full;
	public static String TraceControlView_tracing_stopped_disconnection;
	public static String TraceControlView_tracing_stopped_error;
	public static String TraceControlView_tracing_stopped_unknown;
	public static String TraceControlView_action_Refresh_label;
	public static String TraceControlView_auto_refresh_action_label;
	public static String TraceControlView_action_trace_variable_details;
	public static String TraceControlView_action_Disconnected_tracing_label;
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
	public static String TraceControlView_trace_status_not_supported;
	public static String TraceControlView_trace_status_inactive;
	public static String TraceControlView_trace_status_no_debug_session;
	public static String TraceControlView_trace_status_not_started;
	public static String TraceControlView_trace_status_in_progress;
	public static String TraceControlView_trace_status_visualization;
	public static String TraceControlView_trace_status_stopped;
	public static String TraceControlView_frame_label;
	public static String TraceControlView_frame_looking;
	public static String TraceControlView_frame_dragging;
	public static String TraceControlView_frame_not_looking;
	public static String TraceControlView_trace_notes_edit_tooltip;
	public static String TraceControlView_trace_notes_save_tooltip;
	public static String TraceControlView_trace_notes_not_set;
	public static String TraceControlView_trace_notes_label;
	public static String TraceControlView_trace_status_secondary_stopped;
	public static String TraceControlView_trace_status_secondary_running;
	public static String TraceControlView_trace_status_secondary_user;
	public static String TraceControlView_trace_status_secondary_refresh_time;
	public static String TraceControlView_trace_status_secondary_offline;
	public static String TraceControlView_action_start;
	public static String TraceControlView_action_stop;
	public static String TraceControlView_action_restart;
	public static String TraceControlView_action_finish_visualization;
	public static String TraceControlView_today;
	public static String TraceControlView_yesterday;
	public static String TraceControlView_date_days;
	public static String TraceControlView_date_hours;
	public static String TraceControlView_date_minutes;
	public static String TraceControlView_date_seconds;
	public static String TraceControlView_date_zero;
	public static String TraceControlView_date_short_days;
	public static String TraceControlView_date_short_hours;
	public static String TraceControlView_date_short_minutes;
	public static String TraceControlView_date_short_seconds;
	public static String TraceControlView_date_short_zero;
	// Not used
	public static String TraceControlView_tracing_stopped_at;

	static {
		NLS.initializeMessages(TracepointsMessages.class.getName(), TracepointsMessages.class);
	}
}
