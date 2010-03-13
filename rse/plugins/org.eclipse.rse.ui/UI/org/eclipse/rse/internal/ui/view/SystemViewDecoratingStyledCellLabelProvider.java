/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

/**
 * RSE Remote Systems view decorating styled cell label provider.<br>
 * Add common navigator style decorations.
 * 
 * @since 3.2
 */
public class SystemViewDecoratingStyledCellLabelProvider extends DecoratingStyledCellLabelProvider  implements IPropertyChangeListener, ILabelProvider {

	/**
	 * Constructor.
	 *
	 * @param labelProvider The styled label provider. Must not be <code>null</code>
	 * @param decorator The label decorator or <code>null</code> to use the platforms default label decorator.
	 */
	public SystemViewDecoratingStyledCellLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator) {
		this(labelProvider, decorator, DecorationContext.DEFAULT_CONTEXT);
	}

	/**
	 * Constructor.
	 *
	 * @param labelProvider The styled label provider. Must not be <code>null</code>
	 * @param decorator The label decorator or <code>null</code> to use the platforms default label decorator.
	 * @param decorationContext The decoration context or <code>null</code> to use default.
	 */
	public SystemViewDecoratingStyledCellLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator, IDecorationContext decorationContext) {
		super(labelProvider,
		      decorator != null ? decorator : PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(),
		      decorationContext);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		// Apply the styles to the label
		return getStyledText(element).getString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
	 */
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		// Add ourselfs to the color registry to get notified if the
		// decoration colour is changing
		JFaceResources.getColorRegistry().addListener(this);

		// There is no preference setting yet dedicated to the RSE remote system view
		// for showing coloroured label or not. Until available, we hook on the common
		// navigator preference setting.
		PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
		setOwnerDrawEnabled(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));

		super.initialize(viewer, column);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#dispose()
	 */
	public void dispose() {
		// Remove ourself as listener from the colour registry
		JFaceResources.getColorRegistry().removeListener(this);
		// And remove ourself as listener from the platforms preference store
		PlatformUI.getPreferenceStore().removePropertyChangeListener(this);

		super.dispose();
	}

	/**
	 * Returns the column viewer associated with the styled label decorator.
	 *
	 * @return The column viewer.
	 */
	protected final ColumnViewer getColumnViewer() { return getViewer(); }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(final PropertyChangeEvent event) {
		if (event == null) return;

		// flag to set if or if not to refresh the viewer
		boolean doRefresh = false;

		// We update the viewer if either the decoration colour changed or
		// the user changed the preference if using coloured labels or not
		doRefresh |= event.getProperty().equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
		doRefresh |= event.getProperty().equals(JFacePreferences.DECORATIONS_COLOR);

		// If we do not require to refresh the viewer, we are done here
		if (!doRefresh || !PlatformUI.isWorkbenchRunning() || PlatformUI.getWorkbench().getDisplay() == null) return;

		// Force the refresh of the viewer itself to be asynchronous and within the display thread.
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				// The associated viewer must be not null
				if (getColumnViewer() == null) return;

				// Get the current state if or if not to use coloured label
				boolean useColouredLabels = PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);

				// Update the internal flag if the preference setting has changed
				// and refresh the viewer
				if (isOwnerDrawEnabled() != useColouredLabels) { setOwnerDrawEnabled(useColouredLabels); getColumnViewer().refresh(); }

				// If useColouredLabels is switched on and the colour changed,
				// refresh the viewer
				else if (useColouredLabels) getColumnViewer().refresh();
			}
		});
	}
}
