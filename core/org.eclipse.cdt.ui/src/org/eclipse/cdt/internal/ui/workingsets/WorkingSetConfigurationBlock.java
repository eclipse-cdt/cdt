/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.dialogs.OptionalMessageDialog;

/**
 * A block of UI controls for management of working set configurations. These collect the selection of project
 * configurations for the member projects of the working sets into named presets.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public class WorkingSetConfigurationBlock {

	private static final String BUILD_PROMPT_DIALOG_ID = "workingsets.build.prompt"; //$NON-NLS-1$
	private static final int BUILD_PROMPT_DIALOG_NO = 0;
	private static final int BUILD_PROMPT_DIALOG_CANCEL = 1;
	private static final int BUILD_PROMPT_DIALOG_YES = 2;

	private WorkspaceSnapshot workspace;
	private WorkingSetConfigsController controller;

	private IWorkingSetProxy.ISnapshot initialSelection;
	private IFilter workingSetFilter;

	private Control contents;

	/**
	 * Initializes me. I take the most recently used working set as my initial selection.
	 * 
	 * @param workspace
	 *            the workspace snapshot to edit
	 */
	public WorkingSetConfigurationBlock(WorkspaceSnapshot workspace) {
		this(workspace, null);
	}

	/**
	 * Initializes me with my initial selection.
	 * 
	 * @param workspace
	 *            the workspace snapshot to edit
	 * @param initialSelection
	 *            my initial selection
	 */
	public WorkingSetConfigurationBlock(WorkspaceSnapshot workspace,
			IWorkingSetProxy.ISnapshot initialSelection) {
		
		this.workspace = workspace;
		this.initialSelection = initialSelection;
	}

	/**
	 * Queries the working set filter, if any, that restricts the display of working sets.
	 * 
	 * @return my working-set filter
	 */
	public IFilter getWorkingSetFilter() {
		return workingSetFilter;
	}

	/**
	 * Assigns a filter to restrict the working sets that are shown.
	 * 
	 * @param filter
	 *            a working-set filter
	 */
	public void setWorkingSetFilter(IFilter filter) {
		this.workingSetFilter = filter;
	}

	/**
	 * Creates the contents of the working set configuration management control block.
	 * 
	 * @param parent
	 *            the parent composite in which to create my controls
	 * 
	 * @return my controls
	 */
	public Control createContents(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);

		GridLayoutFactory layoutFactory = GridLayoutFactory.fillDefaults();
		createWorkingSetConfigsArea(sashForm, layoutFactory.extendedMargins(0, 0, 0, 15));
		createProjectConfigsArea(sashForm, layoutFactory.extendedMargins(0, 0, 15, 0));

		sashForm.setWeights(new int[] { 1, 1 });

		contents = sashForm;

		return sashForm;
	}

	/**
	 * Creates the "working set configurations" pane in the upper part of the sash form.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param layoutFactory
	 *            a layout-factory to use to lay out the composite in a grid, possibly pre-configured. Its use
	 *            is optional
	 * 
	 * @return the working set configurations pane
	 */
	protected Composite createWorkingSetConfigsArea(Composite parent, GridLayoutFactory layoutFactory) {
		Composite result = new Composite(parent, SWT.NONE);
		layoutFactory.numColumns(2).applyTo(result);

		GridDataFactory layoutDataFactory = GridDataFactory.fillDefaults();

		Label label = new Label(result, SWT.NONE);
		label.setText(WorkingSetMessages.WSConfigDialog_wsTree_label);
		layoutDataFactory.span(2, 1).applyTo(label);

		controller = new WorkingSetConfigsController(workspace, initialSelection);

		TreeViewer tree = new TreeViewer(result);
		layoutDataFactory.span(1, 1).align(SWT.FILL, SWT.FILL).grab(true, true).hint(250, SWT.DEFAULT)
				.applyTo(tree.getControl());

		tree.setContentProvider(new WSConfigsContentProvider());
		tree.setLabelProvider(new WSConfigsLabelProvider(tree));
		controller.setTreeViewer(tree);

		tree.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				if (element instanceof IWorkingSetConfiguration.ISnapshot) {
					IWorkingSetConfiguration.ISnapshot config = (IWorkingSetConfiguration.ISnapshot) element;
					if (config.isReadOnly()) {
						return 0;
					}
				}
				return 1;
			}
		});

		tree.getTree().getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = WorkingSetMessages.WSConfigDialog_wsTree_accessible_name;
			}
		});

		Composite buttons = new Composite(result, SWT.NONE);
		layoutDataFactory.grab(false, false).hint(SWT.DEFAULT, SWT.DEFAULT).applyTo(buttons);
		layoutFactory.numColumns(1).extendedMargins(0, 0, 0, 0).applyTo(buttons);

		Button button = new Button(buttons, SWT.PUSH);
		layoutDataFactory.align(SWT.FILL, SWT.BEGINNING).applyTo(button);
		button.setText(WorkingSetMessages.WSConfigDialog_add_label);
		controller.setAddButton(button);

		button = new Button(buttons, SWT.PUSH);
		layoutDataFactory.applyTo(button);
		button.setText(WorkingSetMessages.WSConfigDialog_remove_label);
		controller.setRemoveButton(button);

		button = new Button(buttons, SWT.PUSH);
		layoutDataFactory.applyTo(button);
		button.setText(WorkingSetMessages.WSConfigDialog_rename_label);
		controller.setRenameButton(button);

		button = new Button(buttons, SWT.PUSH);
		layoutDataFactory.applyTo(button);
		button.setText(WorkingSetMessages.WSConfigDialog_activate_label);
		controller.setActivateButton(button);

		button = new Button(buttons, SWT.PUSH);
		layoutDataFactory.applyTo(button);
		button.setText(WorkingSetMessages.WSConfigDialog_build_label);
		controller.setBuildButton(button);

		return result;
	}

	/**
	 * Creates the "project configurations" pane in the lower part of the sash form.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param layoutFactory
	 *            a layout-factory to use to lay out the composite in a grid, possibly pre-configured. Its use
	 *            is optional
	 * 
	 * @return the project configurations pane
	 */
	protected Composite createProjectConfigsArea(Composite parent, GridLayoutFactory layoutFactory) {
		Composite result = new Composite(parent, SWT.NONE);
		layoutFactory.numColumns(1).applyTo(result);

		GridDataFactory layoutDataFactory = GridDataFactory.fillDefaults();

		Label label = new Label(result, SWT.NONE);
		label.setText(WorkingSetMessages.WSConfigDialog_projTree_label);
		layoutDataFactory.applyTo(label);

		ProjectConfigsController projectsController = new ProjectConfigsController();
		CheckboxTreeViewer tree = new CheckboxTreeViewer(result);
		layoutDataFactory.span(1, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tree.getControl());

		controller.setProjectConfigsController(projectsController);
		projectsController.setWorkingSetConfigurationsController(controller);

		projectsController.setTreeViewer(tree);

		tree.setComparator(new ViewerComparator());

		tree.getTree().getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = WorkingSetMessages.WSConfigDialog_projTree_accessible_name;
			}
		});

		return result;
	}

	/**
	 * Saves the working set configurations to storage.
	 */
	public void save() {
		workspace.save();
	}

	/**
	 * Builds the projects that were reconfigured by the dialog, if any. The user is prompted (if prompting is
	 * not disabled via the preference) before building. The user has the options to build, not build, or
	 * cancel. The result indicates cancellation.
	 * 
	 * @return <code>true</code> if the user opted to save changes and exit the dialog (with or without
	 *         build); <code>false</code> if the user cancelled and the dialog should remain open and unsaved
	 */
	public boolean build() {
		boolean result = true;
		Collection<IProject> projects = workspace.getProjectsToBuild();

		if (!projects.isEmpty()) {
			int defaultButton = OptionalMessageDialog.getDialogDetail(BUILD_PROMPT_DIALOG_ID);
			if (defaultButton == OptionalMessageDialog.NO_DETAIL) {
				defaultButton = BUILD_PROMPT_DIALOG_YES; // yes button is the default-default
			}

			int button = OptionalMessageDialog.open(BUILD_PROMPT_DIALOG_ID, contents.getShell(),
					WorkingSetMessages.WSConfigDialog_buildPrompt_title, null,
					WorkingSetMessages.WSConfigDialog_buildPrompt_message, MessageDialog.QUESTION,
					new String[] { IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL,
							IDialogConstants.YES_LABEL }, defaultButton);

			if (button == OptionalMessageDialog.NOT_SHOWN) {
				// handle the case where the dialog was suppressed. Get the current default
				button = defaultButton;
			} else if (button != BUILD_PROMPT_DIALOG_CANCEL) {
				// store non-cancel selection as the new default answer
				OptionalMessageDialog.setDialogDetail(BUILD_PROMPT_DIALOG_ID, button);
			}

			switch (button) {
			case BUILD_PROMPT_DIALOG_YES:
				// do the build
				new BuildJob(projects).schedule();
				break;
			case BUILD_PROMPT_DIALOG_NO:
				// just don't build
				break;
			default: // BUILD_PROMPT_DIALOG_CANCEL
				result = false;
				break;
			}
		}

		return result;
	}

	//
	// Nested classes
	//

	/**
	 * Simple content provider for the working set configurations tree.
	 * 
	 * @author Christian W. Damus (cdamus)
	 */
	private class WSConfigsContentProvider implements ITreeContentProvider {
		private Collection<IWorkingSetProxy> workingSets;

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement == workingSets) {
				Collection<IWorkingSetProxy> filtered = filterWorkingSets(workingSets);
				return filtered.toArray();
			} else if (parentElement instanceof IWorkingSetProxy) {
				return ((IWorkingSetProxy) parentElement).getConfigurations().toArray();
			} else {
				return new Object[0];
			}
		}

		private Collection<IWorkingSetProxy> filterWorkingSets(Collection<IWorkingSetProxy> workingSets) {
			if (workingSetFilter == null) {
				return workingSets;
			}

			Collection<IWorkingSetProxy> result = new java.util.ArrayList<IWorkingSetProxy>();

			for (IWorkingSetProxy next : workingSets) {
				if (workingSetFilter.select(next)) {
					result.add(next);
				}
			}

			return result;
		}

		@Override
		public Object getParent(Object element) {
			return (element instanceof IWorkingSetConfiguration) ? ((IWorkingSetConfiguration) element)
					.getWorkingSet() : null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element != null) && !(element instanceof IWorkingSetConfiguration);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
			// nothing to dispose
		}

		@Override
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			workingSets = (Collection<IWorkingSetProxy>) newInput;
		}

	}

	/**
	 * Label provider for working sets and their configurations. The active configuration is highlighted in
	 * bold and affixed with an "(active)" decoration. The special read-only configuration is further
	 * differentiated with an italic font.
	 * 
	 * @author Christian W. Damus (cdamus)
	 */
	private class WSConfigsLabelProvider extends LabelProvider implements IFontProvider {
		private WorkbenchLabelProvider wbLabels = new WorkbenchLabelProvider();
		private Image configImage = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CONFIG);
		private Font defaultFont;
		private ResourceManager fonts = new LocalResourceManager(JFaceResources.getResources());

		WSConfigsLabelProvider(Viewer viewer) {
			defaultFont = viewer.getControl().getFont();
		}

		@Override
		public String getText(Object element) {
			if (element instanceof IWorkingSetConfiguration) {
				IWorkingSetConfiguration config = (IWorkingSetConfiguration) element;

				if (config.isActive()) {
					return WorkingSetConfiguration.isReadOnly(config)
							? WorkingSetMessages.WSConfigDialog_implicit_config : NLS.bind(
									WorkingSetMessages.WSConfigDialog_active_config, config.getName());
				}
				return config.getName();
			} else if (element instanceof IWorkingSetProxy) {
				return ((IWorkingSetProxy) element).resolve().getLabel();
			}

			return wbLabels.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof IWorkingSetConfiguration) {
				return configImage;
			} else if (element instanceof IWorkingSetProxy) {
				return wbLabels.getImage(((IWorkingSetProxy) element).resolve());
			}

			return wbLabels.getImage(element);
		}

		@Override
		public void dispose() {
			wbLabels.dispose();
			fonts.dispose();
			super.dispose();
		}

		@Override
		public Font getFont(Object element) {
			if (element instanceof IWorkingSetConfiguration) {
				IWorkingSetConfiguration config = (IWorkingSetConfiguration) element;
				if (config.isActive()) {
					FontDescriptor desc = FontDescriptor.createFrom(defaultFont);

					desc = WorkingSetConfiguration.isReadOnly(config) ? desc.withStyle(SWT.BOLD | SWT.ITALIC)
							: desc.withStyle(SWT.BOLD);
					return (Font) fonts.get(desc);
				}
			}

			return wbLabels.getFont(element);
		}
	}
}
