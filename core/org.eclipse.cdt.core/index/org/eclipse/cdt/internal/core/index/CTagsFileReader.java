package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.index.ITagEntry;

public class CTagsFileReader {
	String filename = null;
	List list = null;

	public CTagsFileReader(String name) {
		filename = name;
	}

	private void parse(BufferedReader reader) throws IOException {
		CTagsHeader header = new CTagsHeader();
		// Skip the header.
		header.parse(reader);
		String s;
		while ((s = reader.readLine()) != null) {
			ITagEntry entry = new CTagsEntry(s, null); 
			list.add(entry);
			// System.out.println (entry.getLine() + "\n\n");
			// entry.print(); System.out.println();
		}
	}

	public ITagEntry[] getTagEntries() throws IOException {
		if (list == null) {
			list = new LinkedList();
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			parse(reader);
		}
		return (ITagEntry[])list.toArray(new ITagEntry[0]);
	}

	public static void main(String[] args) {
		try {
			CTagsFileReader tagfile = new CTagsFileReader(args[0]);
			ITagEntry[] entries = tagfile.getTagEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i] instanceof CTagsEntry) {
					CTagsEntry entry = (CTagsEntry)entries[i];
						System.out.println(entry.getLine() + "\n\n");
						entry.print();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
