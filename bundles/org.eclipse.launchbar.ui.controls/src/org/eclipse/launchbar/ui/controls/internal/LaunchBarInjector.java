/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *     Torkild U. Resheim - add preference to control target selector
 *     Vincent Guignot - Ingenico - add preference to control Build button
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import javax.inject.Inject;

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
import org.eclipse.swt.widgets.Widget;

public class LaunchBarInjector {

	@Inject
	MApplication application;

	@Inject
	IEventBroker eventBroker;

	@Execute
	void execute() {
		if (application == null) {
			// We are running headless, don't need the launch bar here.
			return;
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(Activator.PREF_ENABLE_LAUNCHBAR);
		injectIntoAll(enabled);

		// Watch for new trimmed windows and inject there too.
		eventBroker.subscribe(UIEvents.TrimmedWindow.TOPIC_TRIMBARS, event -> {
			if (!UIEvents.isADD(event))
				return;
			Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (newValue instanceof MTrimBar) {
				MTrimBar trimBar = (MTrimBar) newValue;
				if (trimBar.getSide() == SideValue.TOP) {
					IPreferenceStore store1 = Activator.getDefault().getPreferenceStore();
					boolean enabled1 = store1.getBoolean(Activator.PREF_ENABLE_LAUNCHBAR);
					injectLaunchBar(trimBar, enabled1);
				}
			}
		});

		// Watch for preference changes
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
			if (event.getProperty().equals(Activator.PREF_ENABLE_LAUNCHBAR)) {
				boolean enabled1 = Boolean.parseBoolean(event.getNewValue().toString());
				injectIntoAll(enabled1);
			}
			if (event.getProperty().equals(Activator.PREF_ALWAYS_TARGETSELECTOR)
					|| event.getProperty().equals(Activator.PREF_ENABLE_BUILDBUTTON)) {
				IPreferenceStore store1 = Activator.getDefault().getPreferenceStore();
				boolean enabled2 = store1.getBoolean(Activator.PREF_ENABLE_LAUNCHBAR);
				if (enabled2){
					injectIntoAll(false);
					injectIntoAll(true);
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
		// and fix up the class URI for 2.0

		// Search for control in trimbar
		MToolControl launchBarElement = null;
		for (MTrimElement trimElement : trimBar.getChildren()) {
			if (LaunchBarControl.ID.equals(trimElement.getElementId())) {
				launchBarElement = (MToolControl) trimElement;
				break;
			}
		}

		if (launchBarElement != null) {
			// Fix up class name
			if (!LaunchBarControl.CLASS_URI.equals(launchBarElement.getContributionURI())) {
				launchBarElement.setContributionURI(LaunchBarControl.CLASS_URI);
			}
			
			// remove it if we're disabled
			if (!enabled) {
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
