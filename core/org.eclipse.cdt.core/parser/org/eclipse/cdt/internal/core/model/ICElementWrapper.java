package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IParent;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface ICElementWrapper {

	public IParent getElement();
	public void setElement (IParent item);
}
