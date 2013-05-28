/*******************************************************************************
 * Copyright (c) 2013 Axel Mueller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Mueller - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;

/**
 * Helper class to determine whether a variable is accessed for reading and/or writing.
 * CPPMarkOccurrences is used to mark occurrences of a variable in the editor
 * and display read/write access.
 * The algorithm differs from VariableReadWriteFlags in case of pointers and arrays.
 * Where VariableReadWriteFlags explicitly checks read/write flags for the
 * variable itself (needed e.g. for refactoring), VariableMarkOccurrences treats pointers
 * as a compound of pointer and the value it points to. If one of them is changed then
 * the write flag is set.
 * <p>
 * The following examples are considered write access compared 
 * to the class VariableReadWriteFlags.
 * <pre>
 * struct A { 
 *   void m();
 *   int x; 
 * };
 * 
 * void test(A* ap) {
 *   ap->x = 1;  // write access for ap
 *   ap->m();    // write access for ap	
 *   (*ap).m();  // write access for ap
 *
 *   int * ip;
 *   *ip = 1;   // write access for ip
 *   ip[1] = 1; // write access for ip
 *   int b = ip[0];  // read access for ip
 *   
 *   int ipp[5][5];
 *   ipp[1][2] = 1; // write access for ipp
 *   **ipp = 1;     // write access for ipp
 * }
 * </pre> 
 */
public final class CPPMarkOccurrences extends CPPVariableReadWriteFlags {
	private static CPPMarkOccurrences INSTANCE = new CPPMarkOccurrences();
	

	public static int getReadWriteFlags(IASTName variable) {
		return INSTANCE.rwAnyNode(variable, 0);
	}

	@Override
	protected int rwInExpression(IASTExpression expr, IASTNode node, int indirection) {
		if (expr instanceof IASTIdExpression) {
			final IASTNode parent = expr.getParent(); 
			if (expr.getPropertyInParent() == IASTArraySubscriptExpression.ARRAY) {	
				if (indirection >= 0)	
					return rwAnyNode(parent, indirection);	
				return READ;
			}
		}
		return super.rwInExpression(expr, node, indirection);
	}
	
	@Override
	protected int rwInFieldReference(IASTNode node, IASTFieldReference expr, int indirection) {
		if (node.getPropertyInParent() == IASTFieldReference.FIELD_NAME) {
			if (expr.getPropertyInParent() != IASTFunctionCallExpression.FUNCTION_NAME)
				return rwAnyNode(expr, indirection);
		} else {  // IASTFieldReference.FIELD_OWNER
			if (expr.isPointerDereference() && indirection > 0)
				--indirection;
			if (indirection >= 0)
				return rwAnyNode(expr, indirection);
		}
		return READ;
	}
	
	@Override
	protected int rwInUnaryExpression(IASTNode node, IASTUnaryExpression expr, int indirection) {
		switch (expr.getOperator()) {
		case ICPPASTUnaryExpression.op_star:
			final IASTNode parent = expr.getParent();
			// handle multiple indirection **a and bracketed indirection (*ap)
			if (parent instanceof IASTUnaryExpression
					&& (((IASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_star || ((IASTUnaryExpression) parent)
							.getOperator() == IASTUnaryExpression.op_bracketedPrimary)) {
				++indirection;
			}
			if ( expr.getPropertyInParent() == IASTBinaryExpression.OPERAND_ONE){
				switch (((IASTBinaryExpression) parent).getOperator()) {
				case ICPPASTBinaryExpression.op_assign:
					return WRITE;
				case IASTBinaryExpression.op_binaryAndAssign:
				case IASTBinaryExpression.op_binaryOrAssign:
				case IASTBinaryExpression.op_binaryXorAssign:
				case IASTBinaryExpression.op_divideAssign:
				case IASTBinaryExpression.op_minusAssign: 
				case IASTBinaryExpression.op_moduloAssign: 
				case IASTBinaryExpression.op_multiplyAssign:
				case IASTBinaryExpression.op_plusAssign: 
				case IASTBinaryExpression.op_shiftLeftAssign: 
				case IASTBinaryExpression.op_shiftRightAssign:
					return READ | WRITE;
				}
			}
			if (indirection > 0) {
				return super.rwAnyNode(parent, indirection - 1);
			}
			return READ;
		}
		return super.rwInUnaryExpression(node, expr, indirection);
	}

	@Override
	protected int rwAssignmentToType(IType type, int indirection) {
		return super.rwAssignmentToType(type, indirection);
	}
}
