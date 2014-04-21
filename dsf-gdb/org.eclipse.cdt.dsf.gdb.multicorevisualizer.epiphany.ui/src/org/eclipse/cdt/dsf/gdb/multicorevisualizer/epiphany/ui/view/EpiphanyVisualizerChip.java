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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO.IOPosition;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.IEpiphanyConstants;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;

public class EpiphanyVisualizerChip extends EpiphanyVisualizerContainer {
	
	/** Manages which color is assigned to a given process/pid */
	private class ProcessColor {
		/** Maps program pid to display color */
		private Hashtable<Integer, Color> m_pidColorMap = null;
		Random m_generator = null;
		
		public ProcessColor() {
			m_pidColorMap = new  Hashtable<Integer, Color>(); 
		}
		
		public void dispose() {
			if (m_pidColorMap != null) {
				m_pidColorMap.clear();
				m_pidColorMap = null;
			}
		}
		
		public Color getColor(int pid) {
			Color progColor;
			if (m_pidColorMap.containsKey(pid)) {
				progColor = m_pidColorMap.get(pid);
			}
			else {
				// generate random color - Skipping low values to make result 
				// more readable for visualizer (use paler colors).
				// note: we want a given pid to always generate the same color, 
				// so we use it as the base of the seed. Adding some salt 
				// experimentally to give a nicer color spread 
				// TODO: test this when we have real PIDs coming from Epiphany 
				// / e-server to make sure it works ok.
				m_generator = new Random(pid*9876);
				progColor = Colors.getColor(
						100 + (int)(m_generator.nextDouble() * 155), 
						100 + (int)(m_generator.nextDouble() * 155), 
						100 + (int)(m_generator.nextDouble() * 155)
				);
				// create new mapping
				m_pidColorMap.put(pid, progColor);
			}
			return progColor;
		}
	}
	
	
	// --- members ---
	
	/** Per-process color assignment */
	protected ProcessColor m_procColors = null;
	
	/** Useful constants */
	protected IEpiphanyConstants m_eConstants = null;
	
	/** Status bar at bottom of EV */
	protected EpiphanyVisualizerStatusBar m_statusBar = null;
	
	/** 2nd level container that contains the IOs */
	protected EpiphanyVisualizerContainer m_IOContainer = null;
	
	/** 3rd level container that contains all CPUs */
	protected EpiphanyVisualizerContainer m_CPUsContainer = null;
	

	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerChip(IEpiphanyConstants constants) {
		m_eConstants = constants;
		
		m_procColors = new ProcessColor();
		
		// Create status bar
		m_statusBar = new EpiphanyVisualizerStatusBar();
		m_statusBar.setVirtualBounds(m_eConstants.getStatusBarBounds());
		this.addChildObject(m_statusBar.toString(), m_statusBar);
		
		// create IO container in Epiphany chip container
		m_IOContainer = new EpiphanyVisualizerContainer();
		m_IOContainer.setVirtualBounds(m_eConstants.getEpiphanyIoContainerBounds());
		m_IOContainer.setDrawContainerBounds(true);
		this.addChildObject(m_IOContainer.toString(), m_IOContainer);
		
		// create CPUs container in IO container
		m_CPUsContainer = new EpiphanyVisualizerContainer();
		m_CPUsContainer.setVirtualBounds(m_eConstants.getEpiphanyCpuContainerBounds());
		m_IOContainer.addChildObject(m_CPUsContainer.toString(), m_CPUsContainer);
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		if (m_procColors != null) {
			m_procColors.dispose();
			m_procColors = null;
		}
	}


	// --- Object methods ---

	/** Returns string representation. */
	@Override
	public String toString() {
		return "Epiphany Chip ;" + super.toString();
	}


	// --- accessors ---

	/** Sets the message to be displayed in the EV status bar */
	public void setStatusBarMessage(String msg) {
		m_statusBar.setMessage(msg);
	}
	

	// --- methods ---
	
	/** Create IO graphical object and add it in the IO Container */
	public EpiphanyVisualizerIO addIO(IOPosition position, boolean connected) {
		EpiphanyVisualizerIO io = new EpiphanyVisualizerIO(position, connected);
		// Set position and size for this IO
		io.setVirtualBounds(m_eConstants.getEpiphanyIoBounds()[position.ordinal()]);
		// add it as a child object to the IO Container
		m_IOContainer.addChildObject(io.toString(), io);
		return io;
	}

	/** Create CPU graphical object and add it in the CPUs container */
	public EpiphanyVisualizerCPU addCPU(int id, int pid, String progName, boolean loadMetersEnabled) {
		// create CPU graphical object
		EpiphanyVisualizerCPU cpu = new EpiphanyVisualizerCPU(id, m_eConstants, progName, m_procColors.getColor(pid), loadMetersEnabled);
		// Set position and size of this CPU
		cpu.setVirtualBounds(m_eConstants.getEpiphanyCpuContainersBounds()[id]);
		// add it as a child object to the CPUs Container
		m_CPUsContainer.addChildObject(cpu.toString(), cpu);		
		return cpu;
	}
	
	/** Convenience method to retrieve EpiphanyVisualizerCPU child objects */
	public List<EpiphanyVisualizerCPU> getCPUs() {
		ArrayList<EpiphanyVisualizerCPU> cpuList = new ArrayList<EpiphanyVisualizerCPU>();
		
		// find CPUs among graphical objects
		for ( Object o :  this.getChildObjects(EpiphanyVisualizerCPU.class, true)) {
			cpuList.add((EpiphanyVisualizerCPU)o);
		}
		return cpuList;
	}
	
	// --- paint methods ---

}
