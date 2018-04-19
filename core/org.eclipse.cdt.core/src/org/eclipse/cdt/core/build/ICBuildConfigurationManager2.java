/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
