/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTCopyLocation;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author Emanuel Graf IFS
 */
public class ASTCopyLocation implements IASTCopyLocation {
	private IASTNode originalNode;

	public ASTCopyLocation(IASTNode originalNode) {
		this.originalNode = originalNode;
	}

	@Override
	public int getNodeOffset() {
		return 0;
	}

	@Override
	public int getNodeLength() {
		return 0;
	}

	@Override
	public IASTFileLocation asFileLocation() {
		return null;
	}

	@Override
	public IASTNode getOriginalNode() {
		return originalNode;
	}
}
