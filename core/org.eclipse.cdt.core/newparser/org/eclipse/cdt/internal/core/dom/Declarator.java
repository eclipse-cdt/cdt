package org.eclipse.cdt.internal.core.dom;

public class Declarator {
	
	public Declarator(SimpleDeclaration declaration) {
		this.declaration = declaration;
	}
	
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

	private Name name;
	
	/**
	 * Returns the name.
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

}
