/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.textmanipulation;

/**
 * A text region describes a certain range in an <code>ITextBuffer</code>. A region is defined by 
 * its offset into the text buffer and its length.
 * <p>
 * A region is considered a value object. Its offset or length do not change over time. </p>
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */

// This class avoids contamination of clients with wrong imports.

public abstract class TextRegion {
	
	/**
	 * Returns the offset of the region.
	 *
	 * @return the offset of the region
	 */
	public abstract int getOffset();
	/**
	 * Returns the length of the region.
	 *
	 * @return the length of the region
	 */
	public abstract int getLength();
	
}