/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.visualizer.ui.util.Todo;

/** 
 * Class representing the state of the data to display in the MulticoreVisualizer. 
 */
public class VisualizerModel
{
	// --- members ---
	
	/** List of cpus (and cores) */
	protected ArrayList<VisualizerCPU> m_cpus;
	
	/** Lookup table for CPUs */
	protected Hashtable<Integer, VisualizerCPU> m_cpuMap;
	
	/** List of threads */
	protected ArrayList<VisualizerThread> m_threads;
	
	/** Completion state tracker. */
	protected Todo m_todo;
	
	// Setting to remove exited threads, or keep them shown.
	// If we are to support this, we should have a preference
	// and a way to for the user to clean up old threads,
	// or maybe a timeout to remove them.
	private boolean m_keepExitedThreads = false;
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public VisualizerModel() {
		m_cpus = new ArrayList<VisualizerCPU>();
		m_cpuMap = new Hashtable<Integer, VisualizerCPU>();
		m_threads = new ArrayList<VisualizerThread>();
		m_todo = new Todo();
	}
	
	/** Dispose method */
	public void dispose() {
		if (m_cpus != null) {
			for (VisualizerCPU cpu : m_cpus) {
				cpu.dispose();
			}
			m_cpuMap.clear();
			m_cpuMap = null;
			m_cpus.clear();
			m_cpus = null;
		}
		if (m_threads != null) {
			for (VisualizerThread thread : m_threads) {
				thread.dispose();
			}
			m_threads.clear();
			m_threads = null;
		}
		if (m_todo != null) {
			m_todo.dispose();
			m_todo = null;
		}
	}
	
	
	// --- accessors ---
	
	/** Gets completion state tracker. */
	public Todo getTodo() {
		return m_todo;
	}
	
	// --- methods ---
	
	/** Sorts cores, cpus, etc. by IDs. */
	public void sort() {
		Collections.sort(m_cpus);
		for (VisualizerCPU cpu : m_cpus) cpu.sort();
		Collections.sort(m_threads);
	}
	
	
	// --- core/cpu management ---
	
	/** Gets number of CPUs. */
	public int getCPUCount() {
		return m_cpus.size();
	}
	
	/** Gets CPU with specified ID. */
	public VisualizerCPU getCPU(int id) {
		return m_cpuMap.get(id);
	}
	
	/** Gets Core with specified ID. */
	public VisualizerCore getCore(int id) {
		VisualizerCore result = null;
		for (VisualizerCPU cpu: m_cpus) {
			result = cpu.getCore(id);
			if (result != null) break;
		}
		return result;
	}
	
	/** Gets CPU set. */
	public List<VisualizerCPU> getCPUs() {
		return m_cpus;
	}
	
	/** Adds CPU. */
	public VisualizerCPU addCPU(VisualizerCPU cpu) {
		m_cpus.add(cpu);
		m_cpuMap.put(cpu.getID(), cpu);
		return cpu;
	}

	/** Removes CPU. */
	public void removeCPU(VisualizerCPU cpu) {
		m_cpus.remove(cpu);
		m_cpuMap.remove(cpu.getID());
	}

	
	/** Gets maximum number of cores per CPU. */
	public int getCoresPerCPU() {
		int maxCores = 1;
		for (VisualizerCPU cpu : m_cpus) {
			int cores = cpu.getCoreCount();
			if (cores > maxCores) maxCores = cores;
		}
		return maxCores;
	}
	
	
	// --- thread management ---
	
	/** Gets threads. */
	public List<VisualizerThread> getThreads() {
		return m_threads;
	}

	/** 
	 * Finds thread(s) by process ID.
	 * If no threads are found, returns null rather
	 * than an empty list.
	 */
	public List<VisualizerThread> getThreadsForProcess(int processId) {
		List<VisualizerThread> result = null;
		for (VisualizerThread thread : m_threads) {
			if (thread.getPID() == processId) {
				if (result == null) result = new ArrayList<VisualizerThread>();
				result.add(thread);
			}
		}
		return result;
	}

	/** 
	 * Find a thread by GDB threadId.
	 * Since thread ids are unique across a GDB session,
	 * we can uniquely find a thread based on its id.
	 */
	public VisualizerThread getThread(int threadId) {
		VisualizerThread result = null;
		for (VisualizerThread thread : m_threads) {
			if (thread.getGDBTID() == threadId) {
				result = thread;
				break;
			}
		}
		return result;
	}

	/** Adds thread. */
	public VisualizerThread addThread(VisualizerThread thread) {
		m_threads.add(thread);
		return thread;
	}

	/** Removes thread. */
	public void removeThread(VisualizerThread thread) {
		m_threads.remove(thread);
	}

	/** 
	 * Removes thread by GDB threadId.
	 */
	public void removeThread(int threadId) {
		Iterator<VisualizerThread> itr = m_threads.iterator();
		while (itr.hasNext()) {
			VisualizerThread thread = itr.next();
			if (thread.getGDBTID() == threadId) {
				itr.remove();
				break;
			}
		}
	}
	
	/**
	 * Mark the specified thread as having exited.
	 */
	public void markThreadExited(int threadId) {
		if (m_keepExitedThreads) {
			VisualizerThread thread = getThread(threadId);
			thread.setState(VisualizerExecutionState.EXITED);
		} else {
			removeThread(threadId);
		}
	}
}
