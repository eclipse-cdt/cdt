package org.eclipse.cdt.internal.core.dom;

public class TypeSpecifier {

	interface IOwner
	{
		public TypeSpecifier getTypeSpecifier(); 
		public void setTypeSpecifier( TypeSpecifier typespec );
	}

	public TypeSpecifier(IOwner owner) {
		this.owner = owner;
	}
	
	/**
	 * Owner declaration.
	 */
	private IOwner owner;
	
	/**
	 * Returns the declaration.
	 * @return SimpleDeclaration
	 */
	public IOwner getOwner() {
		return owner;
	}


}
