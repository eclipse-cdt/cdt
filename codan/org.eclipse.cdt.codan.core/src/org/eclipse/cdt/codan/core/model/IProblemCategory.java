/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Problem category. Allows to group problems.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemCategory extends IProblemElement {
	/**
	 * Category name
	 * 
	 * @return category name
	 */
	String getName();

	/**
	 * Unique category id
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Category children (other categories or problems)
	 * 
	 * @return children of the category
	 */
	IProblemElement[] getChildren();
}
