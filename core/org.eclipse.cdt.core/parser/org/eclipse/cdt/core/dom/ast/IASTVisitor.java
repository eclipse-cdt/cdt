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
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

/**
 * @author aniefer
 */
public interface IASTVisitor {
    public static abstract class BaseVisitorAction {
		public boolean processNames          = false;
		public boolean processDeclarations   = false;
		public boolean processInitializers   = false;
		public boolean processParameterDeclarations = false;
		public boolean processDeclarators    = false;
		public boolean processDeclSpecifiers = false;
		public boolean processExpressions    = false;
		public boolean processStatements     = false;
		public boolean processTypeIds        = false;
		public boolean processEnumerators    = false;

		/**
		 * @return continue to continue visiting, abort to stop, skip to not descend into this node. 
		 */
		public final static int PROCESS_SKIP     = 1;
		public final static int PROCESS_ABORT    = 2;
		public final static int PROCESS_CONTINUE = 3;
        
		
		public int processName( IASTName name ) 					{ return PROCESS_CONTINUE; }
		public int processDeclaration( IASTDeclaration declaration ){ return PROCESS_CONTINUE; }
		public int processInitializer( IASTInitializer initializer ){ return PROCESS_CONTINUE; }
		public int processParameterDeclaration( IASTParameterDeclaration parameterDeclaration ) { return PROCESS_CONTINUE; }
		public int processDeclarator( IASTDeclarator declarator )   { return PROCESS_CONTINUE; }
		public int processDeclSpecifier( IASTDeclSpecifier declSpec ){return PROCESS_CONTINUE; }
		public int processExpression( IASTExpression expression )   { return PROCESS_CONTINUE; }
		public int processStatement( IASTStatement statement )      { return PROCESS_CONTINUE; }
		public int processTypeId( IASTTypeId typeId )               { return PROCESS_CONTINUE; }
		public int processEnumerator( IASTEnumerator enumerator )   { return PROCESS_CONTINUE; }
    }
    
    public void visitTranslationUnit( BaseVisitorAction action );
    public boolean visitDeclaration( IASTDeclaration declaration, BaseVisitorAction action );
    public boolean visitName( IASTName name, BaseVisitorAction action );
    public boolean visitDeclSpecifier( IASTDeclSpecifier declSpecifier, BaseVisitorAction action );
    public boolean visitDeclarator( IASTDeclarator declarator, BaseVisitorAction action );
    public boolean visitStatement( IASTStatement statement, BaseVisitorAction action );
    public boolean visitExpression( IASTExpression expression, BaseVisitorAction action );
    public boolean visitTypeId( IASTTypeId typeId, BaseVisitorAction action );
    public boolean visitInitializer( IASTInitializer initializer, BaseVisitorAction action );
    public boolean visitEnumerator( IASTEnumerator enumerator, BaseVisitorAction action );
    public boolean visitParameterDeclaration( IASTParameterDeclaration parameterDeclaration, BaseVisitorAction action );
}