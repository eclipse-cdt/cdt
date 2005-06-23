/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.search;

import org.eclipse.cdt.core.search.BasicSearchMatch;

public interface IIndexSearchRequestor {
 
	void acceptSearchMatch(BasicSearchMatch match);

	void acceptIncludeDeclaration(String path, char[] decodedSimpleName);

}
