/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.internal.core.parser.scanner.ILexerLog;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;
import org.eclipse.cdt.internal.core.parser.scanner.Token;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * Base class for all non-preprocessor nodes in the AST.
 */
public abstract class ASTNode implements IASTNode {

    private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];

    private IASTNode parent;
    private ASTNodeProperty property;

    private int length;
    private int offset;

    private boolean frozen = false;
    
    public IASTNode getParent() {
    	return parent;
    }
    
	public IASTNode[] getChildren() {
		ChildCollector collector= new ChildCollector(this);
		return collector.getChildren();
	}
	
	public boolean isFrozen() {
		return frozen;
	}
	
	public void freeze() {
		frozen = true;
		for(IASTNode child : getChildren()) {
			if(child != null) {
				((ASTNode)child).freeze();
			}
		}
	}
    
	protected void assertNotFrozen() throws IllegalStateException {
		if(frozen)
			throw new IllegalStateException("attempt to modify frozen AST node"); //$NON-NLS-1$
	}
	
    public void setParent(IASTNode node) {
    	assertNotFrozen();
    	this.parent = node;
    }
    
    public ASTNodeProperty getPropertyInParent() {
    	return property;
    }
    
    public void setPropertyInParent(ASTNodeProperty property) {
    	assertNotFrozen();
    	this.property = property;
    }
    
    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.locations = null;
    }

    public void setLength(int length) {
        this.length = length;
        this.locations = null;
    }

    public void setOffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
        this.locations = null;
    }

    public void setOffsetAndLength(ASTNode node) {
        setOffsetAndLength(node.getOffset(), node.getLength());
    }

    private IASTNodeLocation[] locations = null;
    private IASTFileLocation fileLocation = null;

    public IASTNodeLocation[] getNodeLocations() {
        if (locations != null)
            return locations;
        if (length == 0) {
        	locations= EMPTY_LOCATION_ARRAY;
        }
        else {
        	final IASTTranslationUnit tu= getTranslationUnit();
        	if (tu != null) {
        		ILocationResolver l= (ILocationResolver) tu.getAdapter(ILocationResolver.class);
        		if (l != null) {
        			locations= l.getLocations(offset, length);
        		}
        	}
        }
        return locations;
    }

    public IASTImageLocation getImageLocation() {
    	final IASTTranslationUnit tu= getTranslationUnit();
    	if (tu != null) {
    		ILocationResolver l= (ILocationResolver) tu.getAdapter(ILocationResolver.class);
    		if (l != null) {
    			return l.getImageLocation(offset, length);
    		}
    	}
        return null;
    }

    public String getRawSignature() {
    	final IASTFileLocation floc= getFileLocation();
        final IASTTranslationUnit ast = getTranslationUnit();
        if (floc != null && ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return new String(lr.getUnpreprocessedSignature(getFileLocation()));
        	}
        }
        return ""; //$NON-NLS-1$
    }

    public String getContainingFilename() {
    	if (offset <= 0 && (length == 0 || offset < 0)) {
    		final IASTNode parent = getParent();
    		if (parent == null) {
    			if (this instanceof IASTTranslationUnit) {
    				return ((IASTTranslationUnit) this).getFilePath();
    			}
    			return ""; //$NON-NLS-1$
    		}
    		return parent.getContainingFilename();
    	}
        return getTranslationUnit().getContainingFilename(offset);
    }

    public IASTFileLocation getFileLocation() {
        if( fileLocation != null )
            return fileLocation;
        if (offset <= 0 && (length == 0 || offset < 0)) {
        	return null;
        }
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		fileLocation= lr.getMappedFileLocation(offset, length);
        	}
        	else {
        		// support for old location map
        		fileLocation= ast.flattenLocationsToFile(getNodeLocations());
        	}
        }
        return fileLocation;
    }
    
    public boolean isPartOfTranslationUnitFile() {
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return lr.isPartOfTranslationUnitFile(offset);
        	}
        }
        return false;
    }
    
    public IASTTranslationUnit getTranslationUnit() {
       	return parent != null ? parent.getTranslationUnit() : null;
    }

    public boolean accept(ASTVisitor visitor) {
    	return true;
    }
    
    public boolean contains(IASTNode node) {
    	if (node instanceof ASTNode) {
    		ASTNode astNode= (ASTNode) node;
    		return offset <= astNode.offset && 
    			astNode.offset+astNode.length <= offset+length;
    	}
    	return false;
    }

	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException {
		int left= getBoundary(-1);
		return getSyntax(left, offset, -1);
	}

	public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException {
    	int right= getBoundary(1);
		return getSyntax(offset+length, right, 1);
	}
    
	/**
	 * Compute the sequence number of the boundary of the leading/trailing syntax.
	 */
	private int getBoundary(int direction) {
		ASTNodeSearch visitor= new ASTNodeSearch(this);
		IASTNode sib= direction < 0 ? visitor.findLeftSibling() : visitor.findRightSibling();
		if (sib == null) {
			direction= -direction;
			sib= getParent();
		}
		if (sib instanceof ASTNode) {
			ASTNode astNode= (ASTNode) sib;
			int offset= astNode.getOffset();
			if (direction < 0) {
				offset+= astNode.getLength();
			}
			return offset;
		}
		// no parent
		throw new UnsupportedOperationException();
	}


	private IToken getSyntax(int fromSequenceNumber, int nextSequenceNumber, int direction) throws ExpansionOverlapsBoundaryException {
    	final IASTTranslationUnit tu= getTranslationUnit();
    	if (!(tu instanceof ASTNode)) 
    		throw new UnsupportedOperationException();
    	
    	ILocationResolver lr= (ILocationResolver) tu.getAdapter(ILocationResolver.class);
    	if (lr == null) 
    		throw new UnsupportedOperationException();

    	int endSequenceNumber= lr.convertToSequenceEndNumber(nextSequenceNumber); 
		IASTFileLocation total= lr.getMappedFileLocation(fromSequenceNumber, endSequenceNumber-fromSequenceNumber);
    	IASTFileLocation myfloc= getFileLocation();
    	if (total == null || myfloc == null)
    		throw new UnsupportedOperationException();
    	
    	if (!total.getFileName().equals(myfloc.getFileName()))
    		throw new ExpansionOverlapsBoundaryException();

    	if (fromSequenceNumber > 0) {
    		IASTFileLocation fl= lr.getMappedFileLocation(fromSequenceNumber-1, endSequenceNumber-fromSequenceNumber+1);
    		if (fl.getFileName().equals(total.getFileName()) && fl.getNodeOffset() == total.getNodeOffset()) 
    			throw new ExpansionOverlapsBoundaryException();
    	}
    	
    	if (endSequenceNumber < ((ASTNode) tu).getOffset() + ((ASTNode) tu).getLength()) {
    		IASTFileLocation fl= lr.getMappedFileLocation(fromSequenceNumber, nextSequenceNumber-fromSequenceNumber+1);
    		if (fl.getFileName().equals(total.getFileName()) && fl.getNodeLength() == total.getNodeLength()) 
    			throw new ExpansionOverlapsBoundaryException();
    	}
    	    	
    	int adjustment= total.getNodeOffset() - myfloc.getNodeOffset();
    	if (direction > 0) {
    		adjustment-= myfloc.getNodeLength();
    	} 

    	char[] txt= lr.getUnpreprocessedSignature(total);
    	Lexer lex= new Lexer(txt, (LexerOptions) tu.getAdapter(LexerOptions.class), ILexerLog.NULL, null);
    	try {
			Token result= lex.nextToken();
			if (result.getType() == IToken.tEND_OF_INPUT)
				return null;
			
			Token last= result;
			for(;;) {
				int offset= last.getOffset() + adjustment;
				int endOffset= last.getEndOffset() + adjustment;
				last.setOffset(offset, endOffset);
				
				Token t= lex.nextToken();
				if (t.getType() == IToken.tEND_OF_INPUT)
					break;
				last.setNext(t);
				last= t;
			}
			return result;
		} catch (OffsetLimitReachedException e) {
			// does not happen without using content assist limit
		}
		return null;
	}
}
