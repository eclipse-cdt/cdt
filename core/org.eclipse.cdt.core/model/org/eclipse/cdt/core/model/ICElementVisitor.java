package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.CoreException;

/*
 * (c) Copyright IBM Corp. 2004.
 * All Rights Reserved.
 */

/**
 * This interface is implemented by clients that walk the ICElement tree.
 */
public interface ICElementVisitor {

	/**
	 * Visited a member if the ICElement tree. Returns whether to visit the children
	 * of this element.
	 * 
	 * @param element
	 * @return
	 */
	public boolean visit(ICElement element) throws CoreException;
	
}
