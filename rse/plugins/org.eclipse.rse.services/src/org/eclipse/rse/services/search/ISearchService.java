/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 ********************************************************************************/

package org.eclipse.rse.services.search;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.files.IFileService;

/**
 * RSE Search Service Interface.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Search service implementations must subclass
 *              {@link AbstractSearchService} rather than implementing this
 *              interface directly.
 */
public interface ISearchService
{
	public void search(IHostSearchResultConfiguration searchConfig, IFileService fileService, IProgressMonitor monitor);
	public void cancelSearch(IHostSearchResultConfiguration searchConfig, IProgressMonitor monitor);

}