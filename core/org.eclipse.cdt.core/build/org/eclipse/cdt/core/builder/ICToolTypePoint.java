/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder;

import org.eclipse.cdt.core.builder.model.ICToolType;

/**
 * Interface representing an instance of
 * a CToolType extension point.
 * <p>
 * This interface exists solely to parallel the
 * other extension point interfaces (ICToolPoint, etc.)
 */
public interface ICToolTypePoint extends ICToolType {
}
