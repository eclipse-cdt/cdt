/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

/**
 * @author dsteffle
 */
public class InvalidPreprocessorNodeException extends Exception {
	
	public InvalidPreprocessorNodeException(String message) {
		super(message);
	}
	
	public InvalidPreprocessorNodeException(String message, int offset) {
		super(message);
		globalOffset = offset;
	}
	
	private int globalOffset = -1;
	
	public int getGlobalOffset() {
		return globalOffset;
	}
	
	public void setGlobalOffset(int offset) {
		globalOffset = offset;
	}
}
