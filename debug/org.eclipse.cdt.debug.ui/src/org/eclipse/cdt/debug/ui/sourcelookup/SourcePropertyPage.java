/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The "Source Lookup" property page.
 */
public class SourcePropertyPage extends PropertyPage {

	private SourceLookupBlock fBlock = null;

	/**
	 * Constructor for SourcePropertyPage.
	 */
	public SourcePropertyPage() {
		noDefaultAndApplyButton();
		fBlock = new SourceLookupBlock();
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent ) {
		ICDebugTarget target = getDebugTarget();
		if ( target == null || target.isTerminated() || target.isDisconnected() ) {
			return createTerminatedContents( parent );
		}
		return createActiveContents( parent );
	}

	protected Control createTerminatedContents( Composite parent ) {
		Label label = new Label( parent, SWT.LEFT );
		label.setText( SourceLookupMessages.getString( "SourcePropertyPage.0" ) ); //$NON-NLS-1$
		return label;
	}

	protected Control createActiveContents( Composite parent ) {
		fBlock.initialize( getLaunchConfiguration() );
		fBlock.createControl( parent );
		return fBlock.getControl();
	}

	protected ICDebugTarget getDebugTarget() {
		IAdaptable element = getElement();
		if ( element != null ) {
			return (ICDebugTarget)element.getAdapter( ICDebugTarget.class );
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if ( fBlock.isDirty() ) {
			try {
				setAttributes( fBlock );
			}
			catch( DebugException e ) {
				CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
				return false;
			}
		}
		return true;
	}

	private void setAttributes( SourceLookupBlock block ) throws DebugException {
		ICDebugTarget target = getDebugTarget();
		if ( target != null ) {
			if ( target.getLaunch().getSourceLocator() instanceof IAdaptable ) {
				ICSourceLocator locator = (ICSourceLocator)((IAdaptable)target.getLaunch().getSourceLocator()).getAdapter( ICSourceLocator.class );
				if ( locator != null ) {
					locator.setSourceLocations( block.getSourceLocations() );
					locator.setSearchForDuplicateFiles( block.searchForDuplicateFiles() );
					if ( target.getLaunch().getSourceLocator() instanceof IPersistableSourceLocator ) {
						ILaunchConfiguration configuration = target.getLaunch().getLaunchConfiguration();
						saveChanges( configuration, (IPersistableSourceLocator)target.getLaunch().getSourceLocator() );
					}
				}
			}
		}
	}

	protected void saveChanges( ILaunchConfiguration configuration, IPersistableSourceLocator locator ) {
		try {
			ILaunchConfigurationWorkingCopy copy = configuration.copy( configuration.getName() );
			copy.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			copy.doSave();
		}
		catch( CoreException e ) {
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
		}
	}

	private ILaunchConfiguration getLaunchConfiguration() {
		ICDebugTarget target = getDebugTarget();
		return (target != null) ? target.getLaunch().getLaunchConfiguration() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if ( fBlock != null )
			fBlock.dispose();
		super.dispose();
	}
}