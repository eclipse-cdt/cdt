/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.internal.core.parser.TokenDuple;
import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTASMDefinition;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTCompilationUnit;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTLinkageSpecification;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTScope;

/**
 * @author jcamelon
 *
 */
public interface IASTFactory {
	public abstract IASTUsingDirective createUsingDirective(
		IASTScope scope,
		TokenDuple duple)
		throws Backtrack;
	public abstract IASTASMDefinition createASMDefinition(
		IASTScope scope,
		String assembly,
		int first,
		int last);
	public abstract IASTNamespaceDefinition createNamespaceDefinition(
		int first,
		String identifier,
		int nameOffset);
	public abstract IASTCompilationUnit createCompilationUnit();
	public abstract IASTLinkageSpecification createLinkageSpecification(String spec);
}