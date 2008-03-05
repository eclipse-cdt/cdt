/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a preprocessor #undef statement.
 */
public interface IASTPreprocessorUndefStatement extends	IASTPreprocessorStatement {

	/**
	 * Returns the reference to the macro, or <code>null</code>.
	 */
    public IASTName getMacroName();
}
