package org.eclipse.rse.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * @author coulthar
 *
 * Constants used throughout the User Defined Action framework.
 */
public interface ISystemUDAConstants {
	/**
	 * The name of the xml tag for domain tags.
	 * Domains are used to partition actions. 
	 * Eg, you might have "Folder" and "File" domains
	 *   or for iSeries "Object" and "Member" domains
	 */
	public static final String XE_DOMAIN = "Domain"; //$NON-NLS-1$
	/**
	 * The name of the xml attribute of domain tags which
	 *  identifies the domain type. Its values will be
	 *  an untranslated name like "Object" or "Folder".
	 */
	public static final String XE_DOMTYPE = "Type"; //$NON-NLS-1$
	/**
	 * The name of the xml attribute of domain tags which
	 *  identifies the domain name. Its values will be
	 *  a translated name like "Object" or "Folder".
	 */
	public static final String XE_DOMNAME = "Name"; //$NON-NLS-1$
	/**
	 * The name of the attribute we consistently use to store an element's name
	 */
	public static final String NAME_ATTR = "Name"; //$NON-NLS-1$
	/**
	 * The name of the attribute we consistently use to store an element's original IBM-supplied name
	 */
	public static final String ORIGINAL_NAME_ATTR = "OriginalName"; //$NON-NLS-1$
	/**
	 * The name of the attribute we consistently use to store a release number
	 */
	public static final String RELEASE_ATTR = "release"; //$NON-NLS-1$
}
