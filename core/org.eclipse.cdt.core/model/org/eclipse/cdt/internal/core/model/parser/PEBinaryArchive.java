package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.coff.PEArchive;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class PEBinaryArchive extends PlatformObject implements IBinaryArchive {

	IFile file;
	ArrayList children;
	long timestamp;
	
	public PEBinaryArchive(IFile f) {
		file = f;
		children = new ArrayList(5);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive#getObjects()
	 */
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			IPath location = file.getLocation();
			if (location != null) {
				PEArchive ar = null;
				try {
					ar = new PEArchive(location.toOSString());
					PEArchive.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						IBinaryObject bin = new PEBinaryFile(file, headers[i].getObjectName());
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
	public IFile getFile() {
		return file;
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
			return file.getContents();
		} catch (CoreException e) {
		}
		return new ByteArrayInputStream(new byte[0]);
	}

	boolean hasChanged() {
		long modif = file.getModificationStamp();
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
