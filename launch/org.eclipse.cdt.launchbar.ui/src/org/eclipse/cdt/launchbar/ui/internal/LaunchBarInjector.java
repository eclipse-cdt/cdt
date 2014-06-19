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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class LaunchBarInjector {

	@Inject
	MApplication application;
	
	@Inject
	IEventBroker eventBroker;
	
	@Execute
	void execute() {
		// Inject the toolbar into all top trims
		for (MWindow window : application.getChildren()) {
			if (window instanceof MTrimmedWindow) {
				for (MTrimBar trimBar : ((MTrimmedWindow) window).getTrimBars()) {
					if (trimBar.getSide() == SideValue.TOP) {
						injectLaunchBar(trimBar);
					}
				}
			}
		}
		
		// Watch for new trimmed windows and inject there too.
		eventBroker.subscribe(UIEvents.TrimmedWindow.TOPIC_TRIMBARS, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				if (!UIEvents.isADD(event))
					return;
				Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (newValue instanceof MTrimBar) {
					MTrimBar trimBar = (MTrimBar) newValue;
					if (trimBar.getSide() == SideValue.TOP)
						injectLaunchBar(trimBar);
				}
			}
		});
	}
	
	void injectLaunchBar(MTrimBar trimBar) {
		// Skip if we're already there
		for (MTrimElement trimElement : trimBar.getChildren())
			if (LaunchBarControl.ID.equals(trimElement.getElementId()))
				return;

		MToolControl launchBar = MMenuFactory.INSTANCE.createToolControl();
		launchBar.setElementId(LaunchBarControl.ID);
		launchBar.setContributionURI(LaunchBarControl.CLASS_URI);
		trimBar.getChildren().add(0, launchBar);
	}

}
