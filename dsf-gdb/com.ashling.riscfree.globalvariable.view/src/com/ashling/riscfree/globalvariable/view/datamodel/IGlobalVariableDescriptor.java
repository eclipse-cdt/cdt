package com.ashling.riscfree.globalvariable.view.datamodel;

import org.eclipse.core.runtime.IPath;

/**
 * Provides the description of a global variable.
 */
public interface IGlobalVariableDescriptor {

	/**
	 * Returns the name of the global variable
	 *
	 * @return the name of the global variable
	 */
	public String getName();

	/**
	 * Returns the path of the source file that contains the definition of the
	 * global variable.
	 *
	 * @return the path of the source file
	 */
	public IPath getPath();

	/**
	 * Get file name where global variable defined.
	 * 
	 * @return
	 */
	public String getFileName();

	/**
	 * Get Absolute path for file name where global variable is defined.
	 * 
	 * @return
	 */
	public String getFullname();

	/**
	 * Get line number in which global variable is defined.
	 * 
	 * @return
	 */
	public int getLine();

	/**
	 * Get type of global variable
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Get description for global variable
	 * 
	 * @return
	 */
	public String getDescription();
}