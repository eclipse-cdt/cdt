/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.dom.ast.IASTMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author jcamelon
 */
public interface ILocationResolver {
    
	public IASTMacroDefinition [] getMacroDefinitions(IASTNode parent);
	public IASTPreprocessorIncludeStatement [] getIncludeDirectives(IASTNode parent);
	public IASTPreprocessorStatement [] getAllPreprocessorStatements(IASTNode parent);

	public IASTNodeLocation [] getLocations( int offset, int length );
    public IASTNodeLocation    getLocation( int offset );
    public IProblem [] getScannerProblems();

    public String getTranslationUnitPath();
    public String [] getInclusionsPaths();
    
    public void cleanup();
    
}
