/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.builder.model.ICBuildCmd;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICTool;
import org.eclipse.cdt.core.builder.model.ICToolchain;
import org.eclipse.core.resources.IFile;

/**
 * Standard GNU toolchain.
 */
public class CGnuToolchain implements ICToolchain {

	private Map fToolMap;

	public CGnuToolchain() {
		fToolMap = new HashMap();
		doRefresh();
	}
	
	protected void doRefresh() {
		addTool(new CGnuTool("CC", "gcc"));
		addTool(new CGnuTool("CXX", "c++"));
		addTool(new CGnuTool("CPP", "gcc"));
		addTool(new CGnuTool("AS", "as"));
		addTool(new CGnuTool("LD", "ld"));
		addTool(new CGnuTool("AR", "ar"));
		addTool(new CGnuTool("DEBUGGER", "gdb"));
		addTool(new CGnuTool("PROFILER", "gprof"));
		addTool(new CGnuTool("STRIP", "strip"));
	}

	/**
	 * Determines if a tools exists in the internal map of
	 * tool instances.
	 * 
	 * @param id tool identifier.
	 * @return true if there is a tool instances that corresponds
	 * to the provided id.
	 */
	protected boolean toolExists(String id) {
		return fToolMap.containsKey(id);
	}

	/**
	 * Add a tool to the internal map of tool instances.
	 * 
	 * @param id tool identifier.
	 * @param tc tool instance.
	 */
	protected void addTool(ICTool tool) {
		fToolMap.put(tool.getId(), tool);
	}

	/**
	 * Helper method used to retrieve a tool from the internal
	 * map of tool instances.
	 * 
	 * @param id tool identifier.
	 * @return tool instance, or null if not found.
	 */
	protected ICTool getToolHelper(String id) {
		ICTool	tool	= null;
		Object 	obj 	= fToolMap.get(id);
		if (obj instanceof ICTool) {
			tool = (ICTool) obj;
		}
		return tool;
	}

	/**
	 * Remove a toolchain from the internal map of toolchain instances.
	 * 
	 * @param id toolchain identifier.
	 * @return true if toolchain is removed.
	 */
	protected boolean removeTool(String id) {
		boolean exists = toolExists(id);
		if (exists) {
			Object obj = fToolMap.remove(id);
			obj = null;
		}
		return exists;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchain#canProcess(IFile)
	 */
	public boolean canProcess(IFile file) {
		// TODO: add logic
		return true;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchain#getBuildCommands(IFile, ICBuildConfig)
	 */
	public ICBuildCmd[] getBuildCommands(IFile file, ICBuildConfig cfg) {
		// TODO: add logic
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchain#getDependencies(IFile, ICBuildConfig)
	 */
	public IFile[] getDependencies(IFile file, ICBuildConfig cfg) {
		// TODO: add logic
		// Either depend on gcc -MM, or CDOM
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchain#getOutputs(IFile, ICBuildConfig)
	 */
	public IFile[] getOutputs(IFile file, ICBuildConfig cfg) {
		// TODO: add logic
		// Either depend on gcc -MM, or CDOM
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchain#getTools()
	 */
	public ICTool[] getTools() {
		Collection tcc = fToolMap.values();
		return (ICTool[]) fToolMap.values().toArray(new ICTool[fToolMap.size()]);
	}
}
