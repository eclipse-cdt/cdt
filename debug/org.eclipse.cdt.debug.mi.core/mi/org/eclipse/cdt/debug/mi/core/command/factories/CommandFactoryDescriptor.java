/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories; 

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A command factory descriptor wrappers a configuration
 * element for a <code>commandFactory</code> extension.
 */
public class CommandFactoryDescriptor {
	
	private final static String IDENTIFIER = "id"; //$NON-NLS-1$
	private final static String CLASS = "class"; //$NON-NLS-1$
	private final static String NAME = "name"; //$NON-NLS-1$
	private final static String DEBUGGER_ID = "debuggerID"; //$NON-NLS-1$
	private final static String MI_VERSIONS = "miVersions"; //$NON-NLS-1$
	private final static String DESCRIPTION = "description"; //$NON-NLS-1$
	private final static String PLATFORMS = "platforms"; //$NON-NLS-1$

	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;

	/**
	 * The set of the platforms supported by this command factory.
	 */
	private Set fPlatforms;

	/**
	 * The mi levels supported by this command factory.
	 */
	private String[] fMIVersions = new String[0];

	/** 
	 * Constructor for CommandFactoryDescriptor. 
	 */
	protected CommandFactoryDescriptor( IConfigurationElement element ) {
		fElement = element;
	}

	protected IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	public String getIdentifier() {
		return getConfigurationElement().getAttribute( IDENTIFIER );
	}

	public String getName() {
		return getConfigurationElement().getAttribute( NAME );
	}

	public String getDebuggerIdentifier() {
		return getConfigurationElement().getAttribute( DEBUGGER_ID );
	}

	public String[] getMIVersions() {
		if ( fMIVersions.length == 0 ) {
			String miVersions = getConfigurationElement().getAttribute( MI_VERSIONS );
			if ( miVersions == null || miVersions.trim().length() == 0 )
				miVersions = "mi"; //$NON-NLS-1$
			StringTokenizer tokenizer = new StringTokenizer( miVersions, "," ); //$NON-NLS-1$
			List list = new ArrayList( tokenizer.countTokens() );
			while( tokenizer.hasMoreTokens() ) {
				list.add( tokenizer.nextToken().trim() );
			}
			fMIVersions = (String[])list.toArray( new String[list.size()] );
		}
		return fMIVersions;
	}

	public String getDescription() {
		String desc = getConfigurationElement().getAttribute( DESCRIPTION );
		if ( isEmpty( desc ) ) {
			desc =""; //$NON-NLS-1$
		}
		return desc;
	}

	protected Set getSupportedPlatforms() {
		if ( fPlatforms == null ) {
			String platforms = getConfigurationElement().getAttribute( PLATFORMS );
			if ( platforms == null ) {
				return new HashSet( 0 );
			}
			StringTokenizer tokenizer = new StringTokenizer( platforms, "," ); //$NON-NLS-1$
			fPlatforms = new HashSet( tokenizer.countTokens() );
			while( tokenizer.hasMoreTokens() ) {
				fPlatforms.add( tokenizer.nextToken().trim() );
			}			
		}
		return fPlatforms;
	}

	public boolean supportsPlatform( String platform ) {
		Set all = getSupportedPlatforms();
		return all.isEmpty() || all.contains( "*" ) || all.contains( platform ); //$NON-NLS-1$
	}

	public String[] getSupportedPlatformList() {
		Set platforms = getSupportedPlatforms();
		return (String[])platforms.toArray( new String[platforms.size()] );
	}

	public CommandFactory getCommandFactory() throws CoreException {
		Object clazz = getConfigurationElement().createExecutableExtension( CLASS );
		if ( clazz instanceof CommandFactory ) {
			return (CommandFactory)clazz;
		}
		throw new CoreException( new Status( IStatus.ERROR, MIPlugin.getUniqueIdentifier(), -1, CommandFactoriesMessages.getString( "CommandFactoryDescriptor.0" ), null ) ); //$NON-NLS-1$
	}

	private boolean isEmpty( String str ) {
		return ( str == null || str.trim().length() == 0 );
	}
}
