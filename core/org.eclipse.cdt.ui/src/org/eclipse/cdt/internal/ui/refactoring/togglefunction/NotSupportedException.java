/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

/**
 * Thrown when the developer had no time to implement a special case the user
 * tried to refactor.
 * 
 */
public class NotSupportedException extends RuntimeException {

	private static final long serialVersionUID = -4359705945683270L;

	public NotSupportedException(String message) {
		super(message);
	}
}
