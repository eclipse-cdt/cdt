/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - Modified from MakefileEditor to support Automake files
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutomakeEditorPreferencePage;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


public class AutomakeEditor extends MakefileEditor {
	
	protected AutomakefileContentOutlinePage ampage;
	private AutomakefileSourceConfiguration sourceViewerConfiguration;
	private static AutomakeEditor fgInstance;
	private IEditorInput input;
	
	static {
		fgInstance = new AutomakeEditor();
	}
	
    public AutomakeEditor() {
    	super();
    }

    /**
     * Returns the default editor instance.
     *
     * @return the default editor instance
     */
    public static AutomakeEditor getDefault() {
    	return fgInstance;
    }

    protected void doSetInput(IEditorInput newInput) throws CoreException
	{
		super.doSetInput(newInput);
		this.input = newInput;

		getOutlinePage().setInput(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#MakefileEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#MakefileRulerContext"); //$NON-NLS-1$
		setDocumentProvider(AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider());
		IPreferenceStore[] stores = new IPreferenceStore[2];
		stores[0] = AutotoolsPlugin.getDefault().getPreferenceStore();
		stores[1] = EditorsUI.getPreferenceStore();
		ChainedPreferenceStore chainedStore = new ChainedPreferenceStore(stores);
		setPreferenceStore(chainedStore);
		sourceViewerConfiguration = new AutomakefileSourceConfiguration(chainedStore, this);
		setSourceViewerConfiguration(sourceViewerConfiguration);
		AutotoolsEditorPreferenceConstants.initializeDefaultValues(stores[0]);
		AutomakeEditorPreferencePage.initDefaults(stores[0]);
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}
	
	public AutomakeDocumentProvider getAutomakefileDocumentProvider() {
		return (AutomakeDocumentProvider) AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider();
	}
	
	public AutomakefileContentOutlinePage getAutomakeOutlinePage() {
		if (ampage == null) {
			ampage = new AutomakefileContentOutlinePage(this);
			ampage.addSelectionChangedListener(this);
			ampage.setInput(getEditorInput());
		}
		return ampage;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAdaptable
	 */
	public Object getAdapter(@SuppressWarnings("unchecked") Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return getAutomakeOutlinePage();
		}
		return super.getAdapter(key);
	}
	
	public AutomakefileSourceConfiguration getAutomakeSourceViewerConfiguration() {
		return sourceViewerConfiguration;
	}
	
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		super.handlePreferenceStoreChanged(event);
	}
	
	public IMakefile getMakefile() {
		return getAutomakefileDocumentProvider().getWorkingCopy(this.getEditorInput());
	}
	
	public ISourceViewer getAutomakeSourceViewer() {
		return getSourceViewer();
	}
	
}
