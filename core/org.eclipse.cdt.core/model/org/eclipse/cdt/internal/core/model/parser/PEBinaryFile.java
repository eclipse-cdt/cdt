package org.eclipse.cdt.internal.core.model.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PEArchive;
import org.eclipse.cdt.utils.coff.PE.Attribute;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class PEBinaryFile extends PlatformObject implements IBinaryFile, 
	IBinaryObject, IBinaryExecutable, IBinaryShared {

	IFile file;
	long timestamp;
	PE.Attribute attribute;
	String objectName;
	ArrayList symbols;

	public PEBinaryFile(IFile file) throws IOException {
		this(file, null);
	}
	
	public PEBinaryFile(IFile file, String o) throws IOException {
		this.file = file;
		objectName = o;
		loadInformation();
		hasChanged();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		if (file != null && objectName != null) {
			IPath location = file.getLocation();
			if (location != null) {
				PEArchive ar = null;
				try {   
					ar = new PEArchive(file.getLocation().toOSString());
					PEArchive.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						PEArchive.ARHeader hdr = headers[i];
						if (objectName.equals(hdr.getObjectName())) {
							stream = new ByteArrayInputStream(hdr.getObjectData());
							break;
						}
					}
				} catch (IOException e) {
				}
				if (ar != null) {
					ar.dispose();
				}
			}
		} else if (file != null && file.exists()) {
			try {
				stream = file.getContents();
			} catch (CoreException e) {
			}
		}
		if (stream == null) {
			stream = new ByteArrayInputStream(new byte[0]);
		}
		return stream;
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
		int type = 0;
		Attribute attr = getAttribute();
		if (attr != null) {
			switch (attribute.getType()) {
				case Attribute.PE_TYPE_EXE:
					type = IBinaryFile.EXECUTABLE;
				break;

				case Attribute.PE_TYPE_SHLIB:
					type = IBinaryFile.SHARED;
				break;

				case Attribute.PE_TYPE_OBJ:
					type = IBinaryFile.OBJECT;
				break;

				case Attribute.PE_TYPE_CORE:
					type = IBinaryFile.CORE;
				break;
			}
		}
		return type;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getBSS()
	 */
	public long getBSS() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getCPU()
	 */
	public String getCPU() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.getCPU();
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getData()
	 */
	public long getData() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (objectName != null) {
			return objectName;
		} else if (file != null) {
			return file.getName();
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
		if (hasChanged() || symbols == null) {
			if (symbols == null) {
				symbols = new ArrayList(5);
			}
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return (ISymbol[])symbols.toArray(new ISymbol[0]);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getText()
	 */
	public long getText() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#hasDebug()
	 */
	public boolean hasDebug() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attr.hasDebug();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attr.isLittleEndian();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		return "";
	}

	protected PE getPE() throws IOException {
		if (file != null && objectName != null) {
			IPath location = file.getLocation();
			if (location != null) { 
				PE pe = null;
				PEArchive ar = null;
				try {
					ar = new PEArchive(file.getLocation().toOSString());
					PEArchive.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						PEArchive.ARHeader hdr = headers[i];
						if (objectName.equals(hdr.getObjectName())) {
							pe = hdr.getPE();
							break;
						}
					}
				} finally {
					if (ar != null) {
						ar.dispose();
					}
				}
				if (pe != null) {
					return pe; 
				}
			}
		} else if (file != null && file.exists()) {
			IPath path = file.getLocation();
			if (path == null) {
				path = new Path("");
			}
			return new PE(path.toOSString());
		}
		throw new IOException("No file assiocated with Binary");
	}

	protected PE.Attribute getAttribute() {
		if (hasChanged()) {
			try {
				loadInformation(); 
			} catch (IOException e) {
			}
		}
		return attribute;
	}

	protected void loadInformation() throws IOException {
		PE pe = getPE();
		loadInformation(pe);
		pe.dispose();
	}

	private void loadInformation(PE pe) throws IOException {
		loadAttribute(pe);
		if (symbols != null) {
			symbols.clear();
			loadSymbols(pe);
			symbols.trimToSize();
		}
	}

	private void loadAttribute(PE pe) throws IOException {
		attribute = pe.getAttribute();
	}

	private void loadSymbols(PE pe) throws IOException {
		Coff.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() ||peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 ||
				    !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				Symbol sym = new Symbol();
				sym.filename = null;
				sym.name = name;
				sym.lineno = 0;
				sym.type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				symbols.add(sym);
			}
		}
	}

	boolean hasChanged() {
		long modification = file.getModificationStamp();
		boolean changed = modification != timestamp;
		timestamp = modification;
		return changed;
	}

}
