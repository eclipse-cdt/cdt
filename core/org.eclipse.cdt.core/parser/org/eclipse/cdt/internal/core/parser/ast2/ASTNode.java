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
package org.eclipse.cdt.internal.core.parser.ast2;

import org.eclipse.cdt.core.parser.ast2.IASTNode;

/**
 * @author Doug Schaefer
 */
public class ASTNode implements IASTNode {

	public String getFilename() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object adapt(Class cls) {
		return this;
	}
}
