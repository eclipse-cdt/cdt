/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 25, 2002
 */
public class CDebugUtils
{
	public static boolean question( IStatus status, Object source )
	{
		Boolean result = new Boolean( false );
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null )
		{
			try
			{
				result = (Boolean)handler.handleStatus( status, source );
			}
			catch( CoreException e )
			{
			}
		}
		return result.booleanValue();
	}
	
	public static void info( IStatus status, Object source )
	{
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null )
		{
			try
			{
				handler.handleStatus( status, source );
			}
			catch( CoreException e )
			{
			}
		}
	}
	
	public static void error( IStatus status, Object source )
	{
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null )
		{
			try
			{
				handler.handleStatus( status, source );
			}
			catch( CoreException e )
			{
			}
		}
	}
	
	public static String toHexAddressString( long address )
	{
		String addressString = Long.toHexString( address );
		StringBuffer sb = new StringBuffer( 10 );
		sb.append( "0x" );
		for ( int i = 0; i < 8 - addressString.length(); ++i )
		{
			sb.append( '0' );
		}
		sb.append( addressString );
		return sb.toString();
	}
}
