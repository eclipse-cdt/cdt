package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

public class CPPASTStructuredBindingDeclaration extends CPPASTSimpleDeclaration
		implements ICPPASTStructuredBindingDeclaration {
	private RefQualifier refQualifier;
	private IASTName[] names;
	private IASTInitializer initializer;

	public CPPASTStructuredBindingDeclaration() {
	}

	public CPPASTStructuredBindingDeclaration(ICPPASTSimpleDeclSpecifier declSpecifier, RefQualifier refQualifier,
			IASTName[] names, IASTInitializer initializer) {
		super(declSpecifier);
		this.refQualifier = refQualifier;
		for (IASTName name : names) {
			addName(name);
		}

		setInitializer(initializer);
	}

	@Override
	public RefQualifier getRefQualifier() {
		return refQualifier;
	}

	public void setRefQualifier(RefQualifier refQualifier) {
		assertNotFrozen();
		this.refQualifier = refQualifier;
	}

	@Override
	public IASTName[] getNames() {
		if (names == null)
			return IASTName.EMPTY_NAME_ARRAY;
		names = ArrayUtil.trim(names);
		return names;
	}

	@Override
	public IASTInitializer getInitializer() {
		return initializer;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		
		IASTDeclSpecifier declSpecifier = getDeclSpecifier();
		if (declSpecifier  != null && !declSpecifier.accept(action)) {
			return false;
		}

		for (IASTName name : getNames()) {
			if (!name.accept(action)) {
				return false;
			}
		}

		if (initializer != null && !initializer.accept(action)) {
			return false;
		}

		if (action.shouldVisitDeclarations) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}
	
	protected void addName(IASTName name) {
		assertNotFrozen();
		if (name != null) {
			names = ArrayUtil.append(IASTName.class, names, name);
			name.setParent(this);
			name.setPropertyInParent(IDENTIFIER);
		}
	}

	protected void setInitializer(IASTInitializer initializer) {
		assertNotFrozen();
		this.initializer = initializer;
		initializer.setParent(this);
		//TODO: initializer	.setPropertyInParent();
	}

	@Override
	public CPPASTStructuredBindingDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTStructuredBindingDeclaration copy(CopyStyle style) {
		CPPASTStructuredBindingDeclaration copy = new CPPASTStructuredBindingDeclaration();

		copy.setRefQualifier(refQualifier);
		copy.setInitializer(initializer.copy(style));

		for (IASTName name : names) {
			if (name == null) {
				break;
			}
			copy.addName(name.copy(style));
		}

		return copy(copy, style); //TODO: Wrong, implement copyFrom in super class
	}

	@Override
	public int getRoleForName(IASTName name) {
		return r_definition;
	}
}
