package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.cdt.core.index.ITagEntry;

public class CTagsFileWriter {
	BufferedWriter writer = null;
	ITagEntry [] entries = null;

	public CTagsFileWriter(String tagfile) throws IOException {
		writer = new BufferedWriter(new FileWriter(tagfile));
	}

	public CTagsFileWriter(Writer outfile) {
		writer = new BufferedWriter(outfile);
	}

	public void save(ITagEntry[] ent) throws IOException {
		setTagEntries(ent);
		save();
	}

	public void save() throws IOException {
		if (entries != null) {
			String header = CTagsHeader.header();
			writer.write(header, 0, header.length());	
			for (int i = 0; i < entries.length; i++) {
				String entry = entries[i].getLine();
				writer.write(entry, 0, entry.length());
				writer.newLine();
			}
			writer.flush();
			entries = null;
		}
	}

	public void setTagEntries(ITagEntry[] ent) {
		entries = ent;
	}

	public static void main(String[] args) {
		try {
			CTagsFileReader inFile = new CTagsFileReader(args[0]);
			ITagEntry[] entries = inFile.getTagEntries();
			CTagsFileWriter outFile = new CTagsFileWriter(args[0] + ".back");
			outFile.save(entries);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
