/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.macros.IFileContextData;
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;
import org.eclipse.core.runtime.IPath;

/**
 * This is a trivial implementation of the IFileContextData used internaly by the MBS
 * 
 * @since 3.0
 */
public class FileContextData implements IFileContextData {
	private IPath fInputFileLocation;
	private IPath fOutputFileLocation;
	private IOptionContextData fOptionContextData;
	
	public FileContextData(IPath inputFileLocation, IPath outputFileLocation, IOption option, IBuildObject optionParent){
		this(inputFileLocation, outputFileLocation, new OptionContextData(option,optionParent));
	}

	public FileContextData(IPath inputFileLocation, IPath outputFileLocation, IOptionContextData optionContextData){
		fInputFileLocation = inputFileLocation;
		fOutputFileLocation = outputFileLocation;
		fOptionContextData = optionContextData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IFileContextData#getInputFileLocation()
	 */
	public IPath getInputFileLocation() {
		return fInputFileLocation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IFileContextData#getOutputFileLocation()
	 */
	public IPath getOutputFileLocation() {
		return fOutputFileLocation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IFileContextData#getOption()
	 */
	public IOptionContextData getOptionContextData() {
		return fOptionContextData;
	}

}
