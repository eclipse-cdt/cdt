/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.core.runtime.IPath;

/**
 * Allows the CDI back-end to translate an address to a source location.
 * Usually implemented in the same context as ICDITarget.
 * THIS API IS EXPERIMENTAL AND MAY CHANGE IN THE FUTURE.
 */
public interface ICDIAddressToSource {


	/**
	 * Represents a source location returned by 
	 * ICDIAddressToSource.getSourceForAddress.
	 *
	 */
	interface IMappedSourceLocation extends Comparable {

		/**
		 * Returns the address of the source location.
		 * This should be the same address passed to
		 * ICDIAddressToSource.getSourceForAddress.
		 * @return address of the source location.
		 */
		IAddress getAddress();

		/**
		 * Returns the location of the source file.
		 * @return the location of the source file.
		 */
		IPath getSourceFile();

		/**
		 * Returns the line number corresponding to the address.
		 * @return the line number corresponding to the address.
		 */
		int getLineNumber();

		/**
		 * Returns the name of the function the address is in.
		 * @return the name of the function the address is in.
		 */
		String getFunctionName();

		/**
		 * Returns the unmangled name of the function the address is in.
		 * @return the unmangled name of the function the address is in.
		 */
		String getUnmangledFunctionName();
		
		/**
		 * Return the path to the executable the address is in.
		 * @return the path to the executable the address is in.
		 */
		IPath getExecutable();
	}

	/** Returns a symbol that maps to an address at runtime in a targeted process
	 * @return the symbol (if any) that maps to an address
	 */
	IMappedSourceLocation getSourceForAddress(IAddress address) throws CDIException;

}
