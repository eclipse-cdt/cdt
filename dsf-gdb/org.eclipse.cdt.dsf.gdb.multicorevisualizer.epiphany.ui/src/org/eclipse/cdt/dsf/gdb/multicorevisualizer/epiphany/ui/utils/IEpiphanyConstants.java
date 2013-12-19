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

public interface IEpiphanyConstants {
	
	public int[] getEcoreBounds();
	public int[] getRouterBounds();
	public int[][] getMeshLinksBounds();
	
	/** Returns the number of eCores of the Epiphany chip */
	public int getNumEcores();
	
	/** Relative bounds of the Epiphany chip */
	public int[] getEpiphanyChipBounds();
	
	/** Relative bounds of the status bar at bottom of EV */
	public int[] getStatusBarBounds();
	
	/** Relative bounds of the IO, relative to the Epiphany chip container */
	public int[] getEpiphanyIoContainerBounds();
	
	/** Relative bounds of the IOs, relative to the IO Container */
	public int[][] getEpiphanyIoBounds();
	
	/** Relative bounds of the CPUS container, relative to the IO Container */
	public int[] getEpiphanyCpuContainerBounds();
	
	/** Relative bounds of the each CPU, relative to the CPUs container */
	public int[][] getEpiphanyCpuContainersBounds();

	/** defines which emesh links are connected, for each CPU */
	public boolean[][] getEpiphanyCpuLinksConnected();
		
	/** Get the user-friendly label associated to an eCore id */
	public String getLabelFromId (int id);
	
	/** Returns the numerical cpu id corresponding to a label */
	public Integer getIdFromLabel(String label);
};
