/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.IMulticoreVisualizerConstants;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;

/** Contains constants and methods that are common for all Epiphany platforms */
@SuppressWarnings("restriction")
public abstract class EpiphanyConstants extends IMulticoreVisualizerConstants implements IEpiphanyConstants   {

	// Constants

	/** bounds of the eCore relative to the CPU Container */
//	static final int[] ECORE_BOUNDS = {	0, 0, 3, 3 };
	static final int[] ECORE_BOUNDS = {	0, 0, 5, 5 };

	/** bounds of the mesh router relative to the CPU Container */
	static final int[] ROUTER_BOUNDS = { 3, 3, 2, 2 };

	/** bounds of the links relative to the CPU Container */
	static final int[][] MESH_LINKS_BOUNDS = {
		{ 3, 0, 1, 3 },  // LINK_NORTH_OUT
		{ 4, 0, 1, 3 },  // LINK_NORTH_IN
		{ 5, 3, 3, 1 },  // LINK_EAST_OUT
		{ 5, 4, 3, 1 },  // LINK_EAST_IN
		{ 4, 5, 1, 3 },  // LINK_SOUTH_OUT
		{ 3, 5, 1, 3 },  // LINK_SOUTH_IN
		{ 0, 4, 3, 1 },  // LINK_WEST_OUT
		{ 0, 3, 3, 1 }   // LINK_WEST_IN
	};
	
	/** Default background color */
	public static final Color EV_COLOR_BACKGROUND = Colors.WHITE;
	
	/** Default foreground color */
	public static final Color EV_COLOR_FOREGROUND = Colors.BLACK;

	/** Default selection color */
	public static final Color EV_COLOR_SELECTED = Colors.RED;
	
	/**  */
	public static final Color EV_COLOR_GRID = Colors.GREEN;
	
	/**  */
	public static final Color EV_COLOR_STATUSBAR_TEXT = Colors.WHITE;
	
	/**  */
	public static final Color EV_COLOR_ECORE_TEXT = Colors.BLACK;
	
	/** Default eCore color */
	public static final Color EV_COLOR_ECORE = Colors.GRAY;
	
	/**  */
	public static final Color EV_COLOR_STATUSBAR_BG = Colors.BLACK;
	
	/**  */
	public static final Color EV_COLOR_LOW_LOAD = Colors.DARK_GREEN;
	/**  */
	public static final Color EV_COLOR_MED_LOAD = Colors.YELLOW;
	/**  */
	public static final Color EV_COLOR_HIGH_LOAD = Colors.RED;
	
	/** Color of disconnected IOs and eMesh links */
	public static final Color EV_COLOR_DISCONNECTED = Colors.DARK_GRAY;
	
	/** Color of connected IOs*/
	public static final Color EV_COLOR_CONNECTED = Colors.WHITE;
	
	
	
	// Methods
	
	/** Returns the cpu id corresponding to a label */
	public Integer getIdFromLabel(String label, String[] idToLabel) {
		int i = 0;
		for (String a : idToLabel) {
			if (label.compareTo(a) == 0) {
				return i;
			}
			i++;
		}
		return null;
	}
}
