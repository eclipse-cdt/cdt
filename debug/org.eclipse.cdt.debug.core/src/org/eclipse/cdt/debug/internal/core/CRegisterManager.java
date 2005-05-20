/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CRegisterGroup;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Manages all register groups in a debug target.
 */
public class CRegisterManager {
	
	private static final String ELEMENT_REGISTER_GROUP_LIST = "registerGroups"; //$NON-NLS-1$
	private static final String ELEMENT_REGISTER_GROUP = "group"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_MEMENTO = "memento"; //$NON-NLS-1$

	/**
	 * The debug target associated with this manager.
	 */
	private CDebugTarget fDebugTarget;

	/**
	 * Collection of register groups added to this target. Values are of type <code>CRegisterGroup</code>.
	 */
	protected List fRegisterGroups;

	/**
	 * The list of all register descriptors.
	 */
	private IRegisterDescriptor[] fRegisterDescriptors;

	/** 
	 * Constructor for CRegisterManager. 
	 */
	public CRegisterManager( CDebugTarget target ) {
		fDebugTarget = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( CRegisterManager.class.equals( adapter ) )
			return this;
		return null;
	}

	public void dispose() {
		removeAllRegisterGroups();
	}

	public IRegisterDescriptor[] getAllRegisterDescriptors() throws DebugException {
		return fRegisterDescriptors;
	}

	public IRegisterGroup[] getRegisterGroups( CStackFrame frame ) throws DebugException {
		return (IRegisterGroup[])fRegisterGroups.toArray( new IRegisterGroup[fRegisterGroups.size()] );
	}

	public void initialize() {
		ICDIRegisterGroup[] groups = new ICDIRegisterGroup[0];
		try {
			groups = getDebugTarget().getCDITarget().getRegisterGroups();
		}
		catch( CDIException e ) {
			CDebugCorePlugin.log( e );
		}
		List list = new ArrayList();
		for( int i = 0; i < groups.length; ++i ) {
			try {
				ICDIRegisterDescriptor[] cdiDescriptors = groups[i].getRegisterDescriptors();
				for ( int j = 0; j < cdiDescriptors.length; ++j ) {
					list.add( new CRegisterDescriptor( groups[i], cdiDescriptors[j] ) );
				}
			}
			catch( CDIException e ) {
				CDebugCorePlugin.log( e );
			}
		}
		fRegisterDescriptors = (IRegisterDescriptor[])list.toArray( new IRegisterDescriptor[list.size()] );
		createRegisterGroups();
	}

	public void addRegisterGroup( final String name, final IRegisterDescriptor[] descriptors ) {
		DebugPlugin.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					fRegisterGroups.add( new CRegisterGroup( getDebugTarget(), name, descriptors ) );
					getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
				}
			} );
	}

	public void removeAllRegisterGroups() {
		DebugPlugin.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					synchronized( fRegisterGroups ) {
						Iterator it = fRegisterGroups.iterator();
						while( it.hasNext() ) {
							((CRegisterGroup)it.next()).dispose();
						}
						fRegisterGroups.clear();
					}
					getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
				}
			} );
	}

	public void removeRegisterGroups( final IRegisterGroup[] groups ) {
		DebugPlugin.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					for ( int i = 0; i < groups.length; ++i ) {
						((CRegisterGroup)groups[i]).dispose();
					}
					fRegisterGroups.removeAll( Arrays.asList( groups ) );
					getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
				}
			} );
	}

	private void createRegisterGroups() {
		fRegisterGroups = Collections.synchronizedList( new ArrayList( 20 ) );
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			String memento = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS, "" ); //$NON-NLS-1$
			if ( memento != null && memento.length() > 0 ) {
				initializeFromMemento( memento );
				return;
			}
		}
		catch( CoreException e ) {
		}
		initializeDefaults();
	}

	public void targetSuspended() {
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() ) {
			((CRegisterGroup)it.next()).targetSuspended();
		}
	}

	protected CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	private void initializeFromMemento( String memento ) throws CoreException {
		Node node = DebugPlugin.parseDocument( memento );
		if ( node.getNodeType() != Node.ELEMENT_NODE ) {
			abort( InternalDebugCoreMessages.getString( "CRegisterManager.0" ), null ); //$NON-NLS-1$
		}
		Element element = (Element)node;
		if ( !ELEMENT_REGISTER_GROUP_LIST.equals( element.getNodeName() ) ) {
			abort( InternalDebugCoreMessages.getString( "CRegisterManager.1" ), null ); //$NON-NLS-1$
		}
		Node childNode = element.getFirstChild();
		while( childNode != null ) {
			if ( childNode.getNodeType() == Node.ELEMENT_NODE ) {
				Element child = (Element)childNode;
				if ( ELEMENT_REGISTER_GROUP.equals( child.getNodeName() ) ) {
					String groupMemento = child.getAttribute( ATTR_REGISTER_GROUP_MEMENTO );
					CRegisterGroup group = new CRegisterGroup( getDebugTarget() );
					try {
						group.initializeFromMemento( groupMemento );
						doAddRegisterGroup( group );
					}
					catch( CoreException e ) {
						// skip this group
					}
				}
			}
			childNode = childNode.getNextSibling();
		}
	}

	private void initializeDefaults() {
		String current = null;
		int startIndex = 0;
		for ( int i = 0; i < fRegisterDescriptors.length; ++i ) {
			CRegisterDescriptor d = (CRegisterDescriptor)fRegisterDescriptors[i];
			if (  current != null && d.getGroupName().compareTo( current ) != 0 ) {
				IRegisterDescriptor[] descriptors = new IRegisterDescriptor[i - startIndex];
				System.arraycopy( fRegisterDescriptors, startIndex, descriptors, 0, descriptors.length );
				fRegisterGroups.add( new CRegisterGroup( getDebugTarget(), current, descriptors ) );
				startIndex = i;
			}
			current = d.getGroupName();
		}
		if ( startIndex < fRegisterDescriptors.length - 1 ) {
			IRegisterDescriptor[] descriptors = new IRegisterDescriptor[fRegisterDescriptors.length - startIndex];
			System.arraycopy( fRegisterDescriptors, startIndex, descriptors, 0, descriptors.length );
			fRegisterGroups.add( new CRegisterGroup( getDebugTarget(), current, descriptors ) );
		}
	}

	protected synchronized void doAddRegisterGroup( IRegisterGroup  group ) {
		fRegisterGroups.add( group );
	}

	public void save() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS, getMemento() );
			wc.doSave();
		}
		catch( CoreException e ) {
			CDebugCorePlugin.log( e );
		}
	}

	private String getMemento() throws CoreException {
		if ( fRegisterGroups == null )
			return ""; //$NON-NLS-1$
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement( ELEMENT_REGISTER_GROUP_LIST );
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() ) {
			CRegisterGroup group = (CRegisterGroup)it.next();
			Element child = document.createElement( ELEMENT_REGISTER_GROUP );
			child.setAttribute( ATTR_REGISTER_GROUP_MEMENTO, group.getMemento() );
			element.appendChild( child );			
		}
		document.appendChild( element );
		return DebugPlugin.serializeDocument( document );
	}

	private void abort( String message, Throwable exception ) throws CoreException {
		IStatus status = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, message, exception );
		throw new CoreException( status );
	}

	public IRegisterDescriptor findDescriptor( String groupName, String name ) {
		for ( int i = 0; i < fRegisterDescriptors.length; ++i ) {
			IRegisterDescriptor d = fRegisterDescriptors[i];
			if ( groupName.equals( d.getGroupName() ) && name.equals( d.getName() ) )
				return d;
		}
		return null;
	}
}
