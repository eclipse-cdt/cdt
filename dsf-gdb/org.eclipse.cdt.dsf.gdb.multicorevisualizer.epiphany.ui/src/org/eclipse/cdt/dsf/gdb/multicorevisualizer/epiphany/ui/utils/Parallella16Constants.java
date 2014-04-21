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

/** Internal EV constants specific for the Parallella-16 board */
public final class Parallella16Constants extends EpiphanyConstants {
	
	// Constants
	
	/** Number of eCores */
	private static final int NUM_ECORES = 16;
	
	/** Relative bounds of the Epiphany chip */
	private static final int[] EPIPHANY_CHIP_BOUNDS = { 0, 0, 27, 28 };

	/** Relative bounds of status bar, relative to the Epiphany chip container */
	private static final int[] STATUS_BAR_BOUNDS = { 1, 26, 25, 1 };
	
	/** Relative bounds of the IO, relative to the Epiphany chip container */
	private static final int[] IO_CONTAINER_BOUNDS = { 1, 1, 25, 25 };
	
	/** Relative bounds of the IOs, relative to the IO Container */
	private static final int[][] IOs_BOUNDS = {
		// N
		{ 0, 0, 25, 1 },
		// E
		{ 24, 0, 1, 25 },
		// S
		{ 0, 24, 25, 1 },
		// W
		{ 0, 0, 1, 25 }
	};
	
	/** Relative bounds of the CPUS container, relative to the IO Container */
	private static final int[] CPUS_CONTAINER_BOUNDS = { 1, 1, 23, 23 };
	
	/** Relative bounds of the each CPU, relative to the CPUs container */
	private static final int[][] CPU_CONTAINERS_BOUNDS = {
		// 1st line
		{ 0, 0, 8, 8 },
		{ 5, 0, 8, 8 },
		{ 10, 0, 8, 8 },
		{ 15, 0, 8, 8 },
		// 2nd line
		{ 0, 5, 8, 8 },
		{ 5, 5, 8, 8 },
		{ 10, 5, 8, 8 },
		{ 15, 5, 8, 8 },
		// 3rd line
		{ 0, 10, 8, 8 },
		{ 5, 10, 8, 8 },
		{ 10, 10, 8, 8 },
		{ 15, 10, 8, 8 },
		// 4th line
		{ 0, 15, 8, 8 },
		{ 5, 15, 8, 8 },
		{ 10, 15, 8, 8 },
		{ 15, 15, 8, 8 }
	};
	
	/** defines which emesh links are connected, for each CPU */ 
	//
	// note: it seems a bit strange to define which links are connected
	// at this level. Might be better to deduce this from the connected or not
	// state of the adjacent IO. But not sure how to do that yet.
	private static final boolean[][] CPU_LINKS_CONNECTED = {
		// N-O,  N-I , E-O,   E-I,   S-O,   S-I,   W-O,   W-I
		// first eCore line
		{ false, true, true,  true,  true,  true,  false,  true }, // first CPU
		{ false, true, true,  true,  true,  true,  true,  true }, // second CPU
		{ false, true, true,  true,  true,  true,  true,  true }, // ...
		{ false, true, false, true, true,  true,  true,  true },
		// second eCore line
		{ true,  true,  true,  true,  true,  true,  false,  true }, // fifth CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },
		// third  eCoreline
		{ true,  true,  true,  true,  true,  true,  false,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },
		// forth eCore line
		{ true,  true,  true,  true,  false, true, false,  true },
		{ true,  true,  true,  true,  false, true, true,  true },
		{ true,  true,  true,  true,  false, true, true,  true },
		{ true,  true,  false, true, false, true, true,  true }
	};
	
//	private static final boolean[][] CPU_LINKS_CONNECTED = {
//		// N-O,  N-I , E-O,   E-I,   S-O,   S-I,   W-O,   W-I
//		// first eCore line
//		{ false, false, true,  true,  true,  true,  false,  false }, // first CPU
//		{ false, false, true,  true,  true,  true,  true,  true }, // second CPU
//		{ false, false, true,  true,  true,  true,  true,  true }, // ...
//		{ false, false, false, false, true,  true,  true,  true },
//		// second eCore line
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // fifth CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//		// third  eCoreline
//		{ true,  true,  true,  true,  true,  true,  false,  false },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//		// forth eCore line
//		{ true,  true,  true,  true,  false, false, false,  false },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  false, false, false, false, true,  true }
//	};
	
	/** Mapping between an eCore id (0, 1, 2, ...) and its coordinate-based "label" string */
	private static final String[] ECORE_ID_TO_LABEL = {
		"0000","0001","0002","0003",
		"0100","0101","0102","0103",
		"0200","0201","0202","0203",
		"0300","0301","0302","0303"
	};
	
	// Methods 
	
	@Override
	public int getNumEcores() {
		return NUM_ECORES;
	}

	@Override
	public int[] getEpiphanyChipBounds() {
		return EPIPHANY_CHIP_BOUNDS;
	}

	@Override
	public int[] getStatusBarBounds() {
		return STATUS_BAR_BOUNDS;
	}
	
	@Override
	public int[] getEpiphanyIoContainerBounds() {
		return IO_CONTAINER_BOUNDS;
	}

	@Override
	public int[][] getEpiphanyIoBounds() {
		return IOs_BOUNDS;
	}

	@Override
	public int[] getEpiphanyCpuContainerBounds() {
		return CPUS_CONTAINER_BOUNDS;
	}

	@Override
	public int[][] getEpiphanyCpuContainersBounds() {
		return CPU_CONTAINERS_BOUNDS;
	}

	@Override
	public boolean[][] getEpiphanyCpuLinksConnected() {
		return CPU_LINKS_CONNECTED;
	}

	@Override
	public int[] getEcoreBounds() {
		return ECORE_BOUNDS;
	}

	@Override
	public int[] getRouterBounds() {
		return ROUTER_BOUNDS;
	}

	@Override
	public int[][] getMeshLinksBounds() {
		return MESH_LINKS_BOUNDS;
	}

	@Override
	public String getLabelFromId (int id) {
		return ECORE_ID_TO_LABEL[id];
	}
	
	/** Returns the cpu id corresponding to a label */
	public Integer getIdFromLabel(String label) {
		return getIdFromLabel(label, ECORE_ID_TO_LABEL);
	}
}
