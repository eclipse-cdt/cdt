/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.preferences; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.AddContainerAction;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.DownAction;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.EditContainerAction;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.RemoveAction;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.SourceContainerAction;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.SourceContainerViewer;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.UpAction;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The "Source Lookup Path" preference page.
 */
public class SourcePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private SourceContainerViewer fPathViewer;
	private List fActions = new ArrayList(6);
	private IWorkbench fWorkbench;
	private AddContainerAction fAddAction; 
	private EditContainerAction fEditAction;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		Font font = parent.getFont();
		Composite comp = new Composite( parent, SWT.NONE );
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout( topLayout );
		GridData gd = new GridData( GridData.FILL_BOTH );
		comp.setLayoutData( gd );
		Label viewerLabel = new Label( comp, SWT.LEFT );
		viewerLabel.setText( PreferenceMessages.getString( "SourcePreferencePage.0" ) ); //$NON-NLS-1$
		gd = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
		gd.horizontalSpan = 2;
		viewerLabel.setLayoutData( gd );
		viewerLabel.setFont( font );
		fPathViewer = new SourceContainerViewer( comp );
		gd = new GridData( GridData.FILL_BOTH );
		fPathViewer.getControl().setLayoutData( gd );
		fPathViewer.getControl().setFont( font );
		Composite pathButtonComp = new Composite( comp, SWT.NONE );
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout( pathButtonLayout );
		gd = new GridData( GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL );
		pathButtonComp.setLayoutData( gd );
		pathButtonComp.setFont( font );
		createVerticalSpacer( comp, 2 );
		GC gc = new GC( parent );
		gc.setFont( parent.getFont() );
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		fAddAction = new AddContainerAction();
		Button button = createPushButton( pathButtonComp, fAddAction.getText(), fontMetrics );
		fAddAction.setButton( button );
		addAction( fAddAction );
		fEditAction = new EditContainerAction();
		button = createPushButton( pathButtonComp, fEditAction.getText(), fontMetrics );
		fEditAction.setButton( button );
		addAction( fEditAction );
		SourceContainerAction action = new RemoveAction();
		button = createPushButton( pathButtonComp, action.getText(), fontMetrics );
		action.setButton( button );
		addAction( action );
		action = new UpAction();
		button = createPushButton( pathButtonComp, action.getText(), fontMetrics );
		action.setButton( button );
		addAction( action );
		action = new DownAction();
		button = createPushButton( pathButtonComp, action.getText(), fontMetrics );
		action.setButton( button );
		addAction( action );
		retargetActions( fPathViewer );
		Dialog.applyDialogFont( comp );
		getWorkbench().getHelpSystem().setHelp( comp, ICDebugHelpContextIds.SOURCE_PREFERENCE_PAGE );
		initialize();
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init( IWorkbench workbench ) {
		fWorkbench = workbench;
	}

	private Button createPushButton( Composite parent, String label, FontMetrics fontMetrics ) {
		Button button = new Button( parent, SWT.PUSH );
		button.setFont( parent.getFont() );
		button.setText( label );
		GridData gd = getButtonGridData( button, fontMetrics );
		button.setLayoutData( gd );
		return button;
	}

	private GridData getButtonGridData( Button button, FontMetrics fontMetrics ) {
		GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING );
		int widthHint = Dialog.convertHorizontalDLUsToPixels( fontMetrics, IDialogConstants.BUTTON_WIDTH );
		gd.widthHint = Math.max( widthHint, button.computeSize( SWT.DEFAULT, SWT.DEFAULT, true ).x );
		return gd;
	}

	private IWorkbench getWorkbench() {
		return fWorkbench;
	}

	private void addAction( SourceContainerAction action ) {
		fActions.add( action );
	}

	private void retargetActions( SourceContainerViewer viewer ) {
		Iterator actions = fActions.iterator();
		while( actions.hasNext() ) {
			SourceContainerAction action = (SourceContainerAction)actions.next();
			action.setViewer( viewer );
		}
	}

	private void createVerticalSpacer( Composite comp, int colSpan ) {
		Label label = new Label( comp, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData( gd );
	}

	private void initialize() {
		ISourceLookupDirector director = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector();
		fPathViewer.setEntries( director.getSourceContainers() );
		fAddAction.setSourceLookupDirector( director );
		fEditAction.setSourceLookupDirector( director );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fPathViewer.setEntries( new ISourceContainer[0] );
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().setSourceContainers( fPathViewer.getEntries() );
		CDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}
}
