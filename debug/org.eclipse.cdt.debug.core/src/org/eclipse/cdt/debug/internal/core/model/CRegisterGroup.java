/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 16, 2002
 */
public class CRegisterGroup extends CDebugElement implements IRegisterGroup
{
	private String fName;
	private ICDIRegisterObject[] fRegisterObjects;
	private List fRegisters;

	/**
	 * Constructor for CRegisterGroup.
	 * @param target
	 */
	public CRegisterGroup( CDebugTarget target, String name, ICDIRegisterObject[] regObjects )
	{
		super( target );
		fName = name;
		fRegisterObjects = regObjects;
		fRegisters = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getName()
	 */
	public String getName() throws DebugException
	{
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getRegisters()
	 */
	public IRegister[] getRegisters() throws DebugException
	{
		List list = getRegisters0();
		return (IRegister[])list.toArray( new IRegister[list.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#hasRegisters()
	 */
	public boolean hasRegisters() throws DebugException
	{
		return fRegisterObjects.length > 0;
	}

	private List getRegisters0() throws DebugException
	{
		if ( fRegisters == null || fRegisters.size() == 0 )
		{
			ICDIRegister[] regs = getCDIRegisters();
			fRegisters = new ArrayList( regs.length );
			for ( int i = 0; i < regs.length; ++i )
			{
				fRegisters.add( new CRegister( this, regs[i] ) );
			}
		}
		return fRegisters;
	}
	
	protected void dispose()
	{
		Iterator it = fRegisters.iterator();
		while( it.hasNext() )
		{
			((CRegister)it.next()).dispose();
		}
		fRegisters.clear();
	}

	private ICDIRegister[] getCDIRegisters() throws DebugException
	{
		ICDIRegister[] results = new ICDIRegister[fRegisterObjects.length];
		try
		{
			for ( int i = 0; i < fRegisterObjects.length; ++i )
			{
				results[i] = ((CDebugTarget)getDebugTarget()).getCDISession().getRegisterManager().createRegister( fRegisterObjects[i] );
			}
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return results;
	}
	
	protected void resetChangeFlags()
	{
		if ( fRegisters == null )
			return;
		try
		{
			Iterator it = fRegisters.iterator();
			while( it.hasNext() )
			{
				((CVariable)it.next()).setChanged( false );
			}
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
		}
	}
}
