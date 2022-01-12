/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTArrayRangeDesignator;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of initializer nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 *
 * @see Scribe
 * @see IASTInitializer
 * @author Emanuel Graf IFS
 */
public class InitializerWriter extends NodeWriter {

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
		} else if (initializer instanceof ICPPASTDesignatedInitializer) {
			writeDesignatedInitializer((ICPPASTDesignatedInitializer) initializer);
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
		ICASTDesignator[] designators = desigInit.getDesignators();
		for (ICASTDesignator designator : designators) {
			writeDesignator(designator);
		}
		scribe.print(EQUALS);
		desigInit.getOperand().accept(visitor);
	}

	private void writeDesignatedInitializer(ICPPASTDesignatedInitializer desigInit) {
		ICPPASTDesignator[] designators = desigInit.getDesignators();
		for (ICPPASTDesignator designator : designators) {
			writeDesignator(designator);
		}
		scribe.print(EQUALS);
		desigInit.getOperand().accept(visitor);
	}

	private void writeDesignator(ICASTDesignator designator) {
		if (designator instanceof ICASTFieldDesignator) {
			scribe.print('.');
			ICASTFieldDesignator fieldDes = (ICASTFieldDesignator) designator;
			fieldDes.getName().accept(visitor);
		} else if (designator instanceof ICASTArrayDesignator) {
			scribe.print('[');
			ICASTArrayDesignator arrDes = (ICASTArrayDesignator) designator;
			arrDes.getSubscriptExpression().accept(visitor);
			scribe.print(']');
		} else if (designator instanceof IGCCASTArrayRangeDesignator) {
			scribe.print('[');
			IGCCASTArrayRangeDesignator arrDes = (IGCCASTArrayRangeDesignator) designator;
			arrDes.getRangeFloor().accept(visitor);
			scribe.print(" ... "); //$NON-NLS-1$
			arrDes.getRangeCeiling().accept(visitor);
			scribe.print(']');
		}
	}

	private void writeDesignator(ICPPASTDesignator designator) {
		if (designator instanceof ICPPASTFieldDesignator) {
			scribe.print('.');
			ICPPASTFieldDesignator fieldDes = (ICPPASTFieldDesignator) designator;
			fieldDes.getName().accept(visitor);
		} else if (designator instanceof ICPPASTArrayDesignator) {
			scribe.print('[');
			ICPPASTArrayDesignator arrDes = (ICPPASTArrayDesignator) designator;
			arrDes.getSubscriptExpression().accept(visitor);
			scribe.print(']');
		} else if (designator instanceof IGPPASTArrayRangeDesignator) {
			scribe.print('[');
			IGPPASTArrayRangeDesignator arrDes = (IGPPASTArrayRangeDesignator) designator;
			arrDes.getRangeFloor().accept(visitor);
			scribe.print(" ... "); //$NON-NLS-1$
			arrDes.getRangeCeiling().accept(visitor);
			scribe.print(']');
		}
	}
}
