/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.model.CoreModel;

/* This class is a base for preference pages 
 * which store data in preferences 
 * It means: 
 *  - changes are saved by tabs, not by page
 *  - if changes are made, all projects are
 *    to be updated
 */
public class PrefPage_Abstract extends AbstractPrefPage {
	
	static public boolean isChanged;
	
	public PrefPage_Abstract() {
		super();
		isChanged = false;
	}
	
	protected void doSave(IProgressMonitor monitor) throws CoreException {
		if (isChanged) {
			CoreModel.getDefault().updateProjectDescriptions(null, monitor);
		}
	}

	@Override
	protected String getHeader() { return null;	}
	@Override
	protected boolean isSingle() { return true; }
}
