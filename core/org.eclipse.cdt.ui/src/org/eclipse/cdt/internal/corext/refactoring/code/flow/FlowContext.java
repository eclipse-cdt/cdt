/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class FlowContext extends LocalVariableIndex {
	private static class ComputeMode {
	}

	public static final ComputeMode MERGE = new ComputeMode();
	public static final ComputeMode ARGUMENTS = new ComputeMode();
	public static final ComputeMode RETURN_VALUES = new ComputeMode();

	private boolean fConsiderAccessMode;
	private boolean fLoopReentranceMode;
	private ComputeMode fComputeMode;
	private IVariable[] fLocals;
	private final List<ICPPASTCatchHandler[]> fExceptionStack;

	public FlowContext(IASTFunctionDefinition functionDefinition) {
		super(functionDefinition);
		fExceptionStack= new ArrayList<ICPPASTCatchHandler[]>(3);
	}

	public void setConsiderAccessMode(boolean b) {
		fConsiderAccessMode= b;
	}

	public void setComputeMode(ComputeMode mode) {
		fComputeMode= mode;
	}

	void setLoopReentranceMode(boolean b) {
		fLoopReentranceMode= b;
	}

	int getArrayLength() {
		return getNumLocalVariables();
	}

	boolean considerAccessMode() {
		return fConsiderAccessMode;
	}

	boolean isLoopReentranceMode() {
		return fLoopReentranceMode;
	}

	boolean computeMerge() {
		return fComputeMode == MERGE;
	}

	boolean computeArguments() {
		return fComputeMode == ARGUMENTS;
	}

	boolean computeReturnValues() {
		return fComputeMode == RETURN_VALUES;
	}

	public IVariable getLocalFromIndex(int index) {
		if (fLocals == null || index > fLocals.length)
			return null;
		return fLocals[index];
	}

	/**
	 * Adds a local variable to the context.
	 * @param localVariable the local variable to manage.
	 */
	void manageLocal(IVariable localVariable) {
		int index = getIndexFromLocal(localVariable);
		if (index >= 0) {
			if (fLocals == null)
				fLocals= new IVariable[getNumLocalVariables()];
			fLocals[index] = localVariable;
		}
	}

	//---- Exception handling --------------------------------------------------------

	void pushExceptions(ICPPASTTryBlockStatement node) {
		ICPPASTCatchHandler[] catchHandlers = node.getCatchHandlers();
		fExceptionStack.add(catchHandlers);
	}

	void popExceptions() {
		Assert.isTrue(fExceptionStack.size() > 0);
		fExceptionStack.remove(fExceptionStack.size() - 1);
	}

	boolean isExceptionCaught(IType exceptionType) {
		for (ICPPASTCatchHandler[] catchHandlers : fExceptionStack) {
			for (ICPPASTCatchHandler catchHandler : catchHandlers) {
				if (catchHandler.isCatchAll())
					return true;
				IASTDeclaration caughtException= catchHandler.getDeclaration();
				if (caughtException instanceof IASTSimpleDeclaration) {
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration) caughtException).getDeclarators();
					IType caughtType = CPPVisitor.createType(declarators[0]);
					while (caughtType != null) {
						// 15.3
						if (caughtType.isSameType(exceptionType))
							return true;
						// TODO(sprigogin): Implement the rest of 15.3 matching logic. 
					}
				}
			}
		}
		return false;
	}
}
