/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.registers;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * Registers viewer. As the user steps through code, this
 * viewer renders registers that have changed with a 
 * different foreground color thereby drawing attention
 * to the values that have changed.
 * 
 * @since Jul 23, 2002
 */
public class RegistersViewer extends TreeViewer
{

	private Item fNewItem;
	
	/**
	 * Constructor for RegistersViewer.
	 * @param parent
	 */
	public RegistersViewer( Composite parent )
	{
		super( parent );
	}

	/**
	 * Constructor for RegistersViewer.
	 * @param parent
	 * @param style
	 */
	public RegistersViewer( Composite parent, int style )
	{
		super( parent, style );
	}

	/**
	 * Constructor for RegistersViewer.
	 * @param tree
	 */
	public RegistersViewer( Tree tree )
	{
		super( tree );
	}

	public void refresh()
	{
		super.refresh();

		if ( getSelection().isEmpty() && getNewItem() != null )
		{
			if ( !getNewItem().isDisposed() )
			{
				//ensure that new items are visible
				showItem( getNewItem() );
			}
			setNewItem( null );
		}
		//expandToLevel( 2 );
	}

	/**
	 * @see AbstractTreeViewer#newItem(Widget, int, int)
	 */
	protected Item newItem( Widget parent, int style, int index )
	{
		if ( index != -1 )
		{
			//ignore the dummy items
			setNewItem( super.newItem( parent, style, index ) );
			return getNewItem();
		}
		return super.newItem( parent, style, index );
	}

	protected Item getNewItem()
	{
		return fNewItem;
	}

	protected void setNewItem( Item newItem )
	{
		fNewItem = newItem;
	}
}
