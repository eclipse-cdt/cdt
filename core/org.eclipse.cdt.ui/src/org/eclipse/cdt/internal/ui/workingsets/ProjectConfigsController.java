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

import java.util.Map;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * View controller for the project configurations pane of the working set configurations dialog. It takes care
 * of coordinating the user gestures in that pane with the working-set configuration model and vice-versa.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
class ProjectConfigsController implements ICheckStateListener, DisposeListener {
	private CheckboxTreeViewer tree;

	private IWorkingSetConfiguration.ISnapshot workingSet;
	private WorkingSetConfigsController workingSetController;

	private ControllerContext controllerContext;

	private ILabelProvider labelProvider = new DelegatingLabelProvider();
	private ITreeContentProvider contentProvider = new DelegatingContentProvider();

	/**
	 * Initializes me.
	 */
	ProjectConfigsController() {
		super();
	}

	/**
	 * Assigns the tree viewer that I control.
	 * 
	 * @param tree
	 *            my tree viewer
	 */
	void setTreeViewer(CheckboxTreeViewer tree) {
		if (this.tree != null) {
			this.tree.getTree().removeDisposeListener(this);
			this.tree.removeCheckStateListener(this);
			this.tree.setLabelProvider(null);
			this.tree.setContentProvider(null);
		}

		this.tree = tree;

		if (this.tree != null) {
			this.tree.setUseHashlookup(true);
			this.tree.setContentProvider(contentProvider);
			this.tree.setLabelProvider(labelProvider);
			this.tree.addCheckStateListener(this);
			this.tree.getTree().addDisposeListener(this);
		}
	}

	/**
	 * Injects the current selection of a working set from the Working Set Configurations pane. This changes
	 * the project configurations that I show in my own tree.
	 * 
	 * @param config
	 *            the new working set configuration selection. May be <code>null</code> if there is no
	 *            selection
	 */
	void setWorkingSetConfiguration(IWorkingSetConfiguration.ISnapshot config) {
		if ((tree != null) && (config != workingSet)) {
			this.workingSet = config;

			tree.setSelection(new StructuredSelection());

			if (config != null) {
				controllerContext = new ControllerContext(tree);
				tree.setInput(config);
				tree.getTree().setEnabled(true);
				updateCheckState(config);
				tree.expandToLevel(2);
			} else {
				tree.getTree().setEnabled(false);
				tree.setInput(config);
				controllerContext.dispose();
				controllerContext = null;
			}
		}
	}

	/**
	 * Queries the current working set configuration that I show in my tree.
	 * 
	 * @return the working set configuration, or <code>null</code> if none
	 */
	IWorkingSetConfiguration.ISnapshot getWorkingSetConfiguration() {
		return workingSet;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		IWorkingSetProjectConfigurationController controller = controllerContext
				.controllerForElement(element);

		if (controller != null) {
			controller.checkStateChanged(element, event.getChecked(), controllerContext);

		} else {
			// controller unknown? Cannot change the check-state
			tree.setChecked(element, !event.getChecked());
		}
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		dispose();
	}

	/**
	 * Computes the initial check-box settings for my tree according to the current state of the specified
	 * working set configuration.
	 * 
	 * @param config
	 *            a working set configuration that I am now showing
	 */
	private void updateCheckState(IWorkingSetConfiguration.ISnapshot config) {
		for (IWorkingSetProjectConfiguration project : config.getProjectConfigurations()) {
			IWorkingSetProjectConfigurationController controller = controllerContext
					.controllerForElement(project);

			if (controller != null) {
				controller.updateCheckState(controllerContext);
			}
		}
	}

	void update() {
		if (tree != null) {
			tree.refresh(true);
		}
	}

	/**
	 * Connects me to the controller of the working set configurations pane.
	 * 
	 * @param controller
	 *            the working-set configs controller
	 */
	void setWorkingSetConfigurationsController(WorkingSetConfigsController controller) {
		workingSetController = controller;
	}

	void dispose() {
		if (controllerContext != null) {
			controllerContext.dispose();
			controllerContext = null;
		}
	}

	//
	// Nested classes
	//

	private class ControllerContext implements IWorkingSetProjectConfigurationController.IControllerContext {

		private Map<Object, IWorkingSetProjectConfigurationController> elementToControllerMap;

		private Map<IWorkingSetProjectConfigurationController, ITreeContentProvider> contentProviders;
		private Map<IWorkingSetProjectConfigurationController, ILabelProvider> labelProviders;

		ControllerContext(Viewer viewer) {
			elementToControllerMap = new java.util.IdentityHashMap<Object, IWorkingSetProjectConfigurationController>();
			contentProviders = new java.util.HashMap<IWorkingSetProjectConfigurationController, ITreeContentProvider>();
			labelProviders = new java.util.HashMap<IWorkingSetProjectConfigurationController, ILabelProvider>();

			for (IWorkingSetProjectConfiguration next : getWorkingSetConfiguration()
					.getProjectConfigurations()) {

				IWorkingSetProjectConfiguration.ISnapshot project = (IWorkingSetProjectConfiguration.ISnapshot) next;

				IWorkingSetProjectConfigurationController controller = IWorkingSetProjectConfigurationFactory.Registry.INSTANCE
						.getFactory(project.resolveProject()).createProjectConfigurationController(project);

				if (controller == null) {
					// can only supply the default behaviour, then
					controller = new ProjectConfigurationController(project);
				}

				elementToControllerMap.put(project, controller);
				contentProviders.put(controller, controller.getContentProvider());
				labelProviders.put(controller, controller.getLabelProvider(viewer));

				discoverElements(controller, project, contentProviders.get(controller));
			}
		}

		private void discoverElements(IWorkingSetProjectConfigurationController controller, Object element,
				ITreeContentProvider provider) {
			Object[] children = provider.getChildren(element);

			for (Object next : children) {
				elementToControllerMap.put(next, controller);
				discoverElements(controller, next, provider);
			}
		}

		IWorkingSetProjectConfigurationController controllerForElement(Object element) {
			return elementToControllerMap.get(element);
		}

		ITreeContentProvider contentProviderForElement(Object element) {
			return contentProviders.get(elementToControllerMap.get(element));
		}

		ILabelProvider labelProviderForElement(Object element) {
			return labelProviders.get(elementToControllerMap.get(element));
		}

		@Override
		public void activationStateChanged(IWorkingSetProjectConfiguration projectConfiguration) {
			workingSetController.projectSelectionsChanged(projectConfiguration);
		}

		@Override
		public boolean isReadOnly() {
			return getWorkingSetConfiguration().isReadOnly();
		}

		@Override
		public void setChecked(Object element, boolean checked) {
			tree.setChecked(element, checked);
		}

		@Override
		public void setGrayed(Object element, boolean grayed) {
			tree.setGrayChecked(element, grayed);
		}

		@Override
		public void update(Object element) {
			if ((element instanceof IWorkingSetConfiguration) || (element instanceof IWorkingSetProxy)) {
				// TODO: Talk to the WS controller
			} else if (controllerForElement(element) != null) {
				// only need to update if we have actually accessed this
				// element, yet, in order to display it
				tree.update(element, null);
			}
		}

		public void dispose() {
			elementToControllerMap = null;

			if (contentProviders != null) {
				for (ITreeContentProvider next : contentProviders.values()) {
					next.dispose();
				}
				contentProviders = null;
			}

			if (labelProviders != null) {
				for (ILabelProvider next : labelProviders.values()) {
					next.dispose();
				}
				labelProviders = null;
			}
		}
	}

	private class DelegatingLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {
		private final NullLabelProvider defaultLabels = new NullLabelProvider();

		@Override
		public String getText(Object element) {
			return delegateFor(element, ILabelProvider.class).getText(element);
		}

		@Override
		public Image getImage(Object element) {
			return delegateFor(element, ILabelProvider.class).getImage(element);
		}

		@Override
		public Font getFont(Object element) {
			return delegateFor(element, IFontProvider.class).getFont(element);
		}

		@Override
		public Color getBackground(Object element) {
			return delegateFor(element, IColorProvider.class).getBackground(element);
		}

		@Override
		public Color getForeground(Object element) {
			return delegateFor(element, IColorProvider.class).getForeground(element);
		}

		@SuppressWarnings("unchecked")
		private <T> T delegateFor(Object element, Class<T> expectedType) {
			if (controllerContext == null) {
				return (T) defaultLabels;
			} else {
				T result = (T) controllerContext.labelProviderForElement(element);

				if (!expectedType.isInstance(result)) {
					return (T) defaultLabels;
				}

				return result;
			}
		}
	}

	private class DelegatingContentProvider implements ITreeContentProvider {
		private final Object[] NO_OBJECTS = new Object[0];

		@Override
		public Object[] getChildren(Object parentElement) {
			ITreeContentProvider delegate = delegateFor(parentElement);

			return (delegate != null) ? delegate.getChildren(parentElement) : NO_OBJECTS;
		}

		@Override
		public Object getParent(Object element) {
			ITreeContentProvider delegate = delegateFor(element);

			return (delegate != null) ? delegate.getParent(element) : null;
		}

		@Override
		public boolean hasChildren(Object element) {
			ITreeContentProvider delegate = delegateFor(element);

			return (delegate != null) && delegate.hasChildren(element);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getWorkingSetConfiguration().getProjectConfigurations().toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do
		}

		@Override
		public void dispose() {
			// nothing of my own to dispose
		}

		private ITreeContentProvider delegateFor(Object element) {
			return (controllerContext == null) ? null : controllerContext.contentProviderForElement(element);
		}
	}

	/**
	 * A useful empty implementation of the extended label-provider protocol.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	private static class NullLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {

		@Override
		public Font getFont(Object element) {
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}

		@Override
		public Color getForeground(Object element) {
			return null;
		}

	}
}