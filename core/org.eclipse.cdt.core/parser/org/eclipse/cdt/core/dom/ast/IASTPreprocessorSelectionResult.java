/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface is used to specify what the LocationMap has found when searching for IASTNodes
 * corresponding to a selection from the preprocessor tree.
 * 
 * @author dsteffle
 */
public interface IASTPreprocessorSelectionResult {
	public IASTNode getSelectedNode();
	public void setSelectedNode(IASTNode selectedNode);
	public int getGlobalOffset();
	public void setGlobalOffset(int globalOffset);
}
