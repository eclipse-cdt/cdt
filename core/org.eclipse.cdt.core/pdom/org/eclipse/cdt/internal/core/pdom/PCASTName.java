package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.pdom.PDOMUnimplementedException;

public class PCASTName implements IASTName {

	public IBinding resolveBinding() {
		throw new PDOMUnimplementedException();
	}

	public IBinding getBinding() {
		throw new PDOMUnimplementedException();
	}

	public void setBinding(IBinding binding) {
		throw new PDOMUnimplementedException();
	}

	public IBinding[] resolvePrefix() {
		throw new PDOMUnimplementedException();
	}

	public char[] toCharArray() {
		throw new PDOMUnimplementedException();
	}

	public boolean isDeclaration() {
		throw new PDOMUnimplementedException();
	}

	public boolean isReference() {
		throw new PDOMUnimplementedException();
	}

	public boolean isDefinition() {
		throw new PDOMUnimplementedException();
	}

	public IASTTranslationUnit getTranslationUnit() {
		throw new PDOMUnimplementedException();
	}

	public IASTNodeLocation[] getNodeLocations() {
		throw new PDOMUnimplementedException();
	}

	public IASTFileLocation getFileLocation() {
		throw new PDOMUnimplementedException();
	}

	public String getContainingFilename() {
		throw new PDOMUnimplementedException();
	}

	public IASTNode getParent() {
		throw new PDOMUnimplementedException();
	}

	public void setParent(IASTNode node) {
		throw new PDOMUnimplementedException();
	}

	public ASTNodeProperty getPropertyInParent() {
		throw new PDOMUnimplementedException();
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		throw new PDOMUnimplementedException();
	}

	public boolean accept(ASTVisitor visitor) {
		throw new PDOMUnimplementedException();
	}

	public String getRawSignature() {
		throw new PDOMUnimplementedException();
	}

}
