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
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Emanuel Graf IFS
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.3
 */
public interface IASTCopyLocation extends IASTNodeLocation {
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IASTNode getOriginalNode();
}
