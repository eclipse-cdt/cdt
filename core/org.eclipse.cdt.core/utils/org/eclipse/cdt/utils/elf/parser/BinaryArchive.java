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
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.core.runtime.IPath;

/**
 */
public class BinaryArchive extends BinaryFile implements IBinaryArchive {

	ArrayList children;
	long timestamp;

	public BinaryArchive(IPath p) throws IOException {
		super(p);
		new AR(p.toOSString()).dispose(); // check file type
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
					ar = new AR(getPath().toOSString());
					AR.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						IBinaryObject bin = new ARMember(getPath(), headers[i]);
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
			return new FileInputStream(getPath().toFile());
		} catch (IOException e) {
		}
		return new ByteArrayInputStream(new byte[0]);
	}

	boolean hasChanged() {
		long modif = getPath().toFile().lastModified();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.BinaryFile#getAttribute()
	 */
	protected Attribute getAttribute() {
		return null;
	}

}
