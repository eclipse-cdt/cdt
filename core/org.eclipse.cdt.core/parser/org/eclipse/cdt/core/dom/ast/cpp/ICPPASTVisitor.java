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
 * Created on Feb 22, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * @author aniefer
 */
public interface ICPPASTVisitor extends IASTVisitor {

	public static abstract class CPPBaseVisitorAction extends BaseVisitorAction{
		public boolean processBaseSpecifiers = false;
		public boolean processNamespaces     = false;
		public boolean processTemplateParameters = false;
		
		public int processBaseSpecifier(ICPPASTBaseSpecifier specifier) { return PROCESS_CONTINUE; }
		public int processNamespace( ICPPASTNamespaceDefinition namespace) { return PROCESS_CONTINUE; }
		public int processTemplateParameter( ICPPASTTemplateParameter parameter) { return PROCESS_CONTINUE; }
	}
	

    public boolean visitNamespaceDefinition( ICPPASTNamespaceDefinition namespace, BaseVisitorAction action );
    public abstract boolean visitBaseSpecifier( ICPPASTBaseSpecifier specifier, BaseVisitorAction action );
    public abstract boolean visitTemplateParameter( ICPPASTTemplateParameter parameter, BaseVisitorAction action );
}
