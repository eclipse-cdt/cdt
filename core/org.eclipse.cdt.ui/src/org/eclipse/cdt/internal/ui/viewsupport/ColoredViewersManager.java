/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;


public class ColoredViewersManager implements IPropertyChangeListener {
	public static final String INHERITED_COLOR_NAME= "org.eclipse.cdt.ui.ColoredLabels.inherited"; //$NON-NLS-1$
	
	public static final String HIGHLIGHT_BG_COLOR_NAME= "org.eclipse.cdt.ui.ColoredLabels.match_highlight"; //$NON-NLS-1$
	public static final String HIGHLIGHT_WRITE_BG_COLOR_NAME= "org.eclipse.cdt.ui.ColoredLabels.writeaccess_highlight"; //$NON-NLS-1$
	
	private static ColoredViewersManager fgInstance= new ColoredViewersManager();
	
	private Set<ColoringLabelProvider> fManagedLabelProviders;
		
	public ColoredViewersManager() {
		fManagedLabelProviders= new HashSet<ColoringLabelProvider>();
	}
	
	public void installColoredLabels(ColoringLabelProvider labelProvider) {
		if (fManagedLabelProviders.contains(labelProvider))
			return;
		
		if (fManagedLabelProviders.isEmpty()) {
			// first lp installed
			PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
			JFaceResources.getColorRegistry().addListener(this);
		}
		fManagedLabelProviders.add(labelProvider);
	}
	
	public void uninstallColoredLabels(ColoringLabelProvider labelProvider) {
		if (!fManagedLabelProviders.remove(labelProvider))
			return; // not installed
		
		if (fManagedLabelProviders.isEmpty()) {
			PlatformUI.getPreferenceStore().removePropertyChangeListener(this);
			JFaceResources.getColorRegistry().removeListener(this);
			// last viewer uninstalled
		}
	}
				
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (property.equals(JFacePreferences.QUALIFIER_COLOR) || property.equals(JFacePreferences.COUNTER_COLOR) || property.equals(JFacePreferences.DECORATIONS_COLOR)
				|| property.equals(HIGHLIGHT_BG_COLOR_NAME) || property.equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS) || property.equals(HIGHLIGHT_WRITE_BG_COLOR_NAME)) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					refreshAllViewers();
				}
			});
		}
	}
	
	protected final void refreshAllViewers() {
		for (ColoringLabelProvider provider : fManagedLabelProviders) {
			provider.refresh();
		}
	}
		
	public static boolean showColoredLabels() {
		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
	}
	
	public static void install(ColoringLabelProvider labelProvider) {
		fgInstance.installColoredLabels(labelProvider);
	}
	
	public static void uninstall(ColoringLabelProvider labelProvider) {
		fgInstance.uninstallColoredLabels(labelProvider);
	}	
}
