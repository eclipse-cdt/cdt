package org.eclipse.cdt.internal.core.dom;


/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BaseSpecifier {
	
	public BaseSpecifier(ClassSpecifier classSpecifier) {
		this.classSpecifier = classSpecifier;
		classSpecifier.addBaseSpecifier(this);
		
		switch (classSpecifier.getClassKey()) {
			case ClassKey.t_class:
				access.setAccess(AccessSpecifier.v_private);
				break;
			case ClassKey.t_struct:
			default:
				access.setAccess(AccessSpecifier.v_public);
				break;
		}
	}
	
	private ClassSpecifier classSpecifier;
	public ClassSpecifier getClassSpecifier() { return classSpecifier; }

	private boolean isVirtual = false;
	public void setVirtual(boolean isVirtual) { this.isVirtual = isVirtual; }
	public boolean isVirtual() { return isVirtual; }
	

	private AccessSpecifier access = new AccessSpecifier( AccessSpecifier.v_unknown );
	public void setAccess(int access) { this.access.setAccess(access); }
	public int getAccess() { return access.getAccess(); }
	
	private Name name;
	public void setName(Name name) { this.name = name; }
	public Name getName() { return name; }
}
