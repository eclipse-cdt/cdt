/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [187571] Classification is empty for local directories
 * Martin Oberhuber (Wind River) - [234726] Update IRemoteFile Javadocs
 * David McKnight   (IBM)        -  [431060][local] RSE performance over local network drives are suboptimal
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.local.model;

import org.eclipse.rse.internal.services.local.files.LocalHostFile;
import org.eclipse.rse.services.clientserver.SystemFileClassifier;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;



public class LocalFile extends AbstractRemoteFile
{
	protected LocalHostFile _localHostFile;
	protected String _classification;

	public LocalFile(FileServiceSubSystem subSystem, IRemoteFileContext context, IRemoteFile parent, LocalHostFile hostFile)
	{
		super(subSystem, context, parent, hostFile);
		_localHostFile = hostFile;
	}

	public Object getFile()
	{
		return _localHostFile.getFile();
	}

	public boolean isVirtual()
	{
		return false;
	}

	public String getCanonicalPath()
	{
		try
		{
			return _localHostFile.getFile().getCanonicalPath();
		}
		catch (Exception e)
		{
			return ""; //$NON-NLS-1$
		}
	}

	public String getClassification() {

		if (_classification == null) {
			if (isFile()) {
				_classification = SystemFileClassifier.getInstance().classifyFile(getAbsolutePath());
			} else if (isDirectory()) {
				_classification = "directory"; //$NON-NLS-1$
			} else {
				_classification = "unknown"; //$NON-NLS-1$
			}
		}
		return _classification;
	}

	public void markStale(boolean isStale) {
		super.markStale(isStale);
		_localHostFile.setNeedsQuery(isStale);
	}

}
