/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf & Leo Buettiker - initial API and implementation
 * Guido Zgraggen
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author egraf
 *
 */
public class ASTComment extends ASTNode implements IASTComment {
	
	private char[] comment;
	
	private boolean blockComment;
	
	public ASTComment(IToken commentTocken){
		switch(commentTocken.getType()){
		case IToken.tCOMMENT:
			blockComment = false;
			break;
		case IToken.tBLOCKCOMMENT:
			blockComment = true;
			break;
		default:
			throw new IllegalArgumentException("No Comment Token"); //$NON-NLS-1$
		}
		comment = commentTocken.getImage().toCharArray();
		setOffsetAndLength(commentTocken.getOffset(), commentTocken.getLength());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.ASTNode#accept(org.eclipse.cdt.core.dom.ast.ASTVisitor)
	 */
	public boolean accept(ASTVisitor visitor) {
		if (visitor.shouldVisitComments) {
            switch (visitor.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
	}

	/**
	 * @return the comment
	 */
	public char[] getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(char[] comment) {
		this.comment = comment;
	}

	public boolean isBlockComment() {
		return blockComment;
	}


	public boolean equals(Object obj) {		
		if(! (obj instanceof ASTComment))
			return false;
		ASTComment com2 = (ASTComment)obj;
		if(getOffset() == com2.getOffset() && getLength() == com2.getLength()){
			return true;
		}else{
			return false;
		}
	}

	public static IASTComment[] addComment(IASTComment[] comments, IASTComment comment) {
		if(!ArrayUtil.contains(comments, comment)){
			comments = (IASTComment[]) ArrayUtil.append(IASTComment.class, comments, comment);
		}
		
		return comments;
	}
}
