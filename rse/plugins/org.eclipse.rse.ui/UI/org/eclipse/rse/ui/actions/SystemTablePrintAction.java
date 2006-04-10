/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.actions;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.views.properties.IPropertyDescriptor;



/**
 * This is the action for printing the contents of the table view
 */
public class SystemTablePrintAction extends SystemBaseAction
{

	private int[] _columnWidths = null;
	private int[] _columnAlignments = null;

	private boolean bPrintSelection;
	private boolean bPageRange;

	private int endLine;
	private int bottomMargin = 100;
	private int leftMargin = 100;
	private int rightMargin = 100;
	private int topMargin = 100;

	private String sPrintOutputName = null;
	private String sPageTitle = null;
	private String sTableTitle = null;
	private String sColumnHeader = null;
	private String sUnderLine = null;
	private String sEndOfListing = null;

	private int pageNumber = 1;
	private boolean startedPage = false;
	int startPage;
	int endPage;

	private int pageHeight;
	private int pageWidth;
	private int x;
	private int y = 0;
	private int w;
	private int textHeight;

	private Printer printer;
	private boolean bPrintPage;
	private GC g;

	private SystemTableView _viewer = null;
	private String _title = null;
	private boolean _hasColumns = false;

	/**
	 * Constructor.
	 * @param title the title for the print document
	 * @param viewer the viewer to print the contents of
	 */
	public SystemTablePrintAction(String title, SystemTableView viewer)
	{
		super(SystemResources.ACTION_PRINTLIST_LABEL, null);
		setToolTipText(SystemResources.ACTION_PRINTLIST_TOOLTIP);
		setTableView(title, viewer);
	}

	/**
	 * Sets the title for the print document and the table view to print from
	 * @param title the title for the print document
	 * @param viewer the viewer to print the contents of
	 */
	public void setTableView(String title, SystemTableView viewer)
	{
		_title = title;
		_viewer = viewer;
	}

	/**
	 * Called to check whether this action should be enabled.
	 */
	public void checkEnabledState()
	{
		if (_viewer != null && _viewer.getInput() != null)
		{
			setEnabled(true);
		}
		else
		{
			setEnabled(false);
		}
	}

	/**
	 * Called when the user chooses to print
	 */
	public void run()
	{
		// page format info
		DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String sCurrentDate = dateFormatter.format(new Date());

		sPrintOutputName = SystemResources.RESID_TABLE_PRINTLIST_TITLE;
		sPageTitle = sPrintOutputName;
		sPageTitle = sPageTitle + sCurrentDate;

		// Table title
		sTableTitle = _title;

		/*============================*/
		/*  Present the print dialog  */
		/*============================*/
		PrintDialog printDialog = new PrintDialog(_viewer.getShell());

		PrinterData printerData = printDialog.open();
		if (printerData == null) // user cancelled the print job?
		{
			return;
		}
		// get updated settings from the print dialog
		bPrintSelection = (printerData.scope & PrinterData.SELECTION) != 0;
		bPageRange = printerData.scope == PrinterData.PAGE_RANGE;

		Table table = _viewer.getTable();

		TableItem[] printItems = table.getItems();
		if (bPrintSelection)
		{
			printItems = table.getSelection();
			endLine = printItems.length;
			if (endLine == 0)
				return; // nothing to print      	
		}
		else if (bPageRange)
		{
			endLine = printItems.length;
			startPage = printerData.startPage;
			endPage = printerData.endPage;
			if (endPage < startPage)
				return; // nothing to print
		}

		/*===================*/
		/*  do the printing  */
		/*===================*/
		// start print job
		printer = new Printer(printerData);

		if (!printer.startJob(sPrintOutputName))
		{
			printer.dispose();
			return;
		}

		Rectangle clientArea = printer.getClientArea();

		pageHeight = clientArea.height;
		pageWidth = clientArea.width;
		g = new GC(printer);

		textHeight = g.getFontMetrics().getHeight();

		/*----------------------------------------*/
		/*  go through all the lines to print...  */
		/*----------------------------------------*/
		pageNumber = 1;
		startedPage = false;

		// scale factor			
		int scaleFactor = 1;
		Rectangle tableClientArea = table.getClientArea();
		int tableWidth = tableClientArea.width - 5;
		if (tableWidth > pageWidth)
		{
			scaleFactor = tableWidth / pageWidth;
		}

		int columnCount = table.getColumnCount();
		if (columnCount > 1)
		{
			_hasColumns = true;
		}
		else
		{
			_hasColumns = false;
		}

		// header info

		getColumnInfo(scaleFactor);
		sColumnHeader = getColumnHeader();
		sUnderLine = getHeaderSeparator();

		sEndOfListing = getTableFooter();

		for (int i = 0; i < printItems.length; i++)
		{
			TableItem item = printItems[i];
			Object data = item.getData();

			String line = getLine(data, columnCount);

			printLine(line);
		}

		printLine(" ");
		printLine(sEndOfListing);

		/*=======================*/
		/*  finish up print job  */
		/*=======================*/
		g.dispose();

		printer.endJob();
		printer.dispose();

		System.gc();
		return;
	}
	
   /*
	* Print one line
	*/
	private void printLine(String text)
	{
		do // until the text of one line is printed
		{
			// start a new page
			if (!startedPage)
			{
				if (bPageRange)
				{
					if (pageNumber >= startPage && pageNumber <= endPage)
						bPrintPage = true;
					else
						bPrintPage = false;
				}
				else
					bPrintPage = true;

				startedPage = true;
				x = leftMargin;
				y = topMargin;
				if (bPrintPage)
				{
					printer.startPage();
					g.drawString(sPageTitle + pageNumber, x, y);

					y += textHeight * 2;

					g.drawString(sTableTitle, x, y);
					y += textHeight * 2;

					g.drawString(sColumnHeader, x, y);
					y += textHeight;

					g.drawString(sUnderLine, x, y);
					y += textHeight;
				}
				else
				{
					y = topMargin + textHeight * 6;
				}
				pageNumber++;
			}
			// start at beginning of the line
			x = leftMargin;

			if (text != null)
			{
				int l = text.length();
				while (l > 0)
				{
					w = g.stringExtent(text.substring(0, l)).x;
					if (x + w <= pageWidth - rightMargin)
					{
						break;
					}
					l--;
				}
				String remainingText = null; // text spillin' to next print line
				if (l > 0 && l < text.length())
				{
					remainingText = text.substring(l);
					text = text.substring(0, l);
				}
				if (bPrintPage)
					g.drawString(text, x, y);
				text = remainingText; // still to print text spillin' over edge
			}
			// finished a print line, go to next
			y += textHeight;
			// done with this page (a new line height doesn't fit)?
			if (y + textHeight > pageHeight - bottomMargin)
			{
				if (bPrintPage)
					printer.endPage();
				startedPage = false;
			}
		}
		while (text != null); //end do       
	}

	private void getColumnInfo(int scaleFactor)
	{
		// scale widths
		Table table = _viewer.getTable();
		if (table.getColumnCount() > 1)
		{
			_hasColumns = true;
		}
		else
		{
			_hasColumns = false;
		}

		if (_hasColumns)
		{
			_columnWidths = new int[table.getColumnCount()];
			_columnAlignments = new int[table.getColumnCount()];

			for (int i = 0; i < table.getColumnCount(); i++)
			{
				TableColumn column = table.getColumn(i);
				int width = column.getWidth();
				_columnWidths[i] = width / 9;
				_columnAlignments[i] = column.getAlignment();
			}
		}
	}

	private String getColumnHeader()
	{
		StringBuffer sbColumnHeader = new StringBuffer("");
		sbColumnHeader.append(getBlankLine());

		if (_hasColumns)
		{
			IPropertyDescriptor[] descriptors = _viewer.getVisibleDescriptors(_viewer.getInput());
			sbColumnHeader.insert(0, SystemPropertyResources.RESID_PROPERTY_NAME_LABEL);

			int offset = _columnWidths[0];
			sbColumnHeader.insert(offset, " ");
			offset++;

			for (int i = 0; i < descriptors.length; i++)
			{
				String label = descriptors[i].getDisplayName();
				int columnWidth = _columnWidths[i + 1];
				int labelWidth = label.length();

				if (_columnAlignments[i + 1] == SWT.LEFT)
				{
					if (labelWidth > columnWidth)
					{
						label = label.substring(0, columnWidth - 3);
						label += "...";
					}
					sbColumnHeader.insert(offset, label);
				}
				else
				{

					int rightOffset = offset + (columnWidth - labelWidth) - 1;

					if (rightOffset < offset)
					{
						int delta = (offset - rightOffset) - 3;
						label = label.substring(0, delta);
						label += "...";
						rightOffset = offset;
					}

					sbColumnHeader.insert(rightOffset, label);
				}

				offset += columnWidth;
				sbColumnHeader.insert(offset, " ");
				offset++;
			}
		}
		return sbColumnHeader.toString();
	}

	private String getHeaderSeparator()
	{
		StringBuffer separator = new StringBuffer("");
		if (_hasColumns)
		{
			for (int i = 0; i < _columnWidths.length; i++)
			{
				int width = _columnWidths[i];
				for (int t = 0; t < width; t++)
				{
					separator.append("-");
				}

				separator.append(" ");
			}
		}

		return separator.toString();
	}

	private String getTableFooter()
	{
		String footer = "           * * * * *   E N D   O F   L I S T I N G   * * * * *";
		return footer;
	}

	private int getTotalWidth()
	{
		int totalWidth = 0;
		if (_hasColumns)
		{
			for (int i = 0; i < _columnWidths.length; i++)
			{
				totalWidth += _columnWidths[i];
			}
		}
		else
		{
			totalWidth = pageWidth;
		}

		return totalWidth;
	}

	private String getBlankLine()
	{
		StringBuffer blankLine = new StringBuffer();

		int totalWidth = getTotalWidth();
		for (int b = 0; b < totalWidth; b++)
		{
			blankLine.append(" ");
		}

		return blankLine.toString();
	}

	private String getLine(Object object, int numColumns)
	{
		StringBuffer line = new StringBuffer("");

		SystemTableViewProvider lprovider = (SystemTableViewProvider) _viewer.getLabelProvider();
		if (_hasColumns)
		{
			line.append(getBlankLine());
			int offset = 0;
			for (int column = 0; column < numColumns; column++)
			{
				String columnText = lprovider.getColumnText(object, column);
				int labelWidth = columnText.length();

				int columnWidth = _columnWidths[column];
				if (_columnAlignments[column] == SWT.LEFT)
				{
					if (labelWidth > columnWidth)
					{
						columnText = columnText.substring(0, columnWidth - 3);
						columnText += "...";
					}

					line.insert(offset, columnText);

				}
				else
				{
					int rightOffset = offset + (columnWidth - labelWidth) - 1;
					if (rightOffset < offset)
					{
						int delta = (offset - rightOffset) + 3;
						columnText = columnText.substring(0, labelWidth - delta);
						columnText += "...";
						rightOffset = offset;
					}

					line.insert(rightOffset, columnText);
				}

				offset += columnWidth;
				line.insert(offset, " ");
				offset++;
			}
		}
		else
		{
			String columnText = lprovider.getColumnText(object, 0);
			line.append(columnText);
		}

		return line.toString();
	}
}