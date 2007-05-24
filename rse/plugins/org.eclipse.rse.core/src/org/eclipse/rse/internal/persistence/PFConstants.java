/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.persistence;

interface PFConstants {

	static final String PROPERTIES_FILE_NAME = "node.properties"; //$NON-NLS-1$

	/* 
	 * Metatype names 
	 * each entry is an array. The first is the preferred name.
	 * The other names are acceptable alternates.
	 * Names must not contain periods or whitespace.
	 * Lowercase letters, numbers and dashes (-) are preferred.
	 */
	static final String[] MT_ATTRIBUTE_TYPE = new String[] { "04-attr-type", "attr-type" }; //$NON-NLS-1$ //$NON-NLS-2$
	static final String[] MT_ATTRIBUTE = new String[] { "03-attr", "attr" }; //$NON-NLS-1$ //$NON-NLS-2$
	static final String[] MT_CHILD = new String[] { "06-child", "child" }; //$NON-NLS-1$ //$NON-NLS-2$
	static final String[] MT_NODE_TYPE = new String[] { "01-type", "01-node-type", "n-type" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	static final String[] MT_NODE_NAME = new String[] { "00-name", "00-node-name", "n-name" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	static final String[] MT_REFERENCE = new String[] { "05-ref", "ref" }; //$NON-NLS-1$ //$NON-NLS-2$

	/* Type abbreviations */
	static final String AB_SUBSYSTEM = "SS"; //$NON-NLS-1$
	static final String AB_SERVICE_LAUNCHER = "SL"; //$NON-NLS-1$
	static final String AB_PROPERTY_SET = "PS"; //$NON-NLS-1$
	static final String AB_PROPERTY = "P"; //$NON-NLS-1$
	static final String AB_HOST = "H"; //$NON-NLS-1$
	static final String AB_FILTER_STRING = "FS"; //$NON-NLS-1$
	static final String AB_FILTER_POOL_REFERENCE = "FPR"; //$NON-NLS-1$
	static final String AB_FILTER_POOL = "FP"; //$NON-NLS-1$
	static final String AB_FILTER = "F"; //$NON-NLS-1$
	static final String AB_CONNECTOR_SERVICE = "CS"; //$NON-NLS-1$
	static final String AB_PROFILE = "PRF"; //$NON-NLS-1$

}
