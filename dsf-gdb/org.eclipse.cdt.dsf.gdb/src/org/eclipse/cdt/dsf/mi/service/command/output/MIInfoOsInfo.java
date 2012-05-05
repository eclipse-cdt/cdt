/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 4.2
 */
public class MIInfoOsInfo extends MIInfo {
	
	public MIInfoOsInfo(MIOutput record, boolean resourcesInformation) {
		super(record);
		if (resourcesInformation)
			parseResourcesInformation();
		else 
			parseResourceClasses();
	}
	
	public IGDBHardwareAndOS.IResourcesInformation getResourcesInformation()
	{
		return new IGDBHardwareAndOS.IResourcesInformation() {
			
			@Override
			public String[][] getContent() { return content; }
			
			@Override
			public String[] getColumnNames() { return columnNames; }
		};
	}
	
	public IGDBHardwareAndOS.IResourceClass[] getResourceClasses()
	{
		return resourceClasses;
	}
	
	private void parseResourceClasses()
	{
		List<IGDBHardwareAndOS.IResourceClass> classes = new ArrayList<IGDBHardwareAndOS.IResourceClass>(); 
		
		MITuple table = (MITuple)get(getMIOutput(), "OSDataTable");  //$NON-NLS-1$
		
		MIList body = (MIList)get(table, "body");  //$NON-NLS-1$
		for (MIResult r: body.getMIResults()) {
			MITuple row = (MITuple)r.getMIValue();
			
			final String id = getString(row.getMIResults()[0]);
			final String description = getString(row.getMIResults()[2]);
			
			classes.add(new IGDBHardwareAndOS.IResourceClass() {
				
				@Override
				public String getId() { return id; }
				
				@Override
				public String getHunamDescription() { return description; }
				
			});
		}
		
		resourceClasses = classes.toArray(new IGDBHardwareAndOS.IResourceClass[0]);
	}
	
	private void parseResourcesInformation()
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
		

		MIList body = (MIList) get(table, "body");  //$NON-NLS-1$
		if (body == null)
		{
			content = new String[0][];
			return;
		}
		
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
				++j;
			}
			content[i++] = rowStrings;
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
	
	private String getString(MIValue value)
	{
		return ((MIConst)value).getString();
	}
	
	private String getString(MIResult result)
	{
		return getString(result.getMIValue());
	}
	
	// The fields below are used for response with list of classes.
	private IGDBHardwareAndOS.IResourceClass[] resourceClasses;
	
	// The below fields are used only for data with specific resource class
	private String[] columnNames;
	private String[][] content;
	
}
