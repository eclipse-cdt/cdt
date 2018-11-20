/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.build;

/**
 * @since 6.5
 */
public interface ICBuildConfigurationManager2 {

	/**
	 * Re-evaluate disabled configs to see if they should be re-enabled.
	 */
	public void recheckConfigs();

}
