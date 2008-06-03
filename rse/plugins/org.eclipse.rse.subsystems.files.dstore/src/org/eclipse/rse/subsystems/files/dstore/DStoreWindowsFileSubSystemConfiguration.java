/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [222270] clean up interfaces in org.eclipse.rse.core.filters
 * Martin Oberhuber (Wind River) - [235463][ftp][dstore] Incorrect case sensitivity reported on windows-remote
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.dstore;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.ui.SystemBasePlugin;

public class DStoreWindowsFileSubSystemConfiguration extends DStoreFileSubSystemConfiguration
{

	public DStoreWindowsFileSubSystemConfiguration() {
		super();
		_isWindows = true;
		setIsUnixStyle(!_isWindows);
	}

	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		ISystemFilterPool pool = null;
		try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------
		  pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
		  if (pool == null) // hmmm, why would this happen?
		  {
			SystemBasePlugin.logError("Creating default filter pool "+getDefaultFilterPoolName(mgr.getName(), getId())+" for mgr "+mgr.getName()+" failed.",null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		  }
		 if (isUserPrivateProfile(mgr))
		  {

		      // ----------------------
		      // "My Home" filter...
		      // ----------------------
		      String[] filterStrings = new String[] {".\\*"}; //$NON-NLS-1$
		      RemoteFileFilterString myHomeFilterString = new RemoteFileFilterString(this);
		      myHomeFilterString.setPath(getSeparator());
		      ISystemFilter filter = mgr.createSystemFilter(pool, SystemFileResources.RESID_FILTER_MYHOME,filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);

		      RemoteFileFilterString defaultFilterString = new RemoteFileFilterString(this);
		      filterStrings = new String[] {defaultFilterString.toString()};
		      String filterName = SystemFileResources.RESID_FILTER_DRIVES;

		      mgr.createSystemFilter(pool, filterName, filterStrings);
		  }
		 else
		 {
		      RemoteFileFilterString defaultFilterString = new RemoteFileFilterString(this);
			 String[] filterStrings = new String[] {defaultFilterString.toString()};
		      String filterName = SystemFileResources.RESID_FILTER_DRIVES;

		      mgr.createSystemFilter(pool, filterName, filterStrings);
		 }
		}
		 catch (Exception exc)
			{
				SystemBasePlugin.logError("Error creating default filter pool",exc); //$NON-NLS-1$
			}
			return pool;
	}

	public String getSeparator()
	{
		return "\\"; //$NON-NLS-1$
	}

	public char getSeparatorChar()
	{
		return '\\';
	}



}
