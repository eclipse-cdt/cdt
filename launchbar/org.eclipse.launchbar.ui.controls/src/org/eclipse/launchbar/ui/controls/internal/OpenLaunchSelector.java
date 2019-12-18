/*******************************************************************************
 * Copyright (c) 2015, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.framework.FrameworkUtil;

public class OpenLaunchSelector extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(
				FrameworkUtil.getBundle(org.eclipse.e4.ui.workbench.IWorkbench.class).getBundleContext());
		MApplication application = serviceContext.get(MApplication.class);
		EModelService service = application.getContext().get(EModelService.class);
		List<Object> findElements = service.findElements(application, LaunchBarControl.ID, null, null);
		if (!findElements.isEmpty()) {
			MToolControl mpart = (MToolControl) findElements.get(0);
			Object bar = mpart.getObject();
			if (bar instanceof LaunchBarControl) {
				((LaunchBarControl) bar).getConfigSelector().openPopup();
			}
		}
		return Status.OK_STATUS;
	}
}
