/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * This is the interface to the Persisted DOM (PDOM).
 * It provides services to allow access to DOM information
 * persisted between parses.
 */
public interface IPDOM {

	public IBinding resolveBinding(IASTName name);
	
	public IASTName[] getDeclarations(IBinding binding);
	
	public void delete() throws CoreException;
	
	public ICodeReaderFactory getCodeReaderFactory();
	
	public ICodeReaderFactory getCodeReaderFactory(IWorkingCopy root);
	
}
