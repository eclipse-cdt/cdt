/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2.cpp;

import org.eclipse.cdt.core.parser.ast2.IASTCompoundType;
import org.eclipse.cdt.core.parser.ast2.IASTScope;

/**
 * Represents a C++ namespace. A namespace is represented here as a
 * compound type even though there can be no instances of a namespace.
 * It does have members, though. And, obviously, a namespace is a scope.
 * 
 * @author Doug Schaefer
 */
public interface ICPPNamespace extends IASTCompoundType, IASTScope {
	
}
