package org.eclipse.cdt.core.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 *  
 * This class provides the infrastructure for defining a builder and fulfills the contract
 * specified by the org.eclipse.cdt.core.cbuilder standard extension point.

 * Note: This class/interface is part of an interim API that is still under development and
 * expected to change significantly before reaching stability. It is being made available at
 * this early stage to solicit feedback from pioneering adopters on the understanding that any
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.
 */
public interface ICBuilder {
	/**
	 * return the search include path list.
	 * @return IPath[]
	 */
	IPath[] getIncludePaths();
	
	/**
	 * Change the search include path lists.
	 * @params IPath[]
	 */
	void setIncludePaths(IPath[] incPaths);
	
	/**
	 * return the search library path list.
	 * @return IPath[]
	 */
	IPath[] getLibraryPaths();

	/**
	 * Change the search library path lists.
	 * @params IPath[]
	 */
	void setLibraryPaths(IPath[] libPaths);

	/**
	 * return the list of libraries use.
	 * @return String[]
	 */
	String[] getLibraries();

	/**
	 * Change the libraries.
	 * @params String[]
	 */
	void setLibraries(String[] libs);

	/**
	 * Get the Optimization level.
	 * @return IOptimization
	 */
	IOptimization getOptimization();

	/**
	 * Change the Optimization level.
	 * @params IOptimization
	 */
	void setOptimization(IOptimization o);

	/**
	 * Build the project.
	 */
	IProject[] build(CIncrementalBuilder cbuilder);

	/**
	 * Method getID.
	 * @return String
	 */
	String getID();
}
