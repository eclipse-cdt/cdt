/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

/*
 * Created on Oct 22, 2004
 *
 */
package org.eclipse.cdt.internal.core.parser2;

/**
 * @author jcamelon
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IParserExtensionConfiguration {
	
	public boolean allowRestrictPointerOperatorsCPP();
	public boolean supportTypeofUnaryExpressionsCPP();
	public boolean supportAlignOfUnaryExpressionCPP();
	public boolean supportExtendedTemplateSyntaxCPP();
	public boolean supportMinAndMaxOperatorsCPP();
	public boolean supportStatementsInExpressions();
	public boolean supportGCCStyleDesignatorsC();

}
