package org.eclipse.cdt.internal.core.dom;

/**
 * @author dschaefe
 */
public class Declarator {

	private Name name;
	
	public void setName(Name name) {
		this.name = name;
	}
	
	public Name getName() {
		return name;
	}
	
}
