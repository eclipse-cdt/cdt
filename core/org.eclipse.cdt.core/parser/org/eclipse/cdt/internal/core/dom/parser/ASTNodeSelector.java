/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeMatchKind.Relation;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;

/**
 * Class to support searching for nodes by offsets.
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getNode(int, int)
	 */
	private IASTNode getNode(int offset, int length, ASTNodeMatchKind matchKind) {
		if (!fIsValid) {
			return null;
		}
		
    	final int sequenceNumber= fLocationResolver.getSequenceNumberForFileOffset(fFilePath, offset);
    	if (sequenceNumber < 0) {
    		return null;
    	}
    	final int sequenceLength= length <= 0 ? 0 : 
    		fLocationResolver.getSequenceNumberForFileOffset(fFilePath, offset+length-1) + 1 - sequenceNumber; 

    	ASTNode preCand= searchPreprocessor(sequenceNumber, sequenceLength, matchKind);
    	if (preCand != null && matchKind.getRelationToSelection() != Relation.FIRST_CONTAINED) {
    		return preCand;
    	}
    	ASTNode astCand= searchAST(sequenceNumber, sequenceLength, matchKind);
		return matchKind.isBetterMatch(preCand, astCand) ? preCand : astCand;
	}

	private ASTNode searchPreprocessor(int sequenceNumber, int sequenceLength,	ASTNodeMatchKind matchKind) {
		return fLocationResolver.findPreprocessorNode(sequenceNumber, sequenceLength, matchKind);
	}

	private ASTNode searchAST(int sequenceNumber, int length, ASTNodeMatchKind matchKind) {
		FindNodeForOffsetAction nodeFinder= new FindNodeForOffsetAction(sequenceNumber, length, matchKind);
		fTu.accept(nodeFinder);
		ASTNode result= nodeFinder.getNode();
		// don't accept matches from the ast enclosed in a macro expansion (possible for contained matches, only)
		if (result != null &&
				matchKind.getRelationToSelection() == Relation.FIRST_CONTAINED) {
			IASTNodeLocation[] loc= result.getNodeLocations();
			if (loc.length > 0 && loc[0] instanceof IASTMacroExpansionLocation) {
				return null;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getFirstContainedNode(int, int)
	 */
	public IASTNode findFirstContainedNode(int offset, int length) {
		return getNode(offset, length, ASTNodeMatchKind.MATCH_FIRST_CONTAINED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getNode(int, int)
	 */
	public IASTNode findNode(int offset, int length) {
		return getNode(offset, length, ASTNodeMatchKind.MATCH_EXACT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getSurroundingNode(int, int)
	 */
	public IASTNode findSurroundingNode(int offset, int length) {
		return getNode(offset, length, ASTNodeMatchKind.MATCH_SURROUNDING);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getFirstContainedNode(int, int)
	 */
	public IASTName findFirstContainedName(int offset, int length) {
		return (IASTName) getNode(offset, length, ASTNodeMatchKind.MATCH_FIRST_NAME_CONTAINED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getNode(int, int)
	 */
	public IASTName findName(int offset, int length) {
		return (IASTName) getNode(offset, length, ASTNodeMatchKind.MATCH_EXACT_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeSelector#getSurroundingNode(int, int)
	 */
	public IASTName findSurroundingName(int offset, int length) {
		return (IASTName) getNode(offset, length, ASTNodeMatchKind.MATCH_SURROUNDING_NAME);
	}

}