/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of initializer nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see IASTInitializer
 * @author Emanuel Graf IFS
 */
public class InitializerWriter extends NodeWriter{

	public InitializerWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}
	
	protected void writeInitializer(IASTInitializer initializer) {
		if (initializer instanceof IASTEqualsInitializer) {
			writeEqualsInitializer((IASTEqualsInitializer) initializer);
		} else if (initializer instanceof IASTInitializerList) {
			writeInitializerList((IASTInitializerList) initializer);
		} else if (initializer instanceof ICPPASTConstructorInitializer) {
			writeConstructorInitializer((ICPPASTConstructorInitializer) initializer);
		} else if (initializer instanceof ICASTDesignatedInitializer) {
			writeDesignatedInitializer((ICASTDesignatedInitializer) initializer);
		} else if (initializer instanceof ICPPASTConstructorChainInitializer) {
			writeConstructorChainInitializer((ICPPASTConstructorChainInitializer) initializer);
		}

		writeTrailingComments(initializer, false);
	}
	
	private void writeEqualsInitializer(IASTEqualsInitializer initializer) {
		scribe.print(EQUALS);
		IASTInitializerClause init = initializer.getInitializerClause();
		if (init != null) {
			init.accept(visitor);
		}
	}

	private void writeConstructorChainInitializer(ICPPASTConstructorChainInitializer initializer) {
		initializer.getMemberInitializerId().accept(visitor);
		initializer.getInitializer().accept(visitor);
	}

	private void writeInitializerList(IASTInitializerList initList) {
		scribe.printLBrace();
		IASTInitializerClause[] inits = initList.getClauses();
		writeNodeList(inits);
		scribe.printRBrace();
	}

	private void writeConstructorInitializer(ICPPASTConstructorInitializer ctorInit) {
		scribe.print('(');
		writeNodeList(ctorInit.getArguments());
		scribe.print(')');		
	}

	private void writeDesignatedInitializer(ICASTDesignatedInitializer desigInit) {
		ICASTDesignator[] designators =  desigInit.getDesignators();
		for (ICASTDesignator designator : designators) {
			writeDesignator(designator);
		}
		scribe.print(EQUALS);
		desigInit.getOperand().accept(visitor);
	}

	private void writeDesignator(ICASTDesignator designator) {
		if (designator instanceof ICASTFieldDesignator) {
			ICASTFieldDesignator fieldDes = (ICASTFieldDesignator) designator;
			scribe.print('.');
			fieldDes.getName().accept(visitor);
		} else if (designator instanceof ICASTArrayDesignator) {
			ICASTArrayDesignator arrDes = (ICASTArrayDesignator) designator;
			scribe.print('[');
			arrDes.getSubscriptExpression().accept(visitor);
			scribe.print(']');
		} else if (designator instanceof IGCCASTArrayRangeDesignator) {
			//IGCCASTArrayRangeDesignator new_name = (IGCCASTArrayRangeDesignator) designator;
			//TODO IGCCASTArrayRangeDesignator Bespiel zu parsen bringen
			throw new UnsupportedOperationException("Writing of GCC ArrayRangeDesignator is not yet implemented"); //$NON-NLS-1$
		}
	}
}
