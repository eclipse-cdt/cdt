package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 */
public interface IBinaryModule extends IParent, IBinaryElement {

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	IBinaryElement[] getBinaryElements() throws CModelException;
}
