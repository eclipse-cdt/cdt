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
package org.eclipse.cdt.ui.resources;

import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.swt.widgets.Composite;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 *
 * @author crecoskie
 * @since 5.3
 *
 */
public abstract class RefreshExclusionContributor {

	protected String fID;
	protected boolean fIsTest;
	protected String fName;

	abstract public RefreshExclusion createExclusion();

	/**
	 * Creates the UI that allows user to modify the given RefreshExclusion
	 *
	 * @param parent
	 *            - the parent composite to contain the UI
	 * @param exclusion
	 *            - the RefreshExclusion to be modified
	 */
	abstract public void createProperiesUI(Composite parent, RefreshExclusion exclusion);

	public String getID() {
		return fID;
	}

	/**
	 * Returns the human-readable name of this exclusion type.
	 *
	 * @return String.
	 */
	public String getName() {
		return fName;
	}

	public boolean isTest() {
		return fIsTest;
	}

	public void setID(String id) {
		fID = id;
	}

	public void setIsTest(boolean isTest) {
		fIsTest = isTest;
	}

	public void setName(String name) {
		fName = name;
	}

}
