package org.eclipse.cdt.p2.internal.repo.artifact;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileListWriter {

	private final BufferedWriter out;
	
	public FileListWriter(File fileListFile) throws IOException {
		out = new BufferedWriter(new FileWriter(fileListFile));
	}
	
	public void addFile(InstalledFile file) throws IOException {
		file.write(out);
	}
	
	public void close() throws IOException {
		out.close();
	}

}
