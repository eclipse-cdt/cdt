package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.coff.PEArchive;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class PEBinaryArchive extends PlatformObject implements IBinaryArchive {

	IPath path;
	ArrayList children;
	long timestamp;
	
	public PEBinaryArchive(IPath p) {
		path = p;
		children = new ArrayList(5);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive#getObjects()
	 */
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			if (path != null) {
				PEArchive ar = null;
				try {
					ar = new PEArchive(path.toOSString());
					PEArchive.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						IBinaryObject bin = new PEBinaryFile(path, headers[i].getObjectName());
						children.add(bin);
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
				if (ar != null) {
					ar.dispose();
				}
			}
			children.trimToSize();
		}
		return (IBinaryObject[])children.toArray(new IBinaryObject[0]);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getFile()
	 */
	public IPath getPath() {
		return path;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return IBinaryFile.ARCHIVE;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		try {
			return new FileInputStream(path.toFile());
		} catch (IOException e) {
		}
		return new ByteArrayInputStream(new byte[0]);
	}

	boolean hasChanged() {
		File file = path.toFile();
		if (file != null && file.exists()) {
			long modification = file.lastModified();
			boolean changed = modification != timestamp;
			timestamp = modification;
			return changed;
		}
		return false;
	}
	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive#add(IBinaryObject[])
	 */
	public void add(IBinaryObject[] objs) throws IOException {
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive#delete(IBinaryObject[])
	 */
	public void delete(IBinaryObject[] objs) throws IOException {
	}

}
