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
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.util;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author Emanuel Graf IFS
 */
public class OffsetHelper {
	
	public static int getOffsetIncludingComment(IASTNode node) {
		int nodeStart = Integer.MAX_VALUE;
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
		if (nodeLocations.length != 1) {
			int offset;
			for (IASTNodeLocation location : nodeLocations) {
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					offset = macroLoc.asFileLocation().getNodeOffset();
				} else {
					offset = location.asFileLocation().getNodeOffset();
				}
				if (offset < nodeStart)
					nodeStart = offset;
			}
		} else {
			nodeStart = node.getFileLocation().getNodeOffset();
		}
		
		return nodeStart;
	}
	
	public static int getEndOffsetIncludingComments(IASTNode node) {
		int fileOffset = 0;
		int length = 0;
		
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
		if (nodeLocations.length != 1) {
			for (IASTNodeLocation location : nodeLocations) {
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					fileOffset = macroLoc.asFileLocation().getNodeOffset();
					length = macroLoc.asFileLocation().getNodeLength();
				} else {
					fileOffset = location.asFileLocation().getNodeOffset();
					length = location.asFileLocation().getNodeLength();
				}
			}
		} else {
			IASTFileLocation loc = node.getFileLocation();
			fileOffset = loc.getNodeOffset();
			length = loc.getNodeLength();
		}
		return fileOffset + length;
		
	}

	public static int getEndOffsetWithoutComments(IASTNode node) {
		return node.getFileLocation().getNodeOffset() + node.getFileLocation().getNodeLength();
	}
	
	public static int getLengthIncludingComment(IASTNode node) {
		return getEndOffsetIncludingComments(node) - getOffsetIncludingComment(node);
	}

	public static int getNodeOffset(ASTNode node) {
		return node.getOffset();
	}

	public static int getNodeEndPoint(ASTNode node) {
		return node.getOffset() + node.getLength();
	}

	public static int getStartingLineNumber(IASTNode node) {
		return node.getFileLocation().getStartingLineNumber();
	}

	public static int getEndingLineNumber(IASTNode node) {
		return node.getFileLocation().getEndingLineNumber();
	}
}
