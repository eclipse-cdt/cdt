/*
 * Created on Apr 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IAccessable {
	/**
	 * @return int
	 */
	public abstract int getVisibility();
	/**
	 * Sets the currentVisiblity.
	 * @param currentVisiblity The currentVisiblity to set
	 */
	public abstract void setVisibility(int currentVisiblity);
}