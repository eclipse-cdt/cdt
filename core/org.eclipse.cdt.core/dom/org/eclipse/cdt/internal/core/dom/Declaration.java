package org.eclipse.cdt.internal.core.dom;

/**
 */
public class Declaration {
	
	public Declaration( IScope scope )
	{
		ownerScope = scope;
	}
	
	private final IScope ownerScope;

	/**
	 * @return
	 */
	public IScope getOwnerScope() {
		return ownerScope;
	}

}
