/**
 * 
 */
package com.ashling.riscfree.globalvariable.view.datamodel;

import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;

/**
 * @author vinod
 *
 */
public interface IGlobalVariableDMContext extends IVariableDMContext {

	/**
	 * Get the global variable name
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Get the relative file name where global variable declared, "../src/main.c"
	 * 
	 * @return
	 */
	String getRelativeFilePath();

	/**
	 * Get the absolute path for the file where global variable is declared,
	 * "/home/test/src/main.c"
	 * 
	 * @return
	 */
	String getAbsoluteFilePath();

	/**
	 * Get the line number where the variable is declared
	 * 
	 * @return
	 */
	int getLineNumber();

	/**
	 * Get the type of global variable , "int [1000]"
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Get the complete declaration of global varialble, "int b[1000];"
	 * 
	 * @return
	 */
	String getDescription();
}
