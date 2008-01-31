/**********************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 **********************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;

/**
 * @author Doug Schaefer
 *
 */
public interface ICPPDelegateCreator {
	
	/**
	 * Creates a delegate binding for the using declaration provided.
	 * @param usingDecl
	 */
    ICPPDelegate createDelegate( ICPPUsingDeclaration usingDecl );
}
