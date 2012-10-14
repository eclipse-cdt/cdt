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

import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * Example output:
 *
 *   (gdb) -info-os
 *   ^done,OSDataTable={nr_rows="9",nr_cols="3",
 *   hdr=[{width="10",alignment="-1",col_name="col0",colhdr="Type"},
 *        {width="10",alignment="-1",col_name="col1",colhdr="Description"},
 *        {width="10",alignment="-1",col_name="col2",colhdr="Title"}],
 *   body=[item={col0="processes",col1="Listing of all processes",
 *               col2="Processes"},
 *         item={col0="procgroups",col1="Listing of all process groups",
 *               col2="Process groups"},
 *         item={col0="threads",col1="Listing of all threads",
 *               col2="Threads"},
 *         item={col0="files",col1="Listing of all file descriptors",
 *               col2="File descriptors"},
 *         item={col0="sockets",col1="Listing of all internet-domain sockets",
 *               col2="Sockets"},
 *         item={col0="shm",col1="Listing of all shared-memory regions",
 *               col2="Shared-memory regions"},
 *         item={col0="semaphores",col1="Listing of all semaphores",
 *               col2="Semaphores"},
 *         item={col0="msg",col1="Listing of all message queues",
 *               col2="Message queues"},
 *         item={col0="modules",col1="Listing of all loaded kernel modules",
 *               col2="Kernel modules"}]}
 *   (gdb) -info-os processes
 *   ^done,OSDataTable={nr_rows="190",nr_cols="4",
 *   hdr=[{width="10",alignment="-1",col_name="col0",colhdr="pid"},
 *        {width="10",alignment="-1",col_name="col1",colhdr="user"},
 *        {width="10",alignment="-1",col_name="col2",colhdr="command"},
 *        {width="10",alignment="-1",col_name="col3",colhdr="cores"}],
 *   body=[item={col0="1",col1="root",col2="/sbin/init",col3="0"},
 *         item={col0="2",col1="root",col2="[kthreadd]",col3="1"},
 *         item={col0="3",col1="root",col2="[ksoftirqd/0]",col3="0"},
 *         ...
 *         item={col0="26446",col1="stan",col2="bash",col3="0"},
 *         item={col0="28152",col1="stan",col2="bash",col3="1"}]}
 *   (gdb)
 *
 * @since 4.2
 */
public class MIInfoOsInfo extends MIInfo {

	// The fields below are used for response with list of classes.
	private IGDBHardwareAndOS2.IResourceClass[] resourceClasses;

	// The below fields are used only for data with specific resource class
	private String[] columnNames;
	private String[][] content;

	public MIInfoOsInfo(MIOutput record, boolean resourcesInformation) {
		super(record);
		if (isDone()) {
			if (resourcesInformation)
				parseResourcesInformation();
			else
				parseResourceClasses();
		}
	}

	public IGDBHardwareAndOS2.IResourcesInformation getResourcesInformation()
	{
		return new IGDBHardwareAndOS2.IResourcesInformation() {

			@Override
			public String[][] getContent() { return content; }

			@Override
			public String[] getColumnNames() { return columnNames; }
		};
	}

	public IGDBHardwareAndOS2.IResourceClass[] getResourceClasses()
	{
		return resourceClasses;
	}

	private void parseResourceClasses()
	{
		List<IGDBHardwareAndOS2.IResourceClass> classes = new ArrayList<IGDBHardwareAndOS2.IResourceClass>();

		MITuple table = (MITuple)get(getMIOutput(), "OSDataTable");  //$NON-NLS-1$

		MIList body = (MIList)get(table, "body");  //$NON-NLS-1$
		for (MIResult r: body.getMIResults()) {
			MITuple row = (MITuple)r.getMIValue();

			final String id = getString(row.getMIResults()[0]);
			final String description = getString(row.getMIResults()[2]);

			classes.add(new IGDBHardwareAndOS2.IResourceClass() {

				@Override
				public String getId() { return id; }

				@Override
				public String getHumanDescription() { return description; }

			});
		}

		resourceClasses = classes.toArray(new IGDBHardwareAndOS2.IResourceClass[classes.size()]);
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
}
