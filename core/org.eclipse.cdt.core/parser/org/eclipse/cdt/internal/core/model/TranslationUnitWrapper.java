package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TranslationUnitWrapper implements ICElementWrapper {
	
	TranslationUnit unit = null; 
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.IWrapper#getElement()
	 */
	public ICElement getElement() {
		return unit;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.model.IWrapper#setElement(java.lang.Object)
	 */
	public void setElement(ICElement item) {
		unit = (TranslationUnit)item; 
	}
	
	public TranslationUnitWrapper( )
	{
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.ICElementWrapper#getParent()
	 */
	public IParent getParent() {
		return null;
	}

}
