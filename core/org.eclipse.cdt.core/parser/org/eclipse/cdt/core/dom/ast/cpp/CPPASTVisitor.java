/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Mar 8, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;


public abstract class CPPASTVisitor extends ASTVisitor{
	public boolean shouldVisitBaseSpecifiers = false;
	public boolean shouldVisitNamespaces     = false;
	public boolean shouldVisitTemplateParameters = false;
	
	public int visit( ICPPASTBaseSpecifier specifier ) 		{ return PROCESS_CONTINUE; }
	public int visit( ICPPASTNamespaceDefinition namespace ){ return PROCESS_CONTINUE; }
	public int visit( ICPPASTTemplateParameter parameter ) 	{ return PROCESS_CONTINUE; }
}