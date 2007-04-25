/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved. 
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Kushal Munir (IBM) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.search;

import java.util.List;
import java.util.Vector;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.view.SystemSelectRemoteObjectAPIProviderImpl;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;

/**
 * This is the input provider for selection dialogs related to search.
 */
public class SystemSearchRemoteObjectAPIProvider extends SystemSelectRemoteObjectAPIProviderImpl {

	/**
	 * Constructor.
	 * @param factoryId the subsystem factory id.
	 * @param factoryCategory the subsystem facory category.
	 * @param showNewConnectionPrompt whether to show new connection prompt.
	 * @param systemTypes the system types to restrict to.
	 */
	public SystemSearchRemoteObjectAPIProvider(String factoryId, String factoryCategory, boolean showNewConnectionPrompt, IRSESystemType[] systemTypes) {
		super(factoryId, factoryCategory, showNewConnectionPrompt, systemTypes);
	}

	/**
	 * Constructor.
	 * @param subsystem the subsystem.
	 */
	public SystemSearchRemoteObjectAPIProvider(ISubSystem subsystem) {
		super(subsystem);
	}

	/**
	 * Contrcutor.
	 */
	public SystemSearchRemoteObjectAPIProvider() {
		super();
	}

	/**
	 * @see org.eclipse.rse.internal.ui.view.SystemSelectRemoteObjectAPIProviderImpl#getConnections()
	 */
	protected Object[] getConnections() {
		
		Object[] objs = super.getConnections();
		List l = new Vector();
		
		for (int i = 0; i < objs.length; i++) {
			Object obj = objs[i];
			
			if (obj instanceof IHost) {
				IHost host = (IHost)obj;
				
				ISubSystem[] subsystems = sr.getSubSystems(host);
				
				for (int j = 0; j < subsystems.length; j++) {
					ISubSystem subsystem = subsystems[j];
					
					if (subsystem instanceof FileServiceSubSystem) {
						FileServiceSubSystem fileSubSystem = (FileServiceSubSystem)subsystem;
						
						ISearchService searchService = fileSubSystem.getSearchService();
						
						if (searchService != null) {
							l.add(obj);
							break;
						}
					}
				}
			}
			else {
				l.add(obj);
			}
		}
		
		return l.toArray();
	}
}