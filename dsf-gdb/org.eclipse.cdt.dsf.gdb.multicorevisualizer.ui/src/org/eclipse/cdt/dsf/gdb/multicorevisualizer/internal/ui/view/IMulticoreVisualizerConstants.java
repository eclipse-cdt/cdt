/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;

/**
 * Constants to be used in the Multicore Visualizer.
 */
public class IMulticoreVisualizerConstants {
	// General canvas colors
	public static final Color COLOR_SELECTED = Colors.CYAN;

	// Colors for drawing threads

	/** Color to be used to draw a running thread */
	public static final Color COLOR_RUNNING_THREAD = Colors.GREEN;
	/** Color to be used to draw a suspended thread */
	public static final Color COLOR_SUSPENDED_THREAD = Colors.YELLOW;
	/** Color to be used to draw a crashed thread */
	public static final Color COLOR_CRASHED_THREAD = Colors.RED;
	/** Color to be used to draw an exited thread (if they are being shown) */
	public static final Color COLOR_EXITED_THREAD = Colors.GRAY;

	/** Color to be used to draw an highlight for the process thread */
	public static final Color COLOR_PROCESS_THREAD = Colors.WHITE;

	// Colors for drawing cores

	/** Color to be used to draw a running core */
	public static final Color COLOR_RUNNING_CORE_FG = Colors.GREEN;
	public static final Color COLOR_RUNNING_CORE_BG = Colors.DARK_GREEN;
	/** Color to be used to draw a suspended core */
	public static final Color COLOR_SUSPENDED_CORE_FG = Colors.YELLOW;
	public static final Color COLOR_SUSPENDED_CORE_BG = Colors.DARK_YELLOW;
	/** Color to be used to draw a crashed core */
	public static final Color COLOR_CRASHED_CORE_FG = Colors.RED;
	public static final Color COLOR_CRASHED_CORE_BG = Colors.DARK_RED;

	// Colors for drawing CPUs

	/** Foreground color for cpu */
	public static final Color COLOR_CPU_FG = Colors.GREEN;
	/** Background color for cpu */
	public static final Color COLOR_CPU_BG = Colors.getColor(0, 64, 0);

	// Colors for text

	/** Foreground color to be used to draw a the text for a thread */
	public static final Color COLOR_THREAD_TEXT_FG = Colors.WHITE;
	/** Background color to be used to draw a the text for a thread */
	public static final Color COLOR_THREAD_TEXT_BG = Colors.BLACK;

	/** Color to be used to draw a the text for a core */
	public static final Color COLOR_CORE_TEXT_FG = Colors.WHITE;
	public static final Color COLOR_CORE_TEXT_BG = Colors.BLACK;

	/** Color to be used to draw the load text */
	public static final Color COLOR_LOAD_TEXT = Colors.GREEN;

	/** Color used to draw text to the status bar */
	public static final Color COLOR_STATUS_BAR_TEXT = Colors.GREEN;

	// Colors for load meters

	/** Color used to draw the bar representing load, under normal load */
	public static final Color COLOR_LOAD_LOADBAR_NORMAL = Colors.GREEN;
	/** Color used to draw the bar representing load, under high load */
	public static final Color COLOR_LOAD_LOADBAR_OVERLOAD = Colors.RED;
	/** Color used to draw the load meter foreground */
	public static final Color COLOR_LOAD_UNDERBAR_FG = Colors.getColor(0, 200, 0);
	/** Color used to draw the load meter background */
	public static final Color COLOR_LOAD_UNDERBAR_BG_DEFAULT = Colors.getColor(0, 64, 0);
}
