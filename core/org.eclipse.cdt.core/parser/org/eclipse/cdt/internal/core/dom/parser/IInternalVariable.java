/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;

/**
 * Internal interface for bindings in the ast that have values.
 */
public interface IInternalVariable extends IVariable {
	/**
	 * Returns the value of the variable, or <code>null</code>. 
	 * If the recursion depth is reached {@link Value#UNKNOWN} will be returned.
	 */
	IValue getInitialValue(int maxRecursionDepth);
}
