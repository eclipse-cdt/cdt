/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * A superclass for all variable classes.
 * 
 * @since Aug 9, 2002
 */
public abstract class CVariable extends CDebugElement 
								implements IVariable,
										   ICEventListener
{
	/**
	 * Cache of current value - see #getValue().
	 */
	private CValue fValue;

	/**
	 * Counter corresponding to this variable's debug target
	 * suspend count indicating the last time this value 
	 * changed. This variable's value has changed on the
	 * last suspend event if this counter is equal to the
	 * debug target's suspend count.
	 */
	private int fLastChangeIndex = -1;

	/**
	 * Constructor for CVariable.
	 * @param target
	 */
	public CVariable( CDebugTarget target )
	{
		super( target );
		getCDISession().getEventManager().addEventListener( this );
	}

	/**
	 * Returns the current value of this variable. The value
	 * is cached, but on each access we see if the value has changed
	 * and update if required.
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException
	{
		ICValue currentValue = getCurrentValue();
		if ( fValue == null )
		{
			fValue = CValue.createValue( (CDebugTarget)getDebugTarget(), currentValue );
		}
		else
		{
			ICValue previousValue = fValue.getUnderlyingValue();
			if ( currentValue == previousValue )
			{
				return fValue;
			}
			if ( previousValue == null || currentValue == null )
			{
				fValue = CValue.createValue( (CDebugTarget)getDebugTarget(), currentValue );
				setChangeCount( ((CDebugTarget)getDebugTarget()).getSuspendCount());
			}
			else if ( !previousValue.equals( currentValue ) )
			{
				fValue = CValue.createValue( (CDebugTarget)getDebugTarget(), currentValue );
				setChangeCount( ((CDebugTarget)getDebugTarget()).getSuspendCount());
			}
		}
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException
	{
		return getChangeCount() == ((CDebugTarget)getDebugTarget()).getSuspendCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(String)
	 */
	public void setValue( String expression ) throws DebugException
	{
		notSupported( "Variable does not support value modification." );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(IValue)
	 */
	public void setValue( IValue value ) throws DebugException
	{
		notSupported( "Variable does not support value modification." );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(String)
	 */
	public boolean verifyValue( String expression ) throws DebugException
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue( IValue value ) throws DebugException
	{
		return false;
	}

	/**
	 * Returns this variable's underlying CDI value.
	 */
	protected abstract ICValue retrieveValue() throws DebugException, CDIException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( IVariable.class ) )
			return this;
		return super.getAdapter( adapter );
	}

	/**
	 * Returns this variable's current underlying CDI value.
	 * Subclasses must implement #retrieveValue() and do not
	 * need to guard against CDIException, as this method
	 * handles them.
	 *
	 * @exception DebugException if unable to access the value
	 */
	protected final ICValue getCurrentValue() throws DebugException
	{
		try
		{
			return retrieveValue();
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occurred retrieving value.", new String[] { e.toString() } ), e );
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception			
			return null;
		}
	}

	/**
	 * Sets this variable's change counter to the specified value
	 * 
	 * @param count new value
	 */
	protected void setChangeCount( int count )
	{
		fLastChangeIndex = count;
	}

	/**
	 * Returns this variable's change counter. This corresponds to the
	 * last time this variable changed.
	 * 
	 * @return this variable's change counter
	 */
	protected int getChangeCount()
	{
		return fLastChangeIndex;
	}

	/**
	 * Returns the last known value for this variable
	 */
	protected ICValue getLastKnownValue()
	{
		if ( fValue == null )
		{
			return null;
		}
		else
		{
			return fValue.getUnderlyingValue();
		}
	}
	
	protected void dispose()
	{
		getCDISession().getEventManager().removeEventListener( this );
	}
}
