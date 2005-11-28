/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 31, 2005
 */
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This is the general purpose exception that is thrown for resolving semantic
 * aspects of an illegal binding.
 * 
 * @author aniefer
 */
public class DOMException extends CoreException {
	
	private static final long serialVersionUID = 0;
	
	IProblemBinding problemBinding;

	/**
	 * @param problem
	 *            binding for throwing
	 * 
	 */
	public DOMException(IProblemBinding problem) {
		super(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
				0, "DOMException", new Exception()));
		problemBinding = problem;
	}

	/**
	 * Get the problem associated w/this exception.
	 * 
	 * @return problem
	 */
	public IProblemBinding getProblem() {
		return problemBinding;
	}
}
