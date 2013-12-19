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

/** Internal EV constants specific for the Parallella-64 board */
public final class Parallella64Constants extends EpiphanyConstants {
	
	// Constants
	
	/** Number of eCores */
	private static final int NUM_ECORES = 64;

	/** Relative bounds of the Epiphany chip */
	private static final int[] EPIPHANY_CHIP_BOUNDS = { 0, 0, 47, 48 };
	
	/** Relative bounds of status bar, relative to the Epiphany chip container */
	private static final int[] STATUS_BAR_BOUNDS = { 1, 46, 45, 1 };

	/** Relative bounds of the IO, relative to the Epiphany chip container */
	private static final int[] IO_CONTAINER_BOUNDS = { 1, 1, 45, 45 };

	/** Relative bounds of the IOs, relative to the IO Container */
	private static final int[][] IOs_BOUNDS = {
		// N
		{ 0, 0, 45, 1 },
		// E
		{ 44, 0, 1, 45 },
		// S
		{ 0, 44, 45, 1 },
		// W
		{ 0, 0, 1, 45 }
	};

	/** Relative bounds of the CPUS container, relative to the IO Container */
	private static final int[] CPUS_CONTAINER_BOUNDS = { 1, 1, 43, 43 };

	/** Relative bounds of the each CPU, relative to the CPUs container */
	private static final int[][] CPU_CONTAINERS_BOUNDS = {
		// 1st line
		{ 0, 0, 8, 8 },
		{ 5, 0, 8, 8 },
		{ 10, 0, 8, 8 },
		{ 15, 0, 8, 8 },
		{ 20, 0, 8, 8 },
		{ 25, 0, 8, 8 },
		{ 30, 0, 8, 8 },
		{ 35, 0, 8, 8 },
		// 2nd line
		{ 0, 5, 8, 8 },
		{ 5, 5, 8, 8 },
		{ 10, 5, 8, 8 },
		{ 15, 5, 8, 8 },
		{ 20, 5, 8, 8 },
		{ 25, 5, 8, 8 },
		{ 30, 5, 8, 8 },
		{ 35, 5, 8, 8 },
		// 3rd line
		{ 0, 10, 8, 8 },
		{ 5, 10, 8, 8 },
		{ 10, 10, 8, 8 },
		{ 15, 10, 8, 8 },
		{ 20, 10, 8, 8 },
		{ 25, 10, 8, 8 },
		{ 30, 10, 8, 8 },
		{ 35, 10, 8, 8 },
		// 4th line
		{ 0, 15, 8, 8 },
		{ 5, 15, 8, 8 },
		{ 10, 15, 8, 8 },
		{ 15, 15, 8, 8 },
		{ 20, 15, 8, 8 },
		{ 25, 15, 8, 8 },
		{ 30, 15, 8, 8 },
		{ 35, 15, 8, 8 },
		// 5th line
		{ 0, 20, 8, 8 },
		{ 5, 20, 8, 8 },
		{ 10, 20, 8, 8 },
		{ 15, 20, 8, 8 },
		{ 20, 20, 8, 8 },
		{ 25, 20, 8, 8 },
		{ 30, 20, 8, 8 },
		{ 35, 20, 8, 8 },
		// 6th line
		{ 0, 25, 8, 8 },
		{ 5, 25, 8, 8 },
		{ 10, 25, 8, 8 },
		{ 15, 25, 8, 8 },
		{ 20, 25, 8, 8 },
		{ 25, 25, 8, 8 },
		{ 30, 25, 8, 8 },
		{ 35, 25, 8, 8 },
		// 7th line
		{ 0, 30, 8, 8 },
		{ 5, 30, 8, 8 },
		{ 10, 30, 8, 8 },
		{ 15, 30, 8, 8 },
		{ 20, 30, 8, 8 },
		{ 25, 30, 8, 8 },
		{ 30, 30, 8, 8 },
		{ 35, 30, 8, 8 },
		// 8th line
		{ 0, 35, 8, 8 },
		{ 5, 35, 8, 8 },
		{ 10, 35, 8, 8 },
		{ 15, 35, 8, 8 },
		{ 20, 35, 8, 8 },
		{ 25, 35, 8, 8 },
		{ 30, 35, 8, 8 },
		{ 35, 35, 8, 8 }
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
		{ false, true, true,  true,  true,  true,  true,  true },
		{ false, true, true,  true,  true,  true,  true,  true },
		{ false, true, true,  true,  true,  true,  true,  true },
		{ false, true, true,  true,  true,  true,  true,  true },
		{ false, true, false, true, true,  true,  true,  true },

		// second eCore line
		{ true,  true,  true,  true,  true,  true,  false,  true }, // ninth CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true }, 
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },


		// third  eCoreline
		{ true,  true,  true,  true,  true,  true,  false,  true }, // 17th CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true }, 
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },

		// fourth  eCoreline
		{ true,  true,  true,  true,  true,  true,  false,  true }, // 25th CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },

		// fifth  eCoreline
		{ true,  true,  true,  true,  true,  true,  false,  true }, // 33th CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true }, 
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },

		// sixth  eCoreline
		{ true,  true,  true,  true,  true,  true,  false,  true }, // 41th CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },

		// seventh  eCoreline
		{ true,  true,  true,  true,  true,  true,  false,  true }, // 49th CPU
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  true,  true,  true,  true,  true,  true },
		{ true,  true,  false, true, true,  true,  true,  true },

		// 8th eCore line
		{ true,  true,  true,  true,  false, true, false,  true }, // 57th CPU
		{ true,  true,  true,  true,  false, true, true,  true },
		{ true,  true,  true,  true,  false, true, true,  true },
		{ true,  true,  true,  true,  false, true, true,  true },
		{ true,  true,  true,  true,  false, true, true,  true },
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
//		{ false, false, true,  true,  true,  true,  true,  true },
//		{ false, false, true,  true,  true,  true,  true,  true },
//		{ false, false, true,  true,  true,  true,  true,  true },
//		{ false, false, true,  true,  true,  true,  true,  true },
//		{ false, false, false, false, true,  true,  true,  true },
//
//		// second eCore line
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // ninth CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true }, 
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//
//
//		// third  eCoreline
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // 17th CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true }, 
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//
//		// fourth  eCoreline
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // 25th CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//
//		// fifth  eCoreline
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // 33th CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true }, 
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//
//		// sixth  eCoreline
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // 41th CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//
//		// seventh  eCoreline
//		{ true,  true,  true,  true,  true,  true,  false,  false }, // 49th CPU
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  true,  true,  true,  true,  true,  true },
//		{ true,  true,  false, false, true,  true,  true,  true },
//
//		// 8th eCore line
//		{ true,  true,  true,  true,  false, false, false,  false }, // 57th CPU
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  true,  true,  false, false, true,  true },
//		{ true,  true,  false, false, false, false, true,  true }
//	};

	/** Mapping between an eCore id (0, 1, 2, ...) and its coordinate-based "label" string */
	private static final String[] ECORE_ID_TO_LABEL = {
		"0000","0100","0200","0300","0400","0500","0600","0700",
		"0001","0101","0201","0301","0401","0501","0601","0701",
		"0002","0102","0202","0302","0402","0502","0602","0702",
		"0003","0103","0203","0303","0403","0503","0603","0703",
		"0004","0104","0204","0304","0404","0504","0604","0704",
		"0005","0105","0205","0305","0405","0505","0605","0705",
		"0006","0106","0206","0306","0406","0506","0606","0706",
		"0007","0107","0207","0307","0407","0507","0607","0707",
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
