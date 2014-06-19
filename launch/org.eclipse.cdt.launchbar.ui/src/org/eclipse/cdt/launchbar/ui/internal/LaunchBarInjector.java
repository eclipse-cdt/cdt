/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal;

import javax.inject.Inject;

import org.eclipse.cdt.launchbar.ui.internal.controls.LaunchBarControl;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class LaunchBarInjector {

	@Inject
	MApplication application;
	
	@Inject
	IEventBroker eventBroker;
	
	@Execute
	void execute() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(Activator.PREF_ENABLE_LAUNCHBAR);
		injectIntoAll(enabled);
		
		// Watch for new trimmed windows and inject there too.
		eventBroker.subscribe(UIEvents.TrimmedWindow.TOPIC_TRIMBARS, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				if (!UIEvents.isADD(event))
					return;
				Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (newValue instanceof MTrimBar) {
					MTrimBar trimBar = (MTrimBar) newValue;
					if (trimBar.getSide() == SideValue.TOP) {
						IPreferenceStore store = Activator.getDefault().getPreferenceStore();
						boolean enabled = store.getBoolean(Activator.PREF_ENABLE_LAUNCHBAR);
						injectLaunchBar(trimBar, enabled);
					}
				}
			}
		});

		// Watch for preference changes
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(Activator.PREF_ENABLE_LAUNCHBAR)) {
					boolean enabled = Boolean.parseBoolean(event.getNewValue().toString());
					injectIntoAll(enabled);
				}
			}
		});
	}

	private void injectIntoAll(boolean enabled) {
		// Inject the toolbar into all top trims
		for (MWindow window : application.getChildren()) {
			if (window instanceof MTrimmedWindow) {
				for (MTrimBar trimBar : ((MTrimmedWindow) window).getTrimBars()) {
					if (trimBar.getSide() == SideValue.TOP) {
						injectLaunchBar(trimBar, enabled);
					}
				}
			}
		}
	}

	private void injectLaunchBar(MTrimBar trimBar, boolean enabled) {
		// are we enabled or not
		
		// Search for control in trimbar
		MTrimElement launchBarElement = null;
		for (MTrimElement trimElement : trimBar.getChildren()) {
			if (LaunchBarControl.ID.equals(trimElement.getElementId())) {
				launchBarElement = trimElement;
				break;
			}
		}

		if (launchBarElement != null) {
			if (!enabled) {
				// remove it if we're disabled
				trimBar.getChildren().remove(launchBarElement);
				// This seems to be a bug in the platform but for now, dispose of the widget
				Widget widget = (Widget)launchBarElement.getWidget();
				widget.dispose();
			}
			// either way, we're done
			return;
		}

		if (enabled) {
			// Add it
			MToolControl launchBar = MMenuFactory.INSTANCE.createToolControl();
			launchBar.setElementId(LaunchBarControl.ID);
			launchBar.setContributionURI(LaunchBarControl.CLASS_URI);
			trimBar.getChildren().add(0, launchBar);
		}
	}
	
}
