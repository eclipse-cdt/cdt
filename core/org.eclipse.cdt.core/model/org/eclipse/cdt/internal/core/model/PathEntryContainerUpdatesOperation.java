/*******************************************************************************
 * Copyright (c) 2005 QnX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.cdt.core.model.PathEntryContainerChanged;

public class PathEntryContainerUpdatesOperation extends CModelOperation {

	IPathEntryContainerExtension container;
	PathEntryContainerChanged[] events;

	public PathEntryContainerUpdatesOperation(IPathEntryContainerExtension container, PathEntryContainerChanged[] events) {
		super(CModelManager.getDefault().getCModel());
		this.container = container;
		this.events = events;
	}
	protected void executeOperation() throws CModelException {
		PathEntryManager pathEntryManager = PathEntryManager.getDefault();
		ArrayList list = new ArrayList(events.length);
		for (int i = 0; i < events.length; ++i) {
			PathEntryContainerChanged event = events[i];
			ICElement celement = CoreModel.getDefault().create(event.getPath());
			if (celement != null) {
				// Sanity check the container __must__ be set on the project.
				boolean foundContainer = false;
				IPathEntryContainer[] containers = pathEntryManager.getPathEntryContainers(celement.getCProject());
				for (int k = 0 ; k < containers.length; ++k) {
					if (containers[k].getPath().equals(container.getPath())) {
						foundContainer = true;
						break;
					}
				}
				if (!foundContainer) {
					continue;
				}
				// remove the element info caching.
				if (celement instanceof IOpenable) {
					try {
						((IOpenable)celement).close();
						// Make sure we clear the cache on the project too
						if (!(celement instanceof ICProject)) {
							celement.getCProject().close();
						}
					} catch (CModelException e) {
						// ignore.
					}
				}
				int flag =0;
				if (event.isIncludeChange()) {
					flag |= ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				} 
                if (event.isMacroChange()) {
					flag |= ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				}
				CElementDelta delta = new CElementDelta(celement.getCModel());
				delta.changed(celement, flag);
				list.add(delta);
			}
		}
		if (list.size() > 0) {
			final ICElementDelta[] deltas = new ICElementDelta[list.size()];
			list.toArray(deltas);
			CModelManager manager = CModelManager.getDefault();
			for (int i = 0; i < deltas.length; i++) {
				addDelta(deltas[i]);
			}
		}

	}

}
