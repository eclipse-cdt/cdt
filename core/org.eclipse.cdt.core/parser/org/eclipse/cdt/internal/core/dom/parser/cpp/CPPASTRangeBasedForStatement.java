/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Range based for loop in c++.
 */
public class CPPASTRangeBasedForStatement extends ASTNode implements ICPPASTRangeBasedForStatement, IASTAmbiguityParent {
    private IScope fScope;
    private IASTDeclaration  fDeclaration;
    private IASTInitializerClause fInitClause;
    private IASTStatement fBody;
    private IASTImplicitName[] fImplicitNames;

    public CPPASTRangeBasedForStatement() {
	}

    @Override
	public CPPASTRangeBasedForStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}
    
	@Override
	public CPPASTRangeBasedForStatement copy(CopyStyle style) {
		CPPASTRangeBasedForStatement copy = new CPPASTRangeBasedForStatement();
		copy.setDeclaration(fDeclaration == null ? null : fDeclaration.copy(style));
		copy.setInitializerClause(fInitClause == null ? null : fInitClause.copy(style));
		copy.setBody(fBody == null ? null : fBody.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTDeclaration getDeclaration() {
        return fDeclaration;
    }

    @Override
	public void setDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
        this.fDeclaration = declaration;
        if (declaration != null) {
        	declaration.setParent(this);
        	declaration.setPropertyInParent(DECLARATION);
		}
    }

    @Override
	public IASTInitializerClause getInitializerClause() {
        return fInitClause;
    }

    @Override
	public void setInitializerClause(IASTInitializerClause initClause) {
        assertNotFrozen();
        fInitClause = initClause;
        if (initClause != null) {
			initClause.setParent(this);
			initClause.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public IASTStatement getBody() {
        return fBody;
    }

	@Override
	public void setBody(IASTStatement statement) {
		assertNotFrozen();
		fBody = statement;
		if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(BODY);
		}
	}

	@Override
	public IScope getScope() {
		if (fScope == null)
			fScope = new CPPBlockScope(this);
		return fScope;
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			IASTInitializerClause forInit = getInitializerClause();
			final ASTNode position = (ASTNode) forInit;
			if (forInit instanceof IASTExpression) {
				final IASTExpression forInitExpr = (IASTExpression) forInit;
				IType type= SemanticUtil.getNestedType(forInitExpr.getExpressionType(), TDEF|CVTYPE);
				if (type instanceof IArrayType) {
					fImplicitNames= IASTImplicitName.EMPTY_NAME_ARRAY;
				} else if (type instanceof ICPPClassType) {
					ICPPClassType ct= (ICPPClassType) type;
					if (CPPSemantics.findBindings(ct.getCompositeScope(), CPPVisitor.BEGIN_STR, true).length > 0) {
						CPPASTName name = new CPPASTName(CPPVisitor.BEGIN);
						name.setOffset(position.getOffset());
						CPPASTFieldReference fieldRef = new CPPASTFieldReference(name, forInitExpr.copy());
						IASTExpression expr= new CPPASTFunctionCallExpression(fieldRef, CPPVisitor.NO_ARGS);
						expr.setParent(this);
						expr.setPropertyInParent(ICPPASTRangeBasedForStatement.INITIALIZER);
						CPPASTImplicitName begin= new CPPASTImplicitName(name.toCharArray(), this);
						begin.setBinding(name.resolveBinding());
						begin.setOffsetAndLength(position);
						
						name = new CPPASTName(CPPVisitor.END);
						name.setOffset(position.getOffset());
						fieldRef.setFieldName(name);
						CPPASTImplicitName end= new CPPASTImplicitName(name.toCharArray(), this);
						end.setBinding(name.resolveBinding());
						end.setOffsetAndLength(position);
						
						fImplicitNames= new IASTImplicitName[] {begin, end};
					}
				}
			}
			if (fImplicitNames == null) {
				CPPASTName name = new CPPASTName(CPPVisitor.BEGIN);
				name.setOffset(position.getOffset());
				CPPASTIdExpression fname = new CPPASTIdExpression(name);
				IASTExpression expr= new CPPASTFunctionCallExpression(fname, new IASTInitializerClause[] {forInit.copy()});
				expr.setParent(this);
				expr.setPropertyInParent(ICPPASTRangeBasedForStatement.INITIALIZER);
				
				CPPASTImplicitName begin= new CPPASTImplicitName(name.toCharArray(), this);
				begin.setBinding(name.resolveBinding());
				begin.setOffsetAndLength(position);
				
				name = new CPPASTName(CPPVisitor.END);
				name.setOffset(position.getOffset());
				fname.setName(name);
				CPPASTImplicitName end= new CPPASTImplicitName(name.toCharArray(), this);
				end.setBinding(name.resolveBinding());
				end.setOffsetAndLength(position);
				
				fImplicitNames= new IASTImplicitName[] {begin, end};
			}
		}
		return fImplicitNames;
	}
	 
    @Override
	public boolean accept( ASTVisitor action ){
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if (fDeclaration != null && !fDeclaration.accept(action))
			return false;
		if (fInitClause != null && !fInitClause.accept(action))
			return false;
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;
		if (implicits != null) {
			for (IASTImplicitName implicit : implicits) {
				if (!implicit.accept(action))
					return false;
			}
		}

		if (fBody != null && !fBody.accept(action))
			return false;
        
		if (action.shouldVisitStatements && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		return true;
    }
    
	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fDeclaration) {
			setDeclaration((IASTDeclaration) other);
		} else if (child == fInitClause) {
			setInitializerClause((IASTInitializerClause) other);
		} else if (child == fBody) {
			setBody((IASTStatement) other);
		}
	}
}
