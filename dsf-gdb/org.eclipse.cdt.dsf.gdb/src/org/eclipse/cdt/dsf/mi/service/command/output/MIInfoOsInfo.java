/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 4.1
 */
public class MIInfoOsInfo extends MIInfo {
	
	public MIInfoOsInfo(MIOutput record) {
		super(record);
		parse();
	}
	
	public String[] getColumnNames()
	{
		return columnNames;
	}
	
	public boolean isColumnInteger(int i)
	{
		return columnIsInteger[i];
	}
	
	public String[][] getContent()
	{
		return content;
	}
	
	private void parse()
	{
		MITuple table = (MITuple) get(getMIOutput(), "OSDataTable"); //$NON-NLS-1$

		MIList header = (MIList) get(table, "hdr");  //$NON-NLS-1$
		columnNames = new String[header.getMIValues().length];
		int i = 0;
		for (MIValue v : header.getMIValues()) {
			MITuple column = (MITuple) v;
			String columnName = ((MIConst) get(column, "colhdr")).getString();  //$NON-NLS-1$
			columnNames[i++] = Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1);
		}
		
		columnIsInteger = new boolean[columnNames.length];

		MIList body = (MIList) get(table, "body");  //$NON-NLS-1$
		if (body == null)
		{
			content = new String[0][];
			return;
		}
		
		boolean[] columnHasInteger = new boolean[columnNames.length];
		boolean[] columnHasOther = new boolean[columnNames.length];

		content = new String[body.getMIResults().length][];
		i = 0;
		for (MIResult r : body.getMIResults()) {
			MITuple row = (MITuple) r.getMIValue();
			assert row.getMIResults().length == columnNames.length;
			String[] rowStrings = new String[row.getMIResults().length];
			int j = 0;
			for (MIResult r2 : row.getMIResults())
			{
				rowStrings[j] = ((MIConst) r2.getMIValue()).getString();
				if (!columnHasOther[j])
				{
					try {
						Integer.parseInt(rowStrings[j]);
						columnHasInteger[j] = true;
					}
					catch(NumberFormatException e) {
						columnHasOther[j] = true;
					}
				}
					
				++j;
			}
			content[i++] = rowStrings;
		}
		
		
		for (int j = 0; j < columnNames.length; ++j) {
			columnIsInteger[j] = columnHasInteger[j] && !columnHasOther[j];
		}		
	}
	
	private MIValue get(MIResult[] results, String name) {
		for (MIResult r : results)
			if (r.getVariable().equals(name))
				return r.getMIValue();
		return null;
	}

	private MIValue get(MIOutput output, String name) {
		return get(output.getMIResultRecord().getMIResults(), name);
	}

	private MIValue get(MITuple tuple, String name) {
		return get(tuple.getMIResults(), name);
	}
	
	private String[] columnNames;
	private boolean[] columnIsInteger;
	private String[][] content;


}
