package org.eclipse.cdt.internal.core.dom;

public class TypeSpecifier {

	public TypeSpecifier(SimpleDeclaration declaration) {
		this.declaration = declaration;
	}
	
	/**
	 * Owner declaration.
	 */
	private SimpleDeclaration declaration;
	
	/**
	 * Returns the declaration.
	 * @return SimpleDeclaration
	 */
	public SimpleDeclaration getDeclaration() {
		return declaration;
	}

	/**
	 * Sets the declaration.
	 * @param declaration The declaration to set
	 */
	public void setDeclaration(SimpleDeclaration declaration) {
		this.declaration = declaration;
	}

}
