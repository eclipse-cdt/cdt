/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * 
 * A C/C++ extension of <code>ISourceLocator</code>. 
 * Provides constants and methods to manage different source modes.
 * 
 * @since Aug 19, 2002
 */
public interface ICSourceLocator extends ISourceLocator
{
	static final public int MODE_SOURCE = 0;
	static final public int MODE_DISASSEMBLY = 1;
	static final public int MODE_MIXED = 2;

	/**
	 * Returns the current source presentation mode.
	 * 
	 * @return the current source presentation mode
	 */
	int getMode();

	/**
	 * Sets the source presentation mode.
	 * 
	 * @param the source presentation mode to set
	 */
	void setMode( int mode );
	
	/**
	 * Returns the line number of the instruction pointer in the specified 
	 * stack frame that corresponds to a line in an associated source element, 
	 * or -1 if line number information is unavailable.
	 * 
	 * @param frameInfo the frame data
	 * @return line number of instruction pointer in this stack frame, 
	 * 		   or -1 if line number information is unavailable
	 */
	int getLineNumber( IStackFrameInfo frameInfo );

	/**
	 * Returns a source element that corresponds to the given file name, or
	 * <code>null</code> if a source element could not be located.
	 * 
	 * @param fileName the file name for which to locate source
	 * @return an object representing a source element. 
	 */
	Object getSourceElement( String fileName );
	
	/**
	 * Returns a source element that corresponds to the given function, or
	 * <code>null</code> if a source element could not be located.
	 * 
	 * @param function the function name for which to locate source
	 * @return an object representing a source element. 
	 */
	Object getSourceElementForFunction( String function );
	
	/**
	 * Returns a source element that corresponds to the given address, or
	 * <code>null</code> if a source element could not be located.
	 * 
	 * @param address the address for which to locate source
	 * @return an object representing a source element. 
	 */
	Object getSourceElementForAddress( long address );
	
	/**
	 * Returns the source locations of this locator.
	 * 
	 * @return the source locations of this locator
	 */
	ICSourceLocation[] getSourceLocations();
	
	/**
	 * Sets the source locations of this locator.
	 * 
	 * @param location - an array of source locations
	 */
	void setSourceLocations( ICSourceLocation[] locations );
}