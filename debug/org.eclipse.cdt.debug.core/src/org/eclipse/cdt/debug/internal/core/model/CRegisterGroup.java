/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.CRegisterManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IRegister;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represents a group of registers.
 */
public class CRegisterGroup extends CDebugElement implements IPersistableRegisterGroup, IEnableDisableTarget {

	private static final String ELEMENT_REGISTER_GROUP = "registerGroup"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_ENABLED = "enabled"; //$NON-NLS-1$

	private static final String ELEMENT_REGISTER = "register"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_ORIGINAL_GROUP_NAME = "originalGroupName"; //$NON-NLS-1$

	private String fName;

	private IRegisterDescriptor[] fRegisterDescriptors;

	private IRegister[] fRegisters;

	private boolean fIsEnabled = true;
	
	private boolean fDisposed = false;

	/**
	 * Constructor for CRegisterGroup.
	 */
	public CRegisterGroup( CDebugTarget target ) {
		super( target );
	}

	/**
	 * Constructor for CRegisterGroup.
	 */
	public CRegisterGroup( CDebugTarget target, String name, IRegisterDescriptor[] descriptors ) {
		super( target );
		fName = name;
		fRegisterDescriptors = descriptors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getRegisters()
	 */
	public IRegister[] getRegisters() throws DebugException {
		if ( fDisposed )
			return new IRegister[0];
		if ( fRegisters == null ) {
			fRegisters = new IRegister[fRegisterDescriptors.length];
			for( int i = 0; i < fRegisters.length; ++i ) {
				fRegisters[i] = new CRegister( this, fRegisterDescriptors[i] );
			}
		}
		return fRegisters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#hasRegisters()
	 */
	public boolean hasRegisters() throws DebugException {
		return ( fRegisterDescriptors.length > 0 );
	}

	public void dispose() {
		fDisposed = true;
		invalidate();
	}

	public void targetSuspended() {
		if (fRegisters == null) {
			return;
		}
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null && ((CRegister)fRegisters[i]).hasErrors() ) {
				((CRegister)fRegisters[i]).dispose();
				fRegisters[i] = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( IEnableDisableTarget.class.equals( adapter ) )
			return this;
		return super.getAdapter( adapter );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#isEnabled()
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#setEnabled(boolean)
	 */
	public void setEnabled( boolean enabled ) throws DebugException {
		if ( fRegisters != null ) {
			synchronized( fRegisters ) {
				if ( fRegisters != null ) {
					for ( int i = 0; i < fRegisters.length; ++i ) {
						if ( fRegisters[i] instanceof CRegister ) {
							((CRegister)fRegisters[i]).setEnabled( enabled );
						}
					}
				}
			}
		}
		fIsEnabled = enabled;
		fireChangeEvent( DebugEvent.CONTENT );
	}

	public String getMemento() throws CoreException {
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement( ELEMENT_REGISTER_GROUP );
		element.setAttribute( ATTR_REGISTER_GROUP_NAME, getName() );
		element.setAttribute( ATTR_REGISTER_GROUP_ENABLED, isEnabled() ? Boolean.TRUE.toString() : Boolean.FALSE.toString() );
		for ( int i = 0; i < fRegisterDescriptors.length; ++i ) {
			Element child = document.createElement( ELEMENT_REGISTER );
			child.setAttribute( ATTR_REGISTER_NAME, fRegisterDescriptors[i].getName() );
			child.setAttribute( ATTR_REGISTER_ORIGINAL_GROUP_NAME, fRegisterDescriptors[i].getGroupName() );
			element.appendChild( child );
		}
		document.appendChild( element );
		return DebugPlugin.serializeDocument( document );
	}

	public void initializeFromMemento( String memento ) throws CoreException {
		Node node = DebugPlugin.parseDocument( memento );
		if ( node.getNodeType() != Node.ELEMENT_NODE ) {
			abort( CoreModelMessages.getString( "CRegisterGroup.0" ), null ); //$NON-NLS-1$
		}
		Element element = (Element)node;
		if ( !ELEMENT_REGISTER_GROUP.equals( element.getNodeName() ) ) {
			abort( CoreModelMessages.getString( "CRegisterGroup.1" ), null ); //$NON-NLS-1$
		}
		String groupName = element.getAttribute( ATTR_REGISTER_GROUP_NAME );
		if ( groupName == null || groupName.length() == 0 ) {
			abort( CoreModelMessages.getString( "CRegisterGroup.2" ), null ); //$NON-NLS-1$
		}
		String e = element.getAttribute( ATTR_REGISTER_GROUP_ENABLED );
		boolean enabled = Boolean.valueOf( e ).booleanValue();
		CRegisterManager rm = getRegisterManager();
		ArrayList list = new ArrayList();
		Node childNode = element.getFirstChild();
		while( childNode != null ) {
			if ( childNode.getNodeType() == Node.ELEMENT_NODE ) {
				Element child = (Element)childNode;
				if ( ELEMENT_REGISTER.equals( child.getNodeName() ) ) {
					String name = child.getAttribute( ATTR_REGISTER_NAME );
					String originalGroupName = child.getAttribute( ATTR_REGISTER_ORIGINAL_GROUP_NAME );
					if ( name == null || name.length() == 0 || originalGroupName == null || originalGroupName.length() == 0 ) {
						abort( CoreModelMessages.getString( "CRegisterGroup.3" ), null ); //$NON-NLS-1$
					}
					else {
						IRegisterDescriptor d = rm.findDescriptor( originalGroupName, name );
						if ( d != null )
							list.add( d );
						else
							CDebugCorePlugin.log( CoreModelMessages.getString( "CRegisterGroup.4" ) ); //$NON-NLS-1$
					}
				}
			}
			childNode = childNode.getNextSibling();
		}
		setName( groupName );
		fRegisterDescriptors = (IRegisterDescriptor[])list.toArray( new IRegisterDescriptor[list.size()] );
		setEnabled( enabled );
	}

	private void abort( String message, Throwable exception ) throws CoreException {
		IStatus status = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, message, exception );
		throw new CoreException( status );
	}
	
	private void setName( String name ) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup#setRegisterDescriptors(org.eclipse.cdt.debug.core.model.IRegisterDescriptor[])
	 */
	public void setRegisterDescriptors( IRegisterDescriptor[] registerDescriptors ) {
		invalidate();
		fRegisterDescriptors = registerDescriptors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup#getRegisterDescriptors()
	 */
	public IRegisterDescriptor[] getRegisterDescriptors() {
		return fRegisterDescriptors;
	}

	private CRegisterManager getRegisterManager() {
		return (CRegisterManager)getDebugTarget().getAdapter( CRegisterManager.class );
	}

	private void invalidate() {
		if (fRegisters == null) {
			return;
		}
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null ) {
				((CRegister)fRegisters[i]).dispose();
			}
		}
		fRegisters = null;
	}
}
