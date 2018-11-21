/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB		  - Modules view for DSF implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * @since 1.0
 */
public class ModulesVMProvider extends AbstractDMVMProvider {
	/*
	 *  Current default for register formatting.
	 */
	public ModulesVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);

		/*
		 *  Create the top level node to deal with the root selection.
		 */
		IRootVMNode rootNode = new RootDMVMNode(this);

		/*
		 *  Create the Group nodes next. They represent the first level shown in the view.
		 */
		IVMNode modulesNode = new ModulesVMNode(this, getSession());
		addChildNodes(rootNode, new IVMNode[] { modulesNode });

		/*
		 *  Now set this schema set as the layout set.
		 */
		setRootNode(rootNode);
	}

	@Override
	public void refresh() {
		super.refresh();
		try {
			getSession().getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(),
							getSession().getId());
					IModules modulesService = tracker.getService(IModules.class);
					if (modulesService instanceof ICachingService) {
						((ICachingService) modulesService).flushCache(null);
					}
					tracker.dispose();
				}
			});
		} catch (RejectedExecutionException e) {
			// Session disposed, ignore.
		}
	}
}
