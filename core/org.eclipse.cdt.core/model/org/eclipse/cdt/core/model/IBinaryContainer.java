package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represents a container of all the IBinary's found in the project
 * while inspecting the project.
 */
public interface IBinaryContainer extends ICElement, IParent, IOpenable {

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public IBinary[] getBinaries() throws CModelException;
}
