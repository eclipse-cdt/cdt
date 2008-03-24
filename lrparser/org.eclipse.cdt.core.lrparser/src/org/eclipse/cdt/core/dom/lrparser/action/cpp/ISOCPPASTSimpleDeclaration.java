package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

/**
 * TODO this functionality should be moved into CPPASTSimpleDeclaration
 * @author Mike Kucera
 *
 */
@Deprecated
public class ISOCPPASTSimpleDeclaration extends CPPASTSimpleDeclaration implements IASTAmbiguityParent {

	public ISOCPPASTSimpleDeclaration() {
	}

	public ISOCPPASTSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		super(declSpecifier);
	}

	public void replace(IASTNode child, IASTNode other) {
		IASTDeclarator[] declarators = getDeclarators();
		for(int i = 0; i < declarators.length; i++) {
			if(declarators[i] == child) {
				declarators[i] = (IASTDeclarator)other;
				other.setParent(child.getParent());
	            other.setPropertyInParent(child.getPropertyInParent());
				break;
			}
		}
	}

}
