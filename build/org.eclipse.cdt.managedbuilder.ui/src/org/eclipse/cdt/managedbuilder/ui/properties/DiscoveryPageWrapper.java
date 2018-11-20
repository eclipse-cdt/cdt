/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryOptionsBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Composite;

/**
 * This class is created to support backward compatibility
 * with make.ui discovery pages.
 * It is subclassed from AbstractDiscoveryOptionsBlock
 * just to be passed to existing discovery pages.
 * All methods referenced by these pages are rewritten.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DiscoveryPageWrapper extends AbstractDiscoveryOptionsBlock {

	ICPropertyProvider page = null;
	IBuildInfoContainer container = null;

	public DiscoveryPageWrapper(ICPropertyProvider _page, IBuildInfoContainer c) {
		super(AbstractCPropertyTab.EMPTY_STR);
		page = _page;
		container = c;
	}

	@Override
	public IScannerConfigBuilderInfo2 getBuildInfo() {
		return container.getBuildInfo();
	}

	@Override
	public IProject getProject() {
		return page.getProject();
	}

	@Override
	public String getErrorMessage() {
		return AbstractCPropertyTab.EMPTY_STR;
	}

	@Override
	public Preferences getPrefs() {
		return page.getPreferences();
	}

	@Override
	public boolean isProfileDifferentThenPersisted() {
		return true;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean checkDialogForChanges() {
		return true;
	}

	@Override
	public void callPerformApply() {
	}

	@Override
	public void setInitialized(boolean initialized) {
	}

	@Override
	public void setContainer(ICOptionContainer container) {
	}

	@Override
	public void updateContainer() {
	}

	@Override
	public void updatePersistedProfile() {
	}

	@Override
	public void setVisible(boolean visible) {
	}

	@Override
	protected String getCurrentProfileId() {
		return null;
	}

	@Override
	public void createControl(Composite parent) {
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void performDefaults() {
	}
}
