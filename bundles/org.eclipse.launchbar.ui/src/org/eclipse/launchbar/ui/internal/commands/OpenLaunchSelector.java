/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.launchbar.ui.internal.controls.LaunchBarControl;

public class OpenLaunchSelector extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEclipseContext serviceContext = E4Workbench.getServiceContext();
		MApplication application = serviceContext.get(MApplication.class);
		EModelService service = application.getContext().get(EModelService.class);
		List<Object> findElements = service.findElements(application, LaunchBarControl.ID,
				null, null);
		if (findElements.size() > 0) {
			MToolControl mpart = (MToolControl) findElements.get(0);
			Object bar = mpart.getObject();
			if (bar instanceof LaunchBarControl) {
				((LaunchBarControl) bar).getConfigSelector().openPopup();
			}
		}
		return Status.OK_STATUS;
	}
}
