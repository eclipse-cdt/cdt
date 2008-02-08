package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Interface to model using directives
 * @since 5.0
 */
public interface ICPPUsingDirective {

	/**
	 * Returns the scope of the namespace that is nominated by this
	 * directive.
	 */
	ICPPNamespace getNamespace() throws DOMException;

	/**
	 * Returns the point of declaration as global offset ({@link ASTNode#getOffset()}).
	 */
	int getPointOfDeclaration();
}
