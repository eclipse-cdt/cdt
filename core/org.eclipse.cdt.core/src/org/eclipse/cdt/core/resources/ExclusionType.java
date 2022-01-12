/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.resources;

/**
 * Indicates the type of resources that this exclusion can exclude. Used to determine which type of icon is
 * displayed in the exclusion UI when this exclusion is present.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 *
 * @author crecoskie
 * @since 5.3
 *
 */
public enum ExclusionType {
	/**
	 * Constant indicating that this exclusion only excludes folders.
	 */
	FILE,

	/**
	 * Constant indicating that this exclusion only excludes folders.
	 */
	FOLDER,

	/**
	 * Constant indicating that this exclusion can exclude any resource.
	 */
	RESOURCE
}