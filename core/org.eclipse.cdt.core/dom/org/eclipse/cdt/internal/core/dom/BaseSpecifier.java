package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.internal.core.parser.util.Name;

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
			case ClassSpecifier.t_class:
				access = t_private;
				break;
			case ClassSpecifier.t_struct:
				access = t_public;
				break;
		}
	}
	
	private ClassSpecifier classSpecifier;
	public ClassSpecifier getClassSpecifier() { return classSpecifier; }

	private boolean isVirtual = false;
	public void setVirtual(boolean isVirtual) { this.isVirtual = isVirtual; }
	public boolean isVirtual() { return isVirtual; }
	
	public static final int t_private = 0;
	public static final int t_protected = 1;
	public static final int t_public = 2;
	private int access;
	public void setAccess(int access) { this.access = access; }
	public int getAccess() { return access; }
	
	private Name name;
	public void setName(Name name) { this.name = name; }
	public Name getName() { return name; }
}
