/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Alex Ruiz (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the Preferences dialog.
 * By subclassing {@code FieldEditorPreferencePage}, we can use built-in field support in
 * JFace to create a page that is both small and knows how to save, restore and apply its
 * values.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via
 * the preference store.
 * </p>
 */
public class CodanPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private IProblemProfile profile;
	private ISelectionChangedListener problemSelectionListener;
	private ArrayList<IProblem> selectedProblems;
	private Button infoButton;
	private ProblemsTreeEditor checkedTreeEditor;

	public CodanPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, CodanCorePlugin.PLUGIN_ID));
		problemSelectionListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (infoButton != null && event.getSelection() instanceof ITreeSelection) {
					ITreeSelection s = (ITreeSelection) event.getSelection();
					ArrayList<IProblem> list = new ArrayList<>();
					for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
						Object o = iterator.next();
						if (o instanceof IProblem) {
							list.add((IProblem) o);
						}
					}
					setSelectedProblems(list);
				}
			}
		};
	}

	@Override
	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage"; //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to
	 * save and restore
	 * its own value.
	 */
	@Override
	public void createFieldEditors() {
		checkedTreeEditor = new ProblemsTreeEditor(getFieldEditorParent(), profile);
		addField(checkedTreeEditor);
		checkedTreeEditor.getTreeViewer().addSelectionChangedListener(problemSelectionListener);
		checkedTreeEditor.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				openCustomizeDialog();
			}
		});
		GridData layoutData = new GridData(GridData.FILL, GridData.FILL, true, true);
		layoutData.heightHint = 200;
		checkedTreeEditor.getTreeViewer().getControl().setLayoutData(layoutData);
	}

	@Override
	protected Control createContents(Composite parent) {
		if (isPropertyPage()) {
			profile = getRegistry().getResourceProfileWorkingCopy((IResource) getElement());
		} else {
			profile = getRegistry().getWorkspaceProfile();
		}
		Composite comp = (Composite) super.createContents(parent);
		createInfoControl(parent);
		return comp;
	}

	private void createInfoControl(Composite comp) {
		Composite info = new Composite(comp, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		info.setLayout(layout);
		infoButton = new Button(info, SWT.PUSH);
		infoButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.BEGINNING).create());
		infoButton.setText(CodanUIMessages.CodanPreferencePage_Customize);
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openCustomizeDialog();
			}
		});
		restoreWidgetValues();
	}

	protected void setSelectedProblems(ArrayList<IProblem> list) {
		this.selectedProblems = list;
		updateProblemInfo();
	}

	protected ICheckersRegistry getRegistry() {
		return CodanRuntime.getInstance().getCheckersRegistry();
	}

	@Override
	public boolean performOk() {
		saveWidgetValues();
		IResource resource = (IResource) getElement();
		getRegistry().updateProfile(resource, null);
		boolean success = super.performOk();
		if (success) {
			if (resource == null) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			asynchronouslyUpdateMarkers(resource);
		}
		return success;
	}

	private void saveWidgetValues() {
		String id = !hasSelectedProblems() ? EMPTY_STRING : selectedProblems.get(0).getId();
		getDialogSettings().put(getWidgetId(), id);
	}

	private void restoreWidgetValues() {
		String id = getDialogSettings().get(getWidgetId());
		if (id != null && !id.isEmpty() && checkedTreeEditor != null) {
			IProblem problem = profile.findProblem(id);
			if (problem != null) {
				checkedTreeEditor.getTreeViewer().setSelection(new StructuredSelection(problem), true);
			}
		} else {
			setSelectedProblems(null);
		}
		updateProblemInfo();
	}

	private IDialogSettings getDialogSettings() {
		return CodanUIActivator.getDefault().getDialogSettings();
	}

	protected String getWidgetId() {
		return getPageId() + ".selection"; //$NON-NLS-1$
	}

	private void updateProblemInfo() {
		infoButton.setEnabled(hasSelectedProblems());
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	protected void openCustomizeDialog() {
		if (!hasSelectedProblems()) {
			return;
		}
		IProblem[] selected = selectedProblems.toArray(new IProblem[selectedProblems.size()]);
		CustomizeProblemDialog dialog = new CustomizeProblemDialog(getShell(), selected, (IResource) getElement());
		dialog.open();
		checkedTreeEditor.getTreeViewer().refresh(true);
	}

	private boolean hasSelectedProblems() {
		return selectedProblems != null && !selectedProblems.isEmpty();
	}

	private static void asynchronouslyUpdateMarkers(final IResource resource) {
		final Set<IFile> filesToUpdate = new HashSet<>();
		final IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow active = workbench.getActiveWorkbenchWindow();
		final IWorkbenchPage page = active.getActivePage();
		// Get the files open C/C++ editors.
		for (IEditorReference partRef : page.getEditorReferences()) {
			IEditorPart editor = partRef.getEditor(false);
			if (editor instanceof ICEditor) {
				IFile file = editor.getEditorInput().getAdapter(IFile.class);
				if (file != null && resource.getFullPath().isPrefixOf(file.getFullPath())) {
					filesToUpdate.add(file);
				}
			}
		}

		Job job = new Job(CodanUIMessages.CodanPreferencePage_Update_markers) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final SubMonitor submonitor = SubMonitor.convert(monitor, 1 + 2 * filesToUpdate.size());
				removeMarkersForDisabledProblems(resource, submonitor.newChild(1));
				if (filesToUpdate.isEmpty())
					return Status.OK_STATUS;

				// Run checkers on the currently open files to update the problem markers.
				for (final IFile file : filesToUpdate) {
					ITranslationUnit tu = CoreModelUtil.findTranslationUnit(file);
					if (tu != null) {
						tu = CModelUtil.toWorkingCopy(tu);
						ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, submonitor.newChild(1),
								new ASTRunnable() {
									@Override
									public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
										ICodanBuilder builder = CodanRuntime.getInstance().getBuilder();
										if (ast != null) {
											builder.processResource(file, submonitor.newChild(1),
													CheckerLaunchMode.RUN_AS_YOU_TYPE, ast);
										} else {
											builder.processResource(file, submonitor.newChild(1),
													CheckerLaunchMode.RUN_ON_FILE_OPEN, null);
										}
										return Status.OK_STATUS;
									}
								});
					}
				}
				return Status.OK_STATUS;
			}
		};
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		job.setRule(ruleFactory.markerRule(resource));
		job.setSystem(true);
		job.schedule();
	}

	private static void removeMarkersForDisabledProblems(IResource resource, IProgressMonitor monitor) {
		CheckersRegistry chegistry = CheckersRegistry.getInstance();
		Set<String> markerTypes = new HashSet<>();
		for (IChecker checker : chegistry) {
			Collection<IProblem> problems = chegistry.getRefProblems(checker);
			for (IProblem problem : problems) {
				markerTypes.add(problem.getMarkerType());
			}
		}
		try {
			removeMarkersForDisabledProblems(chegistry, markerTypes, resource, monitor);
		} catch (CoreException e) {
			CodanUIActivator.log(e);
		}
	}

	private static void removeMarkersForDisabledProblems(CheckersRegistry chegistry, Set<String> markerTypes,
			IResource resource, IProgressMonitor monitor) throws CoreException {
		if (!resource.isAccessible()) {
			return;
		}
		IResource[] children = null;
		if (resource instanceof IContainer) {
			children = ((IContainer) resource).members();
		}
		int numChildren = children == null ? 0 : children.length;
		int childWeight = 10;
		SubMonitor progress = SubMonitor.convert(monitor, 1 + numChildren * childWeight);
		IProblemProfile resourceProfile = null;
		for (String markerType : markerTypes) {
			IMarker[] markers = resource.findMarkers(markerType, false, IResource.DEPTH_ZERO);
			for (IMarker marker : markers) {
				String problemId = (String) marker.getAttribute(ICodanProblemMarker.ID);
				if (resourceProfile == null) {
					resourceProfile = chegistry.getResourceProfile(resource);
				}
				IProblem problem = resourceProfile.findProblem(problemId);
				if (problem != null && !problem.isEnabled()) {
					marker.delete();
				}
			}
		}
		progress.worked(1);
		if (children != null) {
			for (IResource child : children) {
				if (monitor.isCanceled())
					return;
				removeMarkersForDisabledProblems(chegistry, markerTypes, child, progress.newChild(childWeight));
			}
		}
	}
}