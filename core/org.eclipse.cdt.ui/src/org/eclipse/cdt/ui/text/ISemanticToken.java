package org.eclipse.cdt.ui.text;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * An interface for accessing details of the token that is being highlighted.
 *
 * @since 5.6
 */
public interface ISemanticToken {
	/**
	 * @return Returns the binding, can be <code>null</code>.
	 */
	public IBinding getBinding();

	/**
	 * @return the AST node
	 */
	public IASTNode getNode();

	/**
	 * @return the AST root
	 */
	public IASTTranslationUnit getRoot();
}
