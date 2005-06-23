/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * C++ specific visitor class.
 * 
 * @author jcamelon
 */
public abstract class CPPASTVisitor extends ASTVisitor {

	/**
	 * Overide this value if you wish to visit base specifiers off composite
	 * types.
	 */
	public boolean shouldVisitBaseSpecifiers = false;

	/**
	 * Overide this value if you wish to visit namespaces.
	 */
	public boolean shouldVisitNamespaces = false;

	/**
	 * Overide this value if you wish to visit template parameters.
	 */
	public boolean shouldVisitTemplateParameters = false;

	/**
	 * Visit BaseSpecifiers.
	 * 
	 * @param specifier
	 * @return
	 */
	public int visit(ICPPASTBaseSpecifier specifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * Visit namespace definitions.
	 * 
	 * @param namespace
	 * @return
	 */
	public int visit(ICPPASTNamespaceDefinition namespace) {
		return PROCESS_CONTINUE;
	}

	/**
	 * Visit template parameter.
	 * 
	 * @param parameter
	 * @return
	 */
	public int visit(ICPPASTTemplateParameter parameter) {
		return PROCESS_CONTINUE;
	}
}
