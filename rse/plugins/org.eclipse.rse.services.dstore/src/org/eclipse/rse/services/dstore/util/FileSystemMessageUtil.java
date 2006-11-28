/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.services.dstore.util;

import org.eclipse.dstore.core.model.DataElement;

public class FileSystemMessageUtil 
{

	/**
	 * Returns the source message (first part of the source attribute) for this element.
	 *
	 * @return the source message
	 */
	public static String getSourceMessage(DataElement element)
	{
		String source = element.getSource();
		if (source == null) return null;
		if (source.equals("")) return ""; //$NON-NLS-1$ //$NON-NLS-2$
		int sepIndex = source.indexOf("|"); //$NON-NLS-1$
		if (sepIndex == -1) return source;
		else return source.substring(0, sepIndex);
	}

	/**
	 * Returns the source location (second part of the source attribute) for this element.
	 *
	 * @return the source location
	 */
	public static String getSourceLocation(DataElement element)
	{
		String source = element.getSource();
		if (source == null) return null;
		if (source.equals("")) return ""; //$NON-NLS-1$ //$NON-NLS-2$
		int sepIndex = source.indexOf("|"); //$NON-NLS-1$
		if (sepIndex == -1) return ""; //$NON-NLS-1$
		else return source.substring(sepIndex+1);
	}
}
