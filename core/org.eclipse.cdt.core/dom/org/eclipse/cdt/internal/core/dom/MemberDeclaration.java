package org.eclipse.cdt.internal.core.dom;

/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MemberDeclaration {
	public static final int t_private = 0;
	public static final int t_protected = 1;
	public static final int t_public = 2;
	
	public MemberDeclaration(int access, Declaration declaration) {
		this.access = access;
		this.declaration = declaration;
	}
	
	private int access;
	public int getAccess() { return access; }
	
	private Declaration declaration;
	public Declaration getDeclaration() { return declaration; }
}
