/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
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
	private CStackFrame fCurrentStackFrame = null;

	/**
	 * Whether the registers need refreshing
	 */
	private boolean fRefresh = true;

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
		return getRegisters0().size() > 0;
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
		else if ( fRefresh )
		{
			updateRegisters();
		}
		fRefresh = false;
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
	
	protected void refresh( CStackFrame stackFrame )
	{
		setCurrentStackFrame( stackFrame );
		fRefresh = true;
	}
	
	private ICDIRegister[] getCDIRegisters() throws DebugException
	{
		ICDIRegister[] result = new ICDIRegister[0];
		CStackFrame currentFrame = getCurrentStackFrame();
		if ( currentFrame != null )
		{
			try
			{
				result = getCurrentStackFrame().getCDIStackFrame().getRegisters( fRegisterObjects );
			}
			catch( CDIException e )
			{
				targetRequestFailed( e.getMessage(), null );
			}
		}
		return result;
	}
	
	protected void setCurrentStackFrame( CStackFrame stackFrame )
	{
		fCurrentStackFrame = stackFrame;
	}
	
	protected CStackFrame getCurrentStackFrame()
	{
		return fCurrentStackFrame;
	}
	
	private void updateRegisters() throws DebugException
	{
		ICDIRegister[] cdiRegisters = getCDIRegisters();
		int index = 0;
		while( index < fRegisters.size() && index < cdiRegisters.length )
		{
			CRegister register = (CRegister)fRegisters.get( index );
			if ( !cdiRegisters[index].equals( register.getCDIVariable() ) )
			{
				register.setCDIVariable( cdiRegisters[index] );
			}
			index++;
		}
	}
}
