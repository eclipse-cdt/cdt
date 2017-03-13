/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredVariableInstance;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPDeferredVariableInstance extends CPPDeferredVariableInstance
		implements IIndexBinding {
	public CompositeCPPDeferredVariableInstance(ICPPVariableTemplate template, 
			ICPPTemplateArgument[] arguments) {
		super(template, arguments);
	}
	
	@Override
	public boolean isFileLocal() throws CoreException {
		return false;
	}

	@Override
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	@Override
	public IIndexBinding getOwner() {
		return (IIndexBinding) super.getOwner();
	}
}
