package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * A C project represents a view of a project resource in terms of C 
 * elements such as ICFile, ICFolder ....
 * <code>CCore.create(project)</code>.
 * </p>
 *
 * @see CCore#create(org.eclipse.core.resources.IProject)
 * @see IBuildEntry
 */
public interface ICProject extends IParent, ICElement {

	/**
	 * Returns the <code>ICElement</code> corresponding to the given
	 * path, or <code>null</code> if no such 
	 * <code>ICElement</code> is found.
	 *
	 * @exception CModelException if the given path is <code>null</code>
	 *  or absolute
	 */
	ICElement findElement(IPath path) throws CModelException;

	/**
	 * Return the ArchiveContainer of this Project.
	 */
	IArchiveContainer getArchiveContainer();

	/**
	 * Return the BinaryContainer of this Project.
	 */
	IBinaryContainer getBinaryContainer();

	IProject getProject();
}
