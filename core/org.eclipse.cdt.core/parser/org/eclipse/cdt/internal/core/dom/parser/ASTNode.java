/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCopyLocation;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.parser.scanner.ILexerLog;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.parser.scanner.Token;

/**
 * Base class for all non-preprocessor nodes in the AST.
 */
public abstract class ASTNode implements IASTNode {
	protected static final ICPPFunction UNINITIALIZED_FUNCTION = new CPPFunction(null);
    private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = {};

    private IASTNode parent;
    private ASTNodeProperty property;

    /**
     * The sequence number of the ast-node as calculated by the location map.
     * Do not access directly, because getOffset() may be overloaded for lazy calculations.
     */
    private int offset;
    private int length;
    private IASTNodeLocation[] locations;
    private IASTFileLocation fileLocation;

    private boolean frozen = false;
    private boolean active = true;
    
    @Override
	public IASTNode getParent() {
    	return parent;
    }
    
	@Override
	public IASTNode[] getChildren() {
		ChildCollector collector= new ChildCollector(this);
		return collector.getChildren();
	}
	
	@Override
	public boolean isFrozen() {
		return frozen;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	void setIsFrozen() {
		frozen = true;
	}
	
	public void setInactive() {
		if (frozen)
			throw new IllegalStateException("attempt to modify frozen AST node"); //$NON-NLS-1$
		active= false;
	}
    
	protected void assertNotFrozen() throws IllegalStateException {
		if (frozen)
			throw new IllegalStateException("attempt to modify frozen AST node"); //$NON-NLS-1$
	}
	
    @Override
	public void setParent(IASTNode node) {
    	assertNotFrozen();
    	this.parent = node;
    }
    
    @Override
	public ASTNodeProperty getPropertyInParent() {
    	return property;
    }
    
    @Override
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
        this.fileLocation = null;
    }

    public void setLength(int length) {
        this.length = length;
        this.locations = null;
        this.fileLocation = null;
    }

    public void setOffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
        this.locations = null;
        this.fileLocation = null;
    }

    public void setOffsetAndLength(ASTNode node) {
        setOffsetAndLength(node.getOffset(), node.getLength());
    }

    @Override
	public IASTNodeLocation[] getNodeLocations() {
        if (locations != null)
            return locations;
        if (length == 0) {
        	locations= EMPTY_LOCATION_ARRAY;
        } else {
        	final IASTTranslationUnit tu= getTranslationUnit();
        	if (tu != null) {
        		ILocationResolver l= (ILocationResolver) tu.getAdapter(ILocationResolver.class);
        		if (l != null) {
        			locations= l.getLocations(getOffset(), length);
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
    			return l.getImageLocation(getOffset(), length);
    		}
    	}
        return null;
    }

    protected char[] getRawSignatureChars() {
    	final IASTFileLocation floc= getFileLocation();
        final IASTTranslationUnit ast = getTranslationUnit();
        if (floc != null && ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return lr.getUnpreprocessedSignature(getFileLocation());
        	}
        }
        return CharArrayUtils.EMPTY;
    }

    @Override
	public String getRawSignature() {
    	return new String(getRawSignatureChars());
    }

    @Override
	public String getContainingFilename() {
    	final int offset = getOffset();
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

    @Override
	public IASTFileLocation getFileLocation() {
        if (fileLocation != null)
            return fileLocation;
        // TODO(sprigogin): The purpose of offset == 0 && length == 0 condition is not clear to me.
        final int offset = getOffset();
		if (offset < 0 || (offset == 0 && length == 0 && !(this instanceof IASTTranslationUnit))) {
        	return null;
        }
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		fileLocation= lr.getMappedFileLocation(offset, length);
        	} else {
        		// Support for old location map
        		fileLocation= ast.flattenLocationsToFile(getNodeLocations());
        	}
        }
        return fileLocation;
    }
    
    @Override
	public boolean isPartOfTranslationUnitFile() {
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return lr.isPartOfTranslationUnitFile(getOffset());
        	}
        }
        return false;
    }
    
    public boolean isPartOfSourceFile() {
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return lr.isPartOfSourceFile(getOffset());
        	}
        }
        return false;
    }
    
    @Override
	public IASTTranslationUnit getTranslationUnit() {
       	return parent != null ? parent.getTranslationUnit() : null;
    }

    @Override
	public boolean accept(ASTVisitor visitor) {
    	return true;
    }
    
    @Override
	public boolean contains(IASTNode node) {
    	if (node instanceof ASTNode) {
    		ASTNode astNode= (ASTNode) node;
    		final int offset = getOffset();
    		final int nodeOffset= astNode.getOffset();
			return offset <= nodeOffset && nodeOffset + astNode.length <= offset + length;
    	}
    	return false;
    }

	@Override
	public IToken getSyntax() throws ExpansionOverlapsBoundaryException {
		final int offset = getOffset();
		return getSyntax(offset, offset + length, 0);
	}

	@Override
	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException {
		int left= getBoundary(-1);
		return getSyntax(left, getOffset(), -1);
	}

	@Override
	public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException {
    	int right= getBoundary(1);
		return getSyntax(getOffset() + length, right, 1);
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
				offset += astNode.getLength();
			}
			return offset;
		}
		// No parent.
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
		IASTFileLocation total= lr.getMappedFileLocation(fromSequenceNumber, endSequenceNumber - fromSequenceNumber);
    	IASTFileLocation myfloc= getFileLocation();
    	if (total == null || myfloc == null)
    		throw new UnsupportedOperationException();
    	
    	if (!total.getFileName().equals(myfloc.getFileName()))
    		throw new ExpansionOverlapsBoundaryException();

    	if (fromSequenceNumber > 0) {
    		IASTFileLocation fl= lr.getMappedFileLocation(fromSequenceNumber-1, endSequenceNumber - fromSequenceNumber + 1);
    		if (fl.getFileName().equals(total.getFileName()) && fl.getNodeOffset() == total.getNodeOffset()) 
    			throw new ExpansionOverlapsBoundaryException();
    	}
    	
    	if (endSequenceNumber < ((ASTNode) tu).getOffset() + ((ASTNode) tu).getLength()) {
    		IASTFileLocation fl= lr.getMappedFileLocation(fromSequenceNumber, nextSequenceNumber - fromSequenceNumber + 1);
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
			Token result= null;	
			Token last= null;
			while (true) {				
				Token t= lex.nextToken();
				switch (t.getType()) {
				case IToken.tEND_OF_INPUT:
					return result;
				case Lexer.tNEWLINE:
					break;
				default:
					int offset= t.getOffset() + adjustment;
					int endOffset= t.getEndOffset() + adjustment;
					t.setOffset(offset, endOffset);
					if (last == null) {
						result= last= t;
					} else {
						last.setNext(t);
						last= t;
					}
					break;
				}
			}
		} catch (OffsetLimitReachedException e) {
			// Does not happen without using content assist limit.
		}
		return null;
	}

	protected void setCopyLocation(IASTNode originalNode) {
		locations = new IASTNodeLocation[] { new ASTCopyLocation(originalNode) };
	}

	/**
	 * If the node is a copy of some other node, returns the original node.
	 * Otherwise returns the node itself.
	 */
	public IASTNode getOriginalNode() {
		IASTNode node = this;
		while (true) {
			IASTNodeLocation[] locations = node.getNodeLocations();
			if (locations.length == 0 || !(locations[0] instanceof IASTCopyLocation))
				break;
			node = ((IASTCopyLocation) locations[0]).getOriginalNode();
		}
		return node;
	}
}
