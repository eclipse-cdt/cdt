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

package org.eclipse.cdt.core.builder;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.builder.model.ICTool;
import org.eclipse.cdt.core.builder.util.Filesystem;
import org.eclipse.cdt.internal.core.ProcessClosure;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * <p>
 * Abstract base class to make the life of ICTool implementers
 * somewhat simpler.
 * <p>
 * Provides default implementations of all methods, such that
 * a basic tool can be defined simply by calling the appropriate
 * constructor.
 * <p>
 * Examples:
 * <p>
 * <code>
 * class CGenericTool extends ACTool {
 * 	CGenericTool() {
 * 		super("typeid", "toolid", "toolname");
 * 	}
 * }
 * 
 * class CExplicitTool extends ACTool {
 * 	CGenericTool(IPath pathToTool) {
 * 		super("typeid", "toolid", pathToTool);
 * 	}
 * }
 * </code>
 */
public abstract class ACTool implements ICTool {

	private String fTypeId;
	private String fToolId;
	private IPath fToolPath;

	/**
	 * Constructor.
	 * <br>
	 * Create a tool with the given type ID and unqiue ID by specifying
	 * the absolute path to the executable.
	 * <br>
	 * @param typeId tool type ID, corresponds to a CToolType extension ID.
	 * @param id unqiue identifier for this tool instance.
	 * @param path explicit path to the tool.
	 */
	public ACTool(String typeId, String id, IPath path) {
		fTypeId = typeId;
		fToolId = id;
		fToolPath = path;
	}

	/**
	 * Constructor.
	 * <br>
	 * Create a tool with the given type ID and unqiue ID by specifying
	 * the name of an executable.  The executable is located using the
	 * "which" utility.
	 * <br>
	 * @param typeId tool type ID, corresponds to a CToolType extension ID.
	 * @param id unqiue identifier for this tool instance.
	 * @param name name of the tool executable.
	 */
	public ACTool(String typeId, String id, String exeName) {
		fTypeId = typeId;
		fToolId = id;
		fToolPath = locateExe(exeName);
	}

	/**
	 * Locate the given executable by running "which name".
	 * 
	 * @param name of executable.
	 * @param path to executable.
	 * @return path specifying the location of the executable
	 * with the given name.  If the executable could not be
	 * located, returns <b>null</b>.
	 */
	protected IPath locateExe(String name) {
		IOResults ior = execHelper("which", new String[] { name }, null);
		if (ior.stdout.size() > 0) {
			return new Path(
				Filesystem.getNativePath(ior.stdout.toString().trim()));
		}
		return null;
	}

	/**
	 * Explicity set the path to this tool's executable.
	 * 
	 * @param path path to executable.
	 */
	protected void setPath(String path) {
		fToolPath = new Path(path);
	}

	/**
	 * Helper method that runs this tool using the provided parameters.
	 * 
	 * @param parameters parameters to pass to tool when executing.
	 * @param workingDir working directory for tool execution.
	 * @return object IOResults object containing the stdout and stderr
	 * streams that resulted from running the tool.
	 */
	protected IOResults execHelper(String[] parameters, String workingDir) {
		return execHelper(
			fToolPath.toString(),
			parameters,
			new File(workingDir));
	}

	/**
	 * Helper method that runs a specified tool using the provided parameters.
	 * 
	 * @param exeName name of executable; may be a simple name or a full path.
	 * @param parameters parameters to pass to tool when executing.
	 * @param workingDir working directory for tool execution.
	 * @return object IOResults object containing the stdout and stderr
	 * streams that resulted from running the tool.
	 */
	protected IOResults execHelper(
		String exeName,
		String[] parameters,
		File dir) {
		IOResults ior = new IOResults();
		String[] cmds = new String[parameters.length + 1];

		cmds[0] = exeName;
		for (int i = 1; i < cmds.length; i++) {
			cmds[i] = parameters[i - 1];
		}

		try {
			ProcessFactory pf = ProcessFactory.getFactory();
			Process pid = pf.exec(cmds, null, dir);
			ProcessClosure pc = new ProcessClosure(pid, ior.stdout, ior.stderr);
			pc.runBlocking();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ior;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICTool#getTypeId()
	 */
	public String getTypeId() {
		return fTypeId;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICTool#getId()
	 */
	public String getId() {
		return fToolId;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICTool#getPath()
	 */
	public IPath getPath() {
		return (IPath) fToolPath.clone();
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICTool#exists()
	 */
	public boolean exists() {
		return fToolPath.toFile().exists();
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICTool#exec(String[], String)
	 */
	public IOResults exec(String[] parameters, String workingDir) {
		return execHelper(parameters, workingDir);
	}
}
