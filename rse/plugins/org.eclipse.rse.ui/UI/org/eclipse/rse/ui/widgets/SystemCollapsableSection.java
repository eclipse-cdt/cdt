/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

/**
 * Class to provide a collapsible composite that can be collapsed
 * to hide some controls   
 */
public class SystemCollapsableSection extends Composite implements MouseListener, PaintListener
{

	public static final String Copyright =
		"(C) Copyright IBM Corp. 2002, 2003.  All Rights Reserved.";

	protected boolean _bCollapsed = false;
	protected boolean _bMouseOver = false;
	protected Composite _compositePage = null;
	protected String _strText = null;
	protected String _strExpandedText = null;
	protected String _strCollapsedText = null;
	protected String _strExpandedToolTip = null;
	protected String _strCollapsedToolTip = null;
	protected Label _labelTitle = null;

	protected static Color _colorCollapsable = null;

	// yantzi: added so we can have a collapse / expand action in the iSeries table view for 
	// accessability reasons.
	private List listeners = new ArrayList(5);
	
	/**
	 *
	 */
	protected class RTwisteeLayout extends Layout
	{

		/**
		 *
		 */
		protected Point computeSize(
			Composite composite,
			int wHint,
			int hHint,
			boolean flushCache)
		{
			checkWidget();

			Point ptSize = getTitleSize(_strText);
			Point ptPageSize =
				_compositePage.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

			ptSize.x = Math.max(ptSize.x, ptPageSize.x + 8);

			if (_bCollapsed == false)
				ptSize.y += ptPageSize.y;

			return ptSize;
		}

		/**
		 * Layout.
		 */
		protected void layout(Composite composite, boolean flushCache)
		{
			Point ptTitleSize = getTitleSize(_strText);
			Point ptLocation = getLocation();

			if (_bCollapsed == true)
			{
				Rectangle rectClient = getClientArea();
				Point ptPageSize =
					new Point(
						rectClient.width - 16,
						rectClient.height - ptTitleSize.y);
				_compositePage.setBounds(16, ptTitleSize.y, ptPageSize.x, 4);
				setSize(
					Math.max(ptTitleSize.x, ptPageSize.x + 16),
					ptTitleSize.y);
			}

			else
			{
				Rectangle rectClient = getClientArea();
				Point ptPageSize =
					new Point(
						rectClient.width - 16,
						rectClient.height - ptTitleSize.y);
				//	  Point ptPageSize = _compositePage.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
				_compositePage.setBounds(
					16,
					ptTitleSize.y,
					ptPageSize.x,
					ptPageSize.y);
				setSize(
					Math.max(ptTitleSize.x, ptPageSize.x + 16),
					ptTitleSize.y + ptPageSize.y);
			}
		}
	}

	/**
	 * Constructor
	 */
	public SystemCollapsableSection(Composite compositeParent)
	{

		super(compositeParent, SWT.NULL);

		if (_colorCollapsable == null)
		{
			Display display = Display.getCurrent();
			_colorCollapsable = new Color(display, 0, 140, 140);
		}

		setLayout(new RTwisteeLayout());

		// Page content
		//-------------
		_compositePage = new Composite(this, SWT.NULL);

		GridData gridData = new GridData();
		setLayoutData(gridData);

		addPaintListener(this);
		addMouseListener(this);
	}

	/**
	 * Get the actual composite inside the collapsible section to
	 * be usde for filling it up with controls 
	 */
	public Composite getPageComposite()
	{
		return _compositePage;
	}

	/**
	 * Compute the title area size.
	 */
	private Point getTitleSize(String strText)
	{

		if (strText == null || strText.length() == 0)
		{
			strText = "MMMMMMMMMMMM";
		}

		GC gc = new GC(this);

		Point ptSize = gc.textExtent(strText);
		ptSize.y = Math.max(ptSize.y, gc.getFontMetrics().getHeight());

		ptSize.x += 20;
		ptSize.y = Math.max(ptSize.y, 20);

		gc.dispose();

		return ptSize;
	}

	/**
	 * Return the collapse state
	 */
	public boolean getCollapsed()
	{
		return _bCollapsed;
	}

	/**
	 * Get the default title text
	 */
	public String getText()
	{
		return _strText;
	}

	/**
	 *
	 */
	public void mouseDoubleClick(MouseEvent e)
	{

	}

	/**
	 *
	 */
	public void mouseDown(MouseEvent e)
	{

	}

	/**
	 * Handle the collapse or expand request from the mouse up event
	 */
	public void mouseUp(MouseEvent e)
	{

		_bCollapsed = _bCollapsed == true ? false : true;

		if (_bCollapsed)
		{
			setToolTipText(_strCollapsedToolTip);
		}
		else
		{
			setToolTipText(_strExpandedToolTip);
		}

		List list = new ArrayList();

		Composite compositeParent = this;

		do
		{
		    list.add(compositeParent);
			compositeParent = compositeParent.getParent();
		}
		while (compositeParent instanceof Shell == false);

		for (int i = list.size() - 1; i >= 0; --i)
		{
			compositeParent = (Composite) list.get(i);
			compositeParent.layout();
		}

		fireCollapseEvent(_bCollapsed);
		//	composite.redraw();
	}

	/**
	 * Paint the control
	 */
	public void paintControl(PaintEvent e)
	{

		paintCollapsable(e.gc, 0, 2, _bCollapsed);

		if (_bCollapsed)
		{
			setToolTipText(_strCollapsedToolTip);
			if (_strCollapsedText != null)
				_strText = _strCollapsedText;			
		}
		else
		{
			setToolTipText(_strExpandedToolTip);
			if (_strExpandedText != null)
				_strText = _strExpandedText;
		}

		if (_strText == null)
			return;

		e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		e.gc.drawString(_strText, 17, 0, true);
	}

	/**
	 * Paints the two states of a collapsable indicator of a collapsable container.
	 */
	public static void paintCollapsable(
		GC gc,
		int iX,
		int iY,
		boolean bCollapsed)
	{
		
		// Not collapsed: v
		//-----------------

		if (bCollapsed == false)
		{
			gc.setForeground(_colorCollapsable);

			int iA = iX;
			int iB = iY + 3;
			gc.drawLine(iA, iB, iA + 10, iB);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA + 8, iB);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA + 6, iB);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA + 4, iB);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA + 2, iB);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA, iB);

			iA = iX;
			iB = iY;
		}

		// Collapsed: >
		//-------------
		else
		{
			gc.setForeground(_colorCollapsable);

			int iA = iX + 2;
			int iB = iY;

			gc.drawLine(iA, iB, iA, iB + 10);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA, iB + 8);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA, iB + 6);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA, iB + 4);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA, iB + 2);
			iA++;
			iB++;
			gc.drawLine(iA, iB, iA, iB);
		}
	}

	/**
	 * Set the section to be collapsed
	 */
	public void setCollapsed(boolean bCollapsed)
	{
		
		_bCollapsed = bCollapsed;
		if (_bCollapsed)
			setToolTipText(_strCollapsedToolTip);
		else
			setToolTipText(_strExpandedToolTip);
		
		redraw();
		
		fireCollapseEvent(bCollapsed);
	}

	/**
	 * Set the default text title
	 */
	public void setText(String strText)
	{
		_strText = strText;
		redraw();
	}
	/**
	 * Set the title to be displayed when the section is expanded 
	 */
	public void setExpandedText(String strText)
	{
		_strExpandedText = strText;		
	}
	/**
	 * Set the title to be displayed when the section is collapsed 
	 */
	public void setCollapsedText(String strText)
	{
		_strCollapsedText = strText;		
	}
	
	/**
	 * Set the two tooltips used in expanded state and collapsed state
	 * @param String - tooltip for the expanded state. e.g. Click line to collapse the section
	 * @param String - tooltip for the collapsed state. e.g. Click line to expand the section  
	 */
	public void setToolTips(String strExpandedToolTip, String strCollapsedToolTip)
	{
		_strCollapsedToolTip = strCollapsedToolTip;
		_strExpandedToolTip = strExpandedToolTip;
	}
	
	/**
	 * Add a collapse / expand event listener
	 */
	public void addCollapseListener(ISystemCollapsableSectionListener listener)
	{
	    if (!listeners.contains(listener))
	    {
	        listeners.add(listener);
	    }
	}

	/**
	 * Remove a collapse / expand event listener
	 */
	public void removeCollapseListener(ISystemCollapsableSectionListener listener)
	{
        listeners.remove(listener);
	}
	
	/** 
	 * Notify collapse / expand listeners of an event
	 */
	private void fireCollapseEvent(boolean collapsed)
	{
	    for (int i = 0; i < listeners.size(); i++)
	    {
	        ((ISystemCollapsableSectionListener) listeners.get(i)).sectionCollapsed(collapsed);
	    }
	}

}