/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import org.eclipse.core.resources.IProject;

/**
 * Extension for the ICOptionContainer to be used with new project wizards.
 * This allows children to access the the project handle.
 * @since 4.0
 */
public interface ICOptionContainerExtension extends ICOptionContainer {
	/**
	 * Returns the project to be created.
	 * @since 4.0
	 */
	IProject getProjectHandle();
}
