/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;

/**
 * @since 8.6
 */
public interface IOptionalBuildObjectPropertiesContainer {
	IOptionalBuildProperties getOptionalBuildProperties();

}
