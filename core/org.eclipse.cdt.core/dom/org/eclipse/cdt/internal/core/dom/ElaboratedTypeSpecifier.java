package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable 
"typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ElaboratedTypeSpecifier extends TypeSpecifier {

	public static final int t_class = 0;
	public static final int t_struct = 1;
	public static final int t_union = 2;
	public static final int t_enum = 3;

	private final int classKey;
	public int getClassKey() { return classKey; }


	/**
	 * @see org.eclipse.cdt.internal.core.dom.TypeSpecifier#getDeclaration()
	 */
	public SimpleDeclaration getDeclaration() {
		return super.getDeclaration();
	}

	/**
	 * @see org.eclipse.cdt.internal.core.dom.TypeSpecifier#setDeclaration(org.eclipse.cdt.internal.core.dom.SimpleDeclaration)
	 */
	public void setDeclaration(SimpleDeclaration declaration) {
		super.setDeclaration(declaration);
	}
	
	public ElaboratedTypeSpecifier(int classKey, SimpleDeclaration declaration) {
		super(declaration);
		this.classKey = classKey;
	}

	private Name name;
	public void setName(Name n) { name = n; }
	public Name getName() { return name; }
	

}
