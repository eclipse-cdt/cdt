/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CRegisterGroup;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Enter type comment.
 * 
 * @since Mar 31, 2003
 */
public class CRegisterManager extends CUpdateManager implements ICRegisterManager
{
	/**
	 * Collection of register groups added to this target. Values are of type <code>CRegisterGroup</code>.
	 */
	private List fRegisterGroups;

	/**
	 * 
	 */
	public CRegisterManager( CDebugTarget target )
	{
		super( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( ICRegisterManager.class.equals( adapter ) )
			return this;
		if ( CRegisterManager.class.equals( adapter ) )
			return this;
		return super.getAdapter( adapter );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#dispose()
	 */
	public void dispose()
	{
		removeAllRegisterGroups();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#addRegisterGroup(org.eclipse.debug.core.model.IRegisterGroup)
	 */
	public void addRegisterGroup( IRegisterGroup group )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException
	{
		return (IRegisterGroup[])fRegisterGroups.toArray( new IRegisterGroup[fRegisterGroups.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#initialize()
	 */
	public void initialize()
	{
		fRegisterGroups = new ArrayList( 20 );
		boolean autoRefresh = CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_REGISTERS_AUTO_REFRESH );
		if ( getCDIManager() != null )
			getCDIManager().setAutoUpdate( autoRefresh );
		createMainRegisterGroup();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#removeAllRegisterGroups()
	 */
	public void removeAllRegisterGroups()
	{
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() )
		{
			((CRegisterGroup)it.next()).dispose();
		}
		fRegisterGroups.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#removeRegisterGroup(org.eclipse.debug.core.model.IRegisterGroup)
	 */
	public void removeRegisterGroup( IRegisterGroup group )
	{
		fRegisterGroups.remove( group );
	}

	private void createMainRegisterGroup()
	{
		ICDIRegisterObject[] regObjects = null;
		try
		{
			regObjects = getDebugTarget().getCDISession().getRegisterManager().getRegisterObjects();
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}
		if ( regObjects != null )
		{
			fRegisterGroups.add( new CRegisterGroup( getDebugTarget(), "Main", regObjects ) ); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#reset()
	 */
	public void reset()
	{
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() )
		{
			((CRegisterGroup)it.next()).resetChangeFlags();
		}
	}

	protected ICDIManager getCDIManager()
	{
		if ( getDebugTarget() != null )
		{
			return getDebugTarget().getCDISession().getRegisterManager();
		}
		return null;
	}

	public void targetSuspended() {
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() ) {
			((CRegisterGroup)it.next()).targetSuspended();
		}
	}
}
