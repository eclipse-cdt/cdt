package org.eclipse.cdt.utils.elf.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class BinaryArchive extends PlatformObject implements IBinaryArchive {

	IPath path;
	ArrayList children;
	long timestamp;

	public BinaryArchive(IPath p) throws IOException {
		path = p;
		new AR(path.toOSString()).dispose(); // check file type
		children = new ArrayList(5);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive#getObjects()
	 */
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			if (path != null) {
				AR ar = null;
				try {
					ar = new AR(path.toOSString());
					AR.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						IBinaryObject bin = new ARMember(path, headers[i]);
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
		return (IBinaryObject[]) children.toArray(new IBinaryObject[0]);
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
		long modif = path.toFile().lastModified();
		boolean changed = modif != timestamp;
		timestamp = modif;
		return changed;
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
