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

import java.util.Set;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.workingsets.IWorkingSetProjectConfiguration.ISnapshot;

/**
 * Default implementation of the working set project configuration controller protocol.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
public class ProjectConfigurationController implements IWorkingSetProjectConfigurationController {

	private IWorkingSetProjectConfiguration.ISnapshot config;

	/**
	 * Initializes me with my project configuration.
	 * 
	 * @param config
	 *            my project configuration
	 */
	public ProjectConfigurationController(IWorkingSetProjectConfiguration.ISnapshot config) {
		this.config = config;
	}

	@Override
	public ISnapshot getProjectConfiguration() {
		return config;
	}

	@Override
	public void checkStateChanged(Object element, boolean checked, IControllerContext context) {
		if (!context.isReadOnly() && (element instanceof ICConfigurationDescription)) {

			ICConfigurationDescription newSel = (ICConfigurationDescription) element;
			ICConfigurationDescription oldSel = getProjectConfiguration().resolveSelectedConfiguration();

			boolean oldActive = getProjectConfiguration().isActive();

			if (checked) {
				if (newSel != oldSel) {
					getProjectConfiguration().setSelectedConfigurationID(newSel.getId());

					if (oldSel != null) {
						context.setChecked(oldSel, false);
					}
				}
			} else if (newSel == oldSel) {
				// cannot just uncheck the current selection
				context.setChecked(oldSel, true);
			}

			boolean newActive = getProjectConfiguration().isActive();
			if (oldActive != newActive) {
				context.activationStateChanged(getProjectConfiguration());
			}
		} else {
			// cannot change the check-state of these nodes
			context.setChecked(element, !checked);
		}
	}

	@Override
	public void updateCheckState(IControllerContext context) {
		ICConfigurationDescription sel = getProjectConfiguration().resolveSelectedConfiguration();

		if (sel != null) {
			context.setChecked(sel, true);
		}

		// gray the project node
		context.setGrayed(getProjectConfiguration(), true);
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return new ContentProvider(getProjectConfiguration());
	}

	@Override
	public ILabelProvider getLabelProvider(Viewer viewer) {
		return new LabelProvider(viewer, getProjectConfiguration());
	}

	//
	// Nested classes
	//

	private static class ContentProvider implements ITreeContentProvider {
		private static final Object[] NO_OBJECTS = new Object[0];

		private IWorkingSetProjectConfiguration project;
		private Set<ICConfigurationDescription> configs;

		ContentProvider(IWorkingSetProjectConfiguration project) {
			this.project = project;
			this.configs = new java.util.HashSet<ICConfigurationDescription>(project.resolveConfigurations());
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return (parentElement == project) ? project.resolveConfigurations().toArray() : NO_OBJECTS;
		}

		@Override
		public Object getParent(Object element) {
			return (element == project) ? null : configs.contains(element) ? project : null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element == project;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return new Object[] { project };
		}

		@Override
		public void dispose() {
			// nothing to dispose
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do
		}

	}

	private static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider implements
			IFontProvider {

		private IWorkingSetProjectConfiguration.ISnapshot projectConfig;
		private WorkbenchLabelProvider wbLabels = new WorkbenchLabelProvider();
		private Image configImage = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CONFIG);

		private Font defaultFont;
		private ResourceManager fonts = new LocalResourceManager(JFaceResources.getResources());

		LabelProvider(Viewer viewer, IWorkingSetProjectConfiguration.ISnapshot projectConfig) {
			this.projectConfig = projectConfig;
			this.defaultFont = viewer.getControl().getFont();
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ICConfigurationDescription) {
				ICConfigurationDescription config = (ICConfigurationDescription) element;

				return isActiveInWorkspace(config) ? NLS.bind(
						WorkingSetMessages.ProjConfigController_activeConfig, config.getName()) : config
						.getName();
			} else if (element instanceof IWorkingSetProjectConfiguration) {
				return wbLabels.getText(((IWorkingSetProjectConfiguration) element).resolveProject());
			}

			return wbLabels.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof ICConfigurationDescription) {
				return configImage;
			} else if (element instanceof IWorkingSetProjectConfiguration) {
				return wbLabels.getImage(((IWorkingSetProjectConfiguration) element).resolveProject());
			}

			return wbLabels.getImage(element);
		}

		@Override
		public Font getFont(Object element) {
			if (element instanceof ICConfigurationDescription) {
				ICConfigurationDescription config = (ICConfigurationDescription) element;

				if (isActiveInWorkspace(config)) {
					FontDescriptor desc = FontDescriptor.createFrom(defaultFont);
					return (Font) fonts.get(desc.withStyle(SWT.BOLD));
				}
			}

			return wbLabels.getFont(element);
		}

		private boolean isActiveInWorkspace(ICConfigurationDescription config) {
			WorkspaceSnapshot workspace = projectConfig.getWorkspaceSnapshot();

			return workspace.getState(projectConfig.resolveProject()).isActive(config.getId());
		}

		@Override
		public void dispose() {
			wbLabels.dispose();
			fonts.dispose();
			super.dispose();
		}
	}

}