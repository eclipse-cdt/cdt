/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.xlc.core.scannerconfig.util;

import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.KVStringPair;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SCDOptionsEnum;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

/**
 * Class that represents a XL C/C++ compiler command and related scanner configuration 
 */
public class XLCCommandDSC extends CCommandDSC {

	public XLCCommandDSC(boolean cppFileType) {
		super(cppFileType);
	}
	
	public XLCCommandDSC(boolean cppFileType, IProject project) {
		super(cppFileType, project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC#addSCOption(org.eclipse.cdt.make.internal.core.scannerconfig.util.KVStringPair)
	 */
	@Override
	public void addSCOption(KVStringPair option) {
		if (project != null &&
			(option.getKey().equals(SCDOptionsEnum.INCLUDE_FILE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.INCLUDE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.ISYSTEM.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.IQUOTE.toString())))
		{
			String value = option.getValue();
			value = makeRelative(project, new Path(value)).toOSString();
			option = new KVStringPair(option.getKey(), value);
		}
		compilerCommand.add(option);
	}

}
