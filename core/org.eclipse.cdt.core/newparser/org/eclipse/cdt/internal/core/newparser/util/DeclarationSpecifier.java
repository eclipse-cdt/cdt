package org.eclipse.cdt.internal.core.newparser.util;

import java.util.List;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DeclarationSpecifier extends DeclSpecifier {
	
	public interface Container {
	
		public DeclarationSpecifier getDeclSpecifier();
	
		public void setDeclSpecifier( DeclarationSpecifier in );
	
		public void addDeclarator(Object declarator);
		public List getDeclarators();

	};
}
