package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

public class CTagsCmd {
		
	public CTagsCmd() {
	}

	private void process(String filename, List list, IFile file) throws IOException {

		BufferedReader stdout;

		String[] args = {"ctags", "--excmd=pattern", "--format=2",
							"--if0=yes", "--sort=no", "--extra=q",
							"--fields=aiKlmnsz", "--line-directives=yes",
							"--sort=no", "--kind-long", "--c-types=cdefgmnpstuvx",
							"-f", "-", filename};

		//Process ctags = Runtime.getRuntime().exec(args);
		Process ctags = ProcessFactory.getFactory().exec(args);
		//stdin = new BufferedWriter(new OutputStreamWriter(ctags.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(ctags.getInputStream()));

		//System.out.println("Processing");
		String line;
		try {
			while ((line = stdout.readLine()) != null) {
				//System.out.println("ProcessingLine " + line);
				CTagsEntry entry = new CTagsEntry(line, file);
				list.add(entry);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		// Force an explicit close even if
		// we call destroy()
		try {
			stdout.close();
			ctags.getOutputStream().close();
			ctags.getErrorStream().close();
		} catch (IOException e) {
		}
		ctags.destroy();
	}

	public ITagEntry[] getTagEntries(IFile file, String filename) throws IOException {
		List list = new LinkedList();
		process(filename, list, file);
		return (ITagEntry[])list.toArray(new ITagEntry[0]);
	}

	public static void main(String[] args) {
		try {
			CTagsCmd cmd = new CTagsCmd();
			ITagEntry[] entries = cmd.getTagEntries(null, args[0]);		
			for (int i = 0; i < entries.length; i++) {
				if (entries[i] instanceof CTagsEntry) {
					CTagsEntry entry = (CTagsEntry)entries[i];
					System.out.println(entry.getLine() + "\n");
					entry.print();
					System.out.println("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
