/*******************************************************************************
 * Copyright (c) 2013 Sebastian Bauer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Bauer - Initial API and implementation
 ******************************************************************************/

package org.eclipse.cdt.core.dom.ast;

import java.util.List;

/**
 * This interface represents a special doxygen comment. It additionally
 * provides access to the tags that occur in these kind of comments.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @author Sebastian Bauer
 */
public interface IASTDoxygenComment extends IASTComment {
	/**
	 * Returns the list of tag elements which this comment covers.
	 *
	 * @return the list
	 */
	List<?extends IASTDoxygenTag> tags();
}
