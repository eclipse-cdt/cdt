/*******************************************************************************
 * Copyright (c) 2007, 2014 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Emanuel Graf & Leo Buettiker - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This class represents a comment.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTComment extends IASTNode {
	/** @since 5.4 */
	public final IASTComment[] EMPTY_COMMENT_ARRAY = {};
	
	/**
	 * Sets the comment.
	 * 
	 * @param comment the text of the comment
	 */
	public void setComment(char[] comment);
	
	/**
	 * Returns a char array representation of the comment.
	 */
	public char[] getComment();
	
	/**
	 * Returns true if this is a block comment.
	 */
	public boolean isBlockComment();
}
