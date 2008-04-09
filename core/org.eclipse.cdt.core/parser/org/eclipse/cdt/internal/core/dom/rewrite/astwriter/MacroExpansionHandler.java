/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * 
 * Recognizes nodes that are the result of an macro expansion and replaces them 
 * with a suitable macro call.
 * @author Emanuel Graf IFS
 *
 */
public class MacroExpansionHandler {
	
	private int lastMacroExpOffset;
	private final Scribe scribe;

	public MacroExpansionHandler(Scribe scribe) {
		this.scribe = scribe;
	}

	protected boolean checkisMacroExpansionNode(IASTNode node) {
		return checkisMacroExpansionNode(node, true);
	}

	protected boolean isStatementWithMixedLocation(IASTStatement node) {
		if(node.getNodeLocations().length > 1) {
			for (IASTNodeLocation loc : node.getNodeLocations()) {
				if (loc instanceof IASTMacroExpansionLocation) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean macroExpansionAlreadyPrinted(IASTNode node) {
		IASTNodeLocation[] locs = node.getNodeLocations();
		if(locs.length ==1) {
			if (locs[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];
				if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean checkisMacroExpansionNode(IASTNode node, boolean write) {
		IASTNodeLocation[] locs = node.getNodeLocations();
		if(locs.length ==1) {
			if (locs[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];
	
				if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
					return true;
				}
				if (write) {
					lastMacroExpOffset = macroNode.asFileLocation().getNodeOffset();
					scribe.print(node.getRawSignature());
				}
				return true;

			}
		}
		return false;
	}
	
	public void reset(){
		lastMacroExpOffset = -1;
	}

}
