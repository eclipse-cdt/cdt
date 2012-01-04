/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug fix (326670)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences.DisassemblyPreferenceConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * DisassemblyView
 */
public class DisassemblyView extends DisassemblyPart implements IViewPart {

	/**
	 * 
	 */
	public DisassemblyView() {
		super();
	}

	@Override
	protected IActionBars getActionBars() {
		return getViewSite().getActionBars();
	}

	/*
	 * @see org.eclipse.ui.IViewPart#getViewSite()
	 */
	@Override
	public IViewSite getViewSite() {
		return (IViewSite)getSite();
	}

	/*
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
	}

	/*
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
        if (memento != null) {
            Boolean trackExpression = memento.getBoolean(DisassemblyPreferenceConstants.TRACK_EXPRESSION);
            if (trackExpression != null)
                fTrackExpression = trackExpression;
            Boolean syncContext = memento.getBoolean(DisassemblyPreferenceConstants.SYNC_ACTIVE_CONTEXT);
            if (syncContext != null)
                fSynchWithActiveDebugContext = syncContext;
        }
	}

	/*
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putBoolean(DisassemblyPreferenceConstants.TRACK_EXPRESSION, isTrackExpression());
		memento.putBoolean(DisassemblyPreferenceConstants.SYNC_ACTIVE_CONTEXT, isSyncWithActiveDebugContext());
	}

	@Override
	protected void contributeToActionBars(IActionBars bars) {
		super.contributeToActionBars(bars);
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(fGlobalActions.get(ActionFactory.FIND.getId()));
        manager.add(new Separator("group.goto")); // ICommonMenuConstants.GROUP_GOTO //$NON-NLS-1$
		manager.add(fActionGotoPC);
		manager.add(fActionGotoAddress);
        manager.add(new Separator("group.show")); // ICommonMenuConstants.GROUP_SHOW //$NON-NLS-1$
        manager.add(fSyncAction);
		manager.add(fTrackExpressionAction);
        manager.add(new Separator(ITextEditorActionConstants.GROUP_SETTINGS));
		manager.add(fActionToggleSource);		
        manager.add(fActionToggleSymbols);
        manager.add(fActionOpenPreferences);
        // Other plug-ins can contribute their actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void closePart() {
		getViewSite().getPage().hideView(this);
	}
}
