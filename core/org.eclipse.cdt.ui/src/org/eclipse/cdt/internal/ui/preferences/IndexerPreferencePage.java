/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.CacheSizeBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

public class IndexerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, ICOptionContainer {
	// bug 217860, allow to hide build configuration
	private static final String SHOW_BUILD_SPECIFIC_CONFIG = "show.build.specific.indexer.config"; //$NON-NLS-1$

	private final IndexerBlock fOptionBlock;
	private final CacheSizeBlock fCacheBlock;
	private final IndexerStrategyBlock fStrategyBlock;
	
	public IndexerPreferencePage(){
		fOptionBlock = new IndexerBlock();
		fOptionBlock.setContainer(this);
		fStrategyBlock= new IndexerStrategyBlock(this);
		fCacheBlock= new CacheSizeBlock(this);
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.INDEXER_PREFERENCE_PAGE);
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout gl;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gl= new GridLayout());
		composite.setLayoutData(new GridData());
		gl.verticalSpacing= 0;
	
		fOptionBlock.createControl(composite);
		fStrategyBlock.createControl(composite);
		fCacheBlock.createControl(composite);
		
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void updateContainer() {
		if (!fOptionBlock.isValid()) {
			setErrorMessage(fOptionBlock.getErrorMessage());
			setValid(false);
		}
		else if (!fStrategyBlock.isValid()) {
			setErrorMessage(fStrategyBlock.getErrorMessage());
			setValid(false);
		}
		else if (!fCacheBlock.isValid()) {
			setErrorMessage(fCacheBlock.getErrorMessage());
			setValid(false);
		}
		else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	@SuppressWarnings("deprecation")
	public org.eclipse.core.runtime.Preferences getPreferences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean performOk() {
		try {
			fOptionBlock.performApply(new NullProgressMonitor());
			fStrategyBlock.performApply(new NullProgressMonitor());
			fCacheBlock.performApply(new NullProgressMonitor());
		} catch (CoreException e) {}
		return true;
	}

	@Override
	public void performDefaults() {
		fOptionBlock.performDefaults();
		fStrategyBlock.performDefaults();
		fCacheBlock.performDefaults();
		updateContainer();
	}

	/**
	 * Returns whether the capability for showing build configurations is enabled.
	 * @since 5.0
	 */
	public static boolean showBuildConfiguration() {
		IWorkbenchActivitySupport activitySupport= PlatformUI.getWorkbench().getActivitySupport();
		IIdentifier identifier= activitySupport.getActivityManager().getIdentifier(
				CUIPlugin.getPluginId() + '/' + SHOW_BUILD_SPECIFIC_CONFIG);
		return identifier.isEnabled();
	}
}
