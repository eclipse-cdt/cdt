package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


/**
 * A C Folder Resource.
 */
public interface ICContainer extends ICElement, IParent, IOpenable {
	/**
	 * Returns an array of non-C resources directly contained in this project.
	 * It does not transitively answer non-C resources contained in folders;
	 * these would have to be explicitly iterated over.
	 * <p>
	 * Non-C resources includes files, folders, projects  not accounted for.
	 * </p>
	 * 
	 * @return an array of non-C resources directly contained in this project
	 * @exception JavaModelException if this element does not exist or if an
	 *              exception occurs while accessing its corresponding resource
	 */
	Object[] getNonCResources() throws CModelException;

}
