package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.internal.core.parser.util.AccessSpecifier;

/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MemberDeclaration {
	
	public MemberDeclaration(int access, Declaration declaration) {
		this.access.setAccess( access );
		this.declaration = declaration;
	}
	
	private AccessSpecifier access = new AccessSpecifier();
	public int getAccess() { return access.getAccess(); }
	
	private Declaration declaration;
	public Declaration getDeclaration() { return declaration; }
}
