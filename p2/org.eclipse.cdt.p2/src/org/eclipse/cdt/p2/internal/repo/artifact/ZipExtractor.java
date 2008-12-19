package org.eclipse.cdt.p2.internal.repo.artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor extends Thread {

	private final InputStream in;
	private final File installDir;
	private final FileListWriter fileListWriter;
	
	public ZipExtractor(InputStream in, File installDir, FileListWriter fileListWriter) {
		this.in = in;
		this.installDir = installDir;
		this.fileListWriter = fileListWriter;
	}

	@Override
	public void run() {
		try {
			ZipInputStream zipIn = new ZipInputStream(in);
			for (ZipEntry zipEntry = zipIn.getNextEntry(); zipEntry != null; zipEntry = zipIn.getNextEntry()) {
				File outFile = new File(installDir, zipEntry.getName());
				if (zipEntry.isDirectory()) {
					outFile.mkdirs();
				} else {
					if (outFile.exists())
						outFile.delete();
					else
						outFile.getParentFile().mkdirs();
					FileOutputStream outStream = new FileOutputStream(outFile);
					copyStream(zipIn, false, outStream, true);
					long lastModified = zipEntry.getTime();
					outFile.setLastModified(lastModified);
					fileListWriter.addFile(new InstalledFile(outFile, lastModified));
				}
				zipIn.closeEntry();
			}
			// Keep reading until there's none left
			while (in.read() != -1);
			zipIn.close();
			fileListWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static int copyStream(InputStream in, boolean closeIn, OutputStream out, boolean closeOut) throws IOException {
		try {
			int written = 0;
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
				written += len;
			}
			return written;
		} finally {
			try {
				if (closeIn) {
					in.close();
				}
			} finally {
				if (closeOut) {
					out.close();
				}
			}
		}
	}

}
