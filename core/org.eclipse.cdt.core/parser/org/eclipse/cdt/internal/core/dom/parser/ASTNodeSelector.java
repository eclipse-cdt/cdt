/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeSpecification.Relation;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;

/**
 * Class to support searching for nodes by file offsets.
 * @since 5.0
 */
public class ASTNodeSelector implements IASTNodeSelector {
	private ASTTranslationUnit fTu;
	private ILocationResolver fLocationResolver;
	private String fFilePath;
	private final boolean fIsValid;

	public ASTNodeSelector(ASTTranslationUnit tu, ILocationResolver locationResolver, String filePath) {
		fTu= tu;
		fLocationResolver= locationResolver;
		fFilePath= filePath;
		fIsValid= verify();
	}

	private boolean verify() {
		if (fLocationResolver != null) {
			if (fFilePath == null) {
				fFilePath= fLocationResolver.getTranslationUnitPath();
			}
			return true;
		}
		return false;
	}

	private <T extends IASTNode> T findNode(int offsetInFile, int lengthInFile, Relation relation, Class<T> requiredClass) {
		return findNode(offsetInFile, lengthInFile, relation, requiredClass, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getNode(int, int)
	 */
	private <T extends IASTNode> T findNode(int offsetInFile, int lengthInFile, Relation relation, 
			Class<T> requiredClass, boolean searchInExpansion) {
		if (!fIsValid) {
			return null;
		}
		if (lengthInFile < 0) {
			throw new IllegalArgumentException("Length cannot be less than zero."); //$NON-NLS-1$
		}
		int sequenceLength;
		int altSequenceNumber= -1;
		int sequenceNumber= fLocationResolver.getSequenceNumberForFileOffset(fFilePath, offsetInFile);
    	if (sequenceNumber < 0) {
    		return null;
    	}
		if (lengthInFile > 0) {
			sequenceLength= fLocationResolver.getSequenceNumberForFileOffset(fFilePath, offsetInFile+lengthInFile-1) + 1 - sequenceNumber;
		} else {
			sequenceLength= 0;
			if (offsetInFile > 0) {
				altSequenceNumber= fLocationResolver.getSequenceNumberForFileOffset(fFilePath, offsetInFile-1);
				if (altSequenceNumber + 1 == sequenceNumber) {
					altSequenceNumber= -1;
				} else {
					// we are on a context boundary and we need to check the variant to the left and
					// the one to the right
 					sequenceLength= 1;
				}
			}
		}
		final ASTNodeSpecification<T> nodeSpec= new ASTNodeSpecification<T>(relation, requiredClass, offsetInFile, lengthInFile);
		nodeSpec.setRangeInSequence(sequenceNumber, sequenceLength, false);
		nodeSpec.setSearchInExpansion(searchInExpansion);
    	getNode(nodeSpec);
    	if (altSequenceNumber != -1) {
    		nodeSpec.setRangeInSequence(altSequenceNumber, sequenceLength, true);
        	getNode(nodeSpec);
    	}
    	return nodeSpec.getBestNode();
	}

	private <T extends IASTNode> T getNode(ASTNodeSpecification<T> nodeSpec) {
		fLocationResolver.findPreprocessorNode(nodeSpec);
    	if (!nodeSpec.requiresClass(IASTPreprocessorMacroExpansion.class)) {
    		// adjust sequence number for search in the expansion of macros
    		int seqbegin= nodeSpec.getSequenceStart();
    		int seqend= nodeSpec.getSequenceEnd();
    		IASTPreprocessorMacroExpansion expansion= nodeSpec.findLeadingMacroExpansion(this);
    		if (expansion != null) {
    			IASTFileLocation floc= expansion.getFileLocation();
    			seqbegin= fLocationResolver.getSequenceNumberForFileOffset(fFilePath, floc.getNodeOffset() + floc.getNodeLength()-1)+1;
    		}
    		expansion= nodeSpec.findTrailingMacroExpansion(this);
    		if (expansion != null) {
    			IASTFileLocation floc= expansion.getFileLocation();
    			seqend= fLocationResolver.getSequenceNumberForFileOffset(fFilePath, floc.getNodeOffset() + floc.getNodeLength());
    		}
    		nodeSpec.setRangeInSequence(seqbegin, seqend-seqbegin);
    		
    		FindNodeForOffsetAction nodeFinder= new FindNodeForOffsetAction(nodeSpec);
    		fTu.accept(nodeFinder);
    	}
		return nodeSpec.getBestNode();
	}

	@Override
	public IASTNode findFirstContainedNode(int offset, int length) {
		return findNode(offset, length, Relation.FIRST_CONTAINED, IASTNode.class);
	}

	@Override
	public IASTNode findNode(int offset, int length) {
		return findNode(offset, length, Relation.EXACT_MATCH, IASTNode.class);
	}

	@Override
	public IASTNode findEnclosingNode(int offset, int length) {
		return findNode(offset, length, Relation.ENCLOSING, IASTNode.class);
	}
	
	@Override
	public IASTNode findStrictlyEnclosingNode(int offset, int length) {
		return findNode(offset, length, Relation.STRICTLY_ENCLOSING, IASTNode.class);
	}

	@Override
	public IASTNode findFirstContainedNodeInExpansion(int offset, int length) {
		return findNode(offset, length, Relation.FIRST_CONTAINED, IASTNode.class, true);
	}

	@Override
	public IASTNode findNodeInExpansion(int offset, int length) {
		return findNode(offset, length, Relation.EXACT_MATCH, IASTNode.class, true);
	}

	@Override
	public IASTNode findEnclosingNodeInExpansion(int offset, int length) {
		return findNode(offset, length, Relation.ENCLOSING, IASTNode.class, true);
	}

	@Override
	public IASTName findFirstContainedName(int offset, int length) {
		return findNode(offset, length, Relation.FIRST_CONTAINED, IASTName.class);
	}

	@Override
	public IASTName findName(int offset, int length) {
		return findNode(offset, length, Relation.EXACT_MATCH, IASTName.class);
	}

	@Override
	public IASTName findEnclosingName(int offset, int length) {
		return findNode(offset, length, Relation.ENCLOSING, IASTName.class);
	}

	@Override
	public IASTImplicitName findImplicitName(int offset, int length) {
		return findNode(offset, length, Relation.EXACT_MATCH, IASTImplicitName.class);
	}
	
	@Override
	public IASTImplicitName findEnclosingImplicitName(int offset, int length) {
		return findNode(offset, length, Relation.ENCLOSING, IASTImplicitName.class);
	}
	
	@Override
	public IASTPreprocessorMacroExpansion findEnclosingMacroExpansion(int offset, int length) {
		return findNode(offset, length, Relation.ENCLOSING, IASTPreprocessorMacroExpansion.class);
	}
}