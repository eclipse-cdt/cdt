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
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;

/**
 * This is a trivial implementation of the IOptionContextData used internaly by the MBS
 * 
 * @since 3.0
 */
public class OptionContextData implements IOptionContextData {
	private IOption fOption;
	private IBuildObject fParent;

	public OptionContextData(IOption option, IBuildObject parent){
		fOption = option;
		fParent = parent;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IOptionContextData#getOption()
	 */
	public IOption getOption() {
		return fOption;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IOptionContextData#getParent()
	 */
	public IBuildObject getParent() {
		return fParent;
	}

}
