/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;

public abstract class AbstractAutotoolsCPropertyTab extends
		AbstractCBuildPropertyTab {

	public boolean isIndexerAffected() {
		return false;
	}
	
}
