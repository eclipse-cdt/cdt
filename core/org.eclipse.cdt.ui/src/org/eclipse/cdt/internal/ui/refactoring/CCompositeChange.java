/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;

/**
 * @author Emanuel Graf IFS
 */
public class CCompositeChange extends CompositeChange {
	private RefactoringChangeDescriptor desc;
	
	public CCompositeChange(String name, Change[] children) {
		super(name, children);
	}

	public CCompositeChange(String name) {
		super(name);
	}

	public void setDescription(RefactoringChangeDescriptor descriptor) {
		desc = descriptor;
	}

	@Override
	public ChangeDescriptor getDescriptor() {
		if (desc != null) {
			return desc;
		}
		return super.getDescriptor();
	}
}
