/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer, Wind River Systems, Inc.
 *     Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.ResourceBundle;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.internal.autotools.ui.MakeUIMessages;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class MakefileEditor extends TextEditor implements ISelectionChangedListener, IReconcilingParticipant {

	/**
	 * The page that shows the outline.
	 */
	protected MakefileContentOutlinePage page;
	ProjectionSupport projectionSupport;
	ProjectionMakefileUpdater fProjectionMakefileUpdater;
	private FindReplaceDocumentAdapter fFindReplaceDocumentAdapter;

	/**
	 * Reconciling listeners.
	 * @since 3.0
	 */
	private ListenerList<IReconcilingParticipant> fReconcilingListeners = new ListenerList<>(ListenerList.IDENTITY);

	MakefileSourceConfiguration getMakefileSourceConfiguration() {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (configuration instanceof MakefileSourceConfiguration) {
			return (MakefileSourceConfiguration) configuration;
		}
		return null;
	}

	public MakefileContentOutlinePage getOutlinePage() {
		if (page == null) {
			page = new MakefileContentOutlinePage(this);
			page.addSelectionChangedListener(this);
			page.setInput(getEditorInput());
		}
		return page;
	}

	public MakefileEditor() {
		super();
	}

	@Override
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
		setSourceViewerConfiguration(new MakefileSourceConfiguration(chainedStore, this));
	}

	@Override
	public void dispose() {
		if (fProjectionMakefileUpdater != null) {
			fProjectionMakefileUpdater.uninstall();
			fProjectionMakefileUpdater = null;
		}
		super.dispose();
	}

	boolean isFoldingEnabled() {
		return AutotoolsPlugin.getDefault().getPreferenceStore()
				.getBoolean(MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED);
	}

	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		// always false for Makefiles
		// see http://bugs.eclipse.org/186106
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		projectionSupport.install();

		if (isFoldingEnabled()) {
			projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		}

		//		ProjectionAnnotationModel model= (ProjectionAnnotationModel) getAdapter(ProjectionAnnotationModel.class);

		fProjectionMakefileUpdater = new ProjectionMakefileUpdater();
		if (fProjectionMakefileUpdater != null) {
			fProjectionMakefileUpdater.install(this, projectionViewer);
			fProjectionMakefileUpdater.initialize();
		}
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
				styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> key) {
		if (ProjectionAnnotationModel.class.equals(key)) {
			if (projectionSupport != null) {
				Object result = projectionSupport.getAdapter(getSourceViewer(), key);
				if (result != null) {
					return (T) result;
				}
			}
		} else if (key.equals(IContentOutlinePage.class)) {
			return (T) getOutlinePage();
		}
		return super.getAdapter(key);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (page != null) {
			page.update();
		}
	}

	@Override
	protected void createActions() {
		super.createActions();

		ResourceBundle bundle = MakeUIMessages.getResourceBundle();

		IAction a = new TextOperationAction(bundle, "ContentAssistProposal.", this, //$NON-NLS-1$
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); //$NON-NLS-1$

		a = new TextOperationAction(bundle, "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a); //$NON-NLS-1$

		a = new TextOperationAction(bundle, "Comment.", this, ITextOperationTarget.PREFIX); //$NON-NLS-1$
		a.setActionDefinitionId(IMakefileEditorActionDefinitionIds.COMMENT);
		setAction("Comment", a); //$NON-NLS-1$
		markAsStateDependentAction("Comment", true); //$NON-NLS-1$

		a = new TextOperationAction(bundle, "Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
		a.setActionDefinitionId(IMakefileEditorActionDefinitionIds.UNCOMMENT);
		setAction("Uncomment", a); //$NON-NLS-1$
		markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$

		a = new OpenDeclarationAction(this);
		a.setActionDefinitionId(IMakefileEditorActionDefinitionIds.OPEN_DECLARATION);
		setAction("OpenDeclarationAction", a); //$NON-NLS-1$
		markAsStateDependentAction("OpenDeclarationAction", true); //$NON-NLS-1$

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			resetHighlightRange();
		} else if (selection instanceof IStructuredSelection) {
			if (!isActivePart() && AutotoolsUIPlugin.getActivePage() != null) {
				AutotoolsUIPlugin.getActivePage().bringToTop(this);
			}
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IDirective) {
				IDirective statement = (IDirective) element;
				setSelection(statement, !isActivePart());
			}
		}
	}

	/**
	 * Returns whether the editor is active.
	 */
	private boolean isActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		IWorkbenchPart part = service.getActivePart();
		return part != null && part.equals(this);
	}

	/**
	 * Returns the find/replace document adapter.
	 *
	 * @return the find/replace document adapter.
	 */
	private FindReplaceDocumentAdapter getFindRepalceDocumentAdapter() {
		if (fFindReplaceDocumentAdapter == null) {
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			fFindReplaceDocumentAdapter = new FindReplaceDocumentAdapter(doc);
		}
		return fFindReplaceDocumentAdapter;
	}

	public void setSelection(IDirective directive, boolean moveCursor) {
		int startLine = directive.getStartLine() - 1;
		int endLine = directive.getEndLine() - 1;
		try {
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			int start = doc.getLineOffset(startLine);
			int len = doc.getLineLength(endLine) - 1;
			int length = (doc.getLineOffset(endLine) + len) - start;
			setHighlightRange(start, length, true);
			if (moveCursor) {
				// Let see if we can move the cursor at the position also
				String var = directive.toString().trim();
				IWordDetector detector = new MakefileWordDetector();
				for (len = 0; len < var.length(); len++) {
					char c = var.charAt(len);
					//if (! (Character.isLetterOrDigit(c) || c == '.' || c == '_')) {
					if (!(detector.isWordPart(c) || detector.isWordStart(c) || c == '-' || c == '_')) {
						break;
					}
				}
				if (len > 0) {
					var = var.substring(0, len);
				}
				IRegion region = getFindRepalceDocumentAdapter().find(start, var, true, true, true, false);

				if (region != null) {
					len = region.getOffset();
					length = region.getLength();
					getSourceViewer().revealRange(len, length);
					// Selected region begins one index after offset
					getSourceViewer().setSelectedRange(len, length);
				}

			}
		} catch (IllegalArgumentException x) {
			resetHighlightRange();
		} catch (BadLocationException e) {
			resetHighlightRange();
		}
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Comment"); //$NON-NLS-1$
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Uncomment"); //$NON-NLS-1$
		//addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "OpenDeclarationAction"); //$NON-NLS-1$
	}

	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener	The reconcile listener to be added
	 * @since 3.0
	 */
	final void addReconcilingParticipant(IReconcilingParticipant listener) {
		synchronized (fReconcilingListeners) {
			fReconcilingListeners.add(listener);
		}
	}

	/**
	 * Removes the given listener.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener	the reconcile listener to be removed
	 * @since 3.0
	 */
	final void removeReconcilingParticipant(IReconcilingParticipant listener) {
		synchronized (fReconcilingListeners) {
			fReconcilingListeners.remove(listener);
		}
	}

	@Override
	public void reconciled() {
		// Notify listeners
		for (IReconcilingParticipant listener : fReconcilingListeners) {
			listener.reconciled();
		}
	}

	@Override
	protected void performRevert() {
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionViewer.setRedraw(false);
		try {

			boolean projectionMode = projectionViewer.isProjectionMode();
			if (projectionMode) {
				projectionViewer.disableProjection();
				if (fProjectionMakefileUpdater != null)
					fProjectionMakefileUpdater.uninstall();
			}

			super.performRevert();

			if (projectionMode) {
				if (fProjectionMakefileUpdater != null)
					fProjectionMakefileUpdater.install(this, projectionViewer);
				projectionViewer.enableProjection();
			}

		} finally {
			projectionViewer.setRedraw(true);
		}
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer == null)
			return;

		String property = event.getProperty();

		MakefileSourceConfiguration makeConf = getMakefileSourceConfiguration();
		if (makeConf != null) {
			if (makeConf.affectsBehavior(event)) {
				makeConf.adaptToPreferenceChange(event);
				sourceViewer.invalidateTextPresentation();
			}
		}

		if (MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED.equals(property)) {
			if (sourceViewer instanceof ProjectionViewer) {
				ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
				if (fProjectionMakefileUpdater != null)
					fProjectionMakefileUpdater.uninstall();
				// either freshly enabled or provider changed
				fProjectionMakefileUpdater = new ProjectionMakefileUpdater();
				if (fProjectionMakefileUpdater != null) {
					fProjectionMakefileUpdater.install(this, projectionViewer);
				}
			}
			return;
		}

		super.handlePreferenceStoreChanged(event);
	}

	@Override
	protected String[] collectContextMenuPreferencePages() {
		// Add Makefile Editor relevant pages
		String[] parentPrefPageIds = super.collectContextMenuPreferencePages();
		String[] prefPageIds = new String[parentPrefPageIds.length + 2];
		int nIds = 0;
		prefPageIds[nIds++] = "org.eclipse.cdt.make.ui.preferences.MakeFileEditorPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.make.ui.preferences.MakefileSettingPreferencePage"; //$NON-NLS-1$
		System.arraycopy(parentPrefPageIds, 0, prefPageIds, nIds, parentPrefPageIds.length);
		return prefPageIds;
	}
}
