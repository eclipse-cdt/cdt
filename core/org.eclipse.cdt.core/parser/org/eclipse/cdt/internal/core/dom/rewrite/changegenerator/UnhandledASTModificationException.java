/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;

public class UnhandledASTModificationException extends RuntimeException {
	private final ASTModification illegalModification;

	public UnhandledASTModificationException(ASTModification illegalModification) {
		this.illegalModification = illegalModification;
	}
	
	@Override
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append("Tried to "). //$NON-NLS-1$
				append(illegalModification.getKind().name()).
				append(" on "). //$NON-NLS-1$
				append(illegalModification.getTargetNode()).
				append(" with "). //$NON-NLS-1$
				append(illegalModification.getNewNode());
		return message.toString();
	}
}
