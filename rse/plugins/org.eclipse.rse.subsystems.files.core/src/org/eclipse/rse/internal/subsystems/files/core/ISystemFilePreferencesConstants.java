/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 * David McKnight  (IBM) - [191367] setting supertransfer to be disabled by default
 * Xuan Chen (IBM)       - [191367] setting supertransfer back to enabled by default
 * Xuan Chen (IBM)       - [202686] Supertransfer should be disabled by default for 2.0.1
 * David McKnight   (IBM)        - [245260] Different user's connections on a single host are mapped to the same temp files cache
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core;

public interface ISystemFilePreferencesConstants 
{

	public static final String ROOT = "org.eclipse.rse.subsystems.files.core.preferences."; //$NON-NLS-1$
	
    public static final String SHOWHIDDEN              = ROOT + "showhidden"; //$NON-NLS-1$
    
    public static final String FILETRANSFERMODEDEFAULT = ROOT + "filetransfermodedefault"; //$NON-NLS-1$
    
    public static final String PRESERVETIMESTAMPS     = ROOT + "preservetimestamps"; //$NON-NLS-1$
    
    public static final String SHARECACHEDFILES       = ROOT + "sharecachedfiles";//$NON-NLS-1$
    
	public static final String LIMIT_CACHE             = ROOT + "limit.cache"; //$NON-NLS-1$
	public static final String MAX_CACHE_SIZE          = ROOT + "max.cache.size"; //$NON-NLS-1$

    public static final String DOSUPERTRANSFER		   = ROOT + "dosupertransfer"; //$NON-NLS-1$
    public static final String SUPERTRANSFER_ARC_TYPE  = ROOT + "supertransfer.archivetype"; //$NON-NLS-1$
    
    public static final String DOWNLOAD_BUFFER_SIZE    = ROOT + "download.buffer.size"; //$NON-NLS-1$
    public static final String UPLOAD_BUFFER_SIZE      = ROOT + "upload.buffer.size"; //$NON-NLS-1$
    
    public static final boolean DEFAULT_SHOW_HIDDEN              = true;
    public static final boolean DEFAULT_PRESERVETIMESTAMPS       = true;
    public static final boolean DEFAULT_SHARECACHEDFILES        = true;
    public static final int     DEFAULT_FILETRANSFERMODE         = 0;
    
	public static final int FILETRANSFERMODE_BINARY 			= 0;
	public static final int FILETRANSFERMODE_TEXT 				= 1;
	
	public static final String DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE 	= "zip"; //$NON-NLS-1$
	public static final boolean DEFAULT_DOSUPERTRANSFER 			= false;
	
	public static final int DEFAULT_DOWNLOAD_BUFFER_SIZE        = 40;
	
	public static final boolean DEFAULT_LIMIT_CACHE             = false;
	public static final String  DEFAULT_MAX_CACHE_SIZE          = "512"; //$NON-NLS-1$
}
