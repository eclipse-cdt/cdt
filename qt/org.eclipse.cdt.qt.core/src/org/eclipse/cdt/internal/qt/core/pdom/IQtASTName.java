/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.core.runtime.CoreException;

public interface IQtASTName extends IASTName {
	/**
	 * Create and return a new instance of PDOMBinding for the receiver.  The implementation
	 * is allowed to return null if there is no possibility of creating a PDOMBinding.
	 * The value that is returned must be consistent -- if null is returned one time then
	 * it must be returned every time.
	 */
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException;
}
