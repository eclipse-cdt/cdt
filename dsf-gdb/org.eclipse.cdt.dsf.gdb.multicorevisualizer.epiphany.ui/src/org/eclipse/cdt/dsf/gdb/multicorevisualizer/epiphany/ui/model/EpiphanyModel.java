/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;

@SuppressWarnings("restriction")
public class EpiphanyModel extends VisualizerModel {

	// --- members ---
	
	/** IO Blocks of an Epiphany chip */
	protected ArrayList<EpiphanyModelIO> m_IOBlocks = null;
	
	
	// --- constructors/destructors ---
	
	public EpiphanyModel(String sessionId) {
		super(sessionId);
		m_IOBlocks = new ArrayList<EpiphanyModelIO>();
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		if (m_IOBlocks != null) {
			for (EpiphanyModelIO io : m_IOBlocks) {
				io.dispose();
			}
			m_IOBlocks.clear();
			m_IOBlocks = null;
		}
	}

	// --- accessors ---
	
	// --- core/cpu/io management ---
	
	/** Adapter for superclass's addCPU() method */
	public EpiphanyModelCPU addCPU(EpiphanyModelCPU cpu) {
		return (EpiphanyModelCPU) super.addCPU(cpu);
	}
	
	public EpiphanyModelCPU getCPU(int id) {
		return (EpiphanyModelCPU) super.getCPU(id);
	}
	
//	/** Gets CPU set. */
//	public List<EpiphanyModelCPU> getCPUs() {
//		List<EpiphanyModelCPU> cpus = new ArrayList<EpiphanyModelCPU>();
//		for( VisualizerCPU cpu : m_cpus) {
//			cpus.add((EpiphanyModelCPU)cpu);
//		}
//		return cpus;
//	}
	
//	/** Gets CPU set. */
//	@Override
//	public List<VisualizerCPU> getCPUs() {
//		return  m_cpus;
//	}
	
	/** Adds an IO block */
	public void addIO(EpiphanyModelIO io) {
		m_IOBlocks.add(io);
	}
	
	/** get one IO block  */
	public EpiphanyModelIO getIO(int index) {
		return m_IOBlocks.get(index);
	}
	
	/** get all IO block  */
	public ArrayList<EpiphanyModelIO> getIOs() {
		return m_IOBlocks;
	}
	
	
	// --- Process/thread management ---
	
	/** Get a list of the programs currently running on Epiphany */
	public List<String> getPrograms() {
		List<String> progList = null;
		for(VisualizerThread thread : m_threads) {
			EpiphanyModelThread t = (EpiphanyModelThread) thread;
			if( progList == null) {
				progList = new ArrayList<String>();
			}
			if (!progList.contains(t.getProgramName())) {
				progList.add(t.getProgramName());
			}
		}
		return progList;
	}
	
	/** Get all Epiphany threads running in given program */
	public List<EpiphanyModelThread> getThreadsforProgram(String progName) {
		List<EpiphanyModelThread> progThreads = null;
		for(VisualizerThread thread : m_threads) {
			EpiphanyModelThread t = (EpiphanyModelThread) thread;
			if (progThreads == null) {
				progThreads = new ArrayList<EpiphanyModelThread>();
			}
			if(t.getProgramName().compareTo(progName) == 0) {
				progThreads.add(t);
			}
		}
		return progThreads;
	}
	
	
}