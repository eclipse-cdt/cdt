/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class Binary extends Openable implements IBinary {

	private int fBinType;
	private String hasDebug;
	private String cpu;
	private String[] needed;
	private long longData;
	private long longText;
	private long longBSS;
	private String endian;
	private String soname;
	
	private long fLastModification;

	IBinaryObject binaryObject;

	public Binary(ICElement parent, IFile file, IBinaryObject bin) {
		super(parent, file, ICElement.C_BINARY);
		binaryObject = bin;
	}

	public Binary(ICElement parent, IPath path, IBinaryObject bin) {
		super (parent, path, ICElement.C_BINARY);
		binaryObject = bin;
	}

	public boolean isSharedLib() {
		return getType() == IBinaryFile.SHARED;
	}

	public boolean isExecutable() {
		return getType() == IBinaryFile.EXECUTABLE;
	}

	public boolean isObject() {
		return getType() == IBinaryFile.OBJECT;
	}

	public boolean isCore() {
		return getType() == IBinaryFile.CORE;
	}

	public boolean hasDebug() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (hasDebug == null || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj != null) {
					hasDebug = new Boolean(obj.hasDebug()).toString();
				}
			}
		}
		return Boolean.valueOf(hasDebug).booleanValue();
	}

	public String getCPU() {
		if (isObject() || isExecutable() || isSharedLib() || isCore()) {
			if (cpu == null || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				cpu = obj.getCPU();
			}
		}
		return (cpu == null ? "" : cpu); //$NON-NLS-1$
	}

	public String[] getNeededSharedLibs() {
		if (isExecutable() || isSharedLib()) {
			if (needed == null || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj instanceof IBinaryExecutable) {
					needed = ((IBinaryExecutable)obj).getNeededSharedLibs();
				}
			}
		}
		return (needed == null ? new String[0] : needed);
	}

	public long getText() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (longText == -1 || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj != null) {
					longText = obj.getText();
				}
			}
		}
		return longText;
	}

	public long getData() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (longData == -1 || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj != null) {
					longData = obj.getData();
				}
			}
		}
		return longData;
	}

	public long getBSS() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (longBSS == -1 || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj != null) {
					longBSS = obj.getBSS();
				}
			}
		}
		return longBSS;
	}

	public String getSoname() {
		if (isSharedLib()) {
			if (soname == null || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj instanceof IBinaryShared) {
					soname = ((IBinaryShared)obj).getSoName();
				}
			}
		}
		return (soname == null ? "" : soname); //$NON-NLS-1$
	}

	public boolean isLittleEndian() {
		if (isObject() || isExecutable() || isSharedLib() || isCore()) {
			if (endian == null || hasChanged()) {
				IBinaryObject obj = getBinaryObject();
				if (obj != null) {
					endian = new Boolean(obj.isLittleEndian()).toString();
				}
			}
		}
		return Boolean.valueOf(endian).booleanValue();
	}

	protected IBinaryObject getBinaryObject() {
		return binaryObject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IBinaryObject.class.equals(adapter)) {
			return getBinaryObject();
		}
		return super.getAdapter(adapter);
	}

	protected int getType() {
		IBinaryObject obj = getBinaryObject();
		if (obj != null && (fBinType == 0 || hasChanged())) {
			fBinType = obj.getType();
		}
		return fBinType;
	}

	protected boolean hasChanged() {
		long modification = getModificationStamp();
		boolean changed = modification != fLastModification;
		fLastModification = modification;
		if (changed) {
			hasDebug = null;
			needed = null;
			cpu = null;
			endian = null;
			longBSS = -1;
			longData = -1;
			longText = -1;
			soname = null;
		}
		return changed;
	}

	protected long getModificationStamp() {
		IResource res = getResource();
		if (res != null) {
			return res.getModificationStamp();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	 public CElementInfo createElementInfo() {
		return new BinaryInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		return computeChildren(info, underlyingResource);
	}

	boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
		boolean ok = false;
		if (isObject() || isExecutable() || isSharedLib()) {
			Map hash = new HashMap();
			IBinaryObject obj = getBinaryObject();
			if (obj != null) {
				ISymbol[] symbols = obj.getSymbols();
				for (int i = 0; i < symbols.length; i++) {
					switch (symbols[i].getType()) {
						case ISymbol.FUNCTION :
							addFunction(info, symbols[i], hash);
						break;

						case ISymbol.VARIABLE :
							addVariable(info, symbols[i], hash);
						break;
					}
				}
				ok = true;
			}
		}
		return ok;
	}

	private void addFunction(OpenableInfo info, ISymbol symbol, Map hash) throws CModelException {
		IPath filename = filename = symbol.getFilename();
		BinaryFunction function = null;

		if (filename != null && !filename.isEmpty()) {
			BinaryModule module = null;
			if (hash.containsKey(filename)) {
				module = (BinaryModule)hash.get(filename);
			} else {
				// A special container we do not want the file to be parse.
				module = new BinaryModule(this, filename);
				hash.put(filename, module);
				info.addChild(module);
			}
			function = new BinaryFunction(module, symbol.getName(), symbol.getAddress());
			function.setLines(symbol.getStartLine(), symbol.getEndLine());
			module.addChild(function);
		} else {
			//function = new Function(parent, symbol.getName());
			function = new BinaryFunction(this, symbol.getName(), symbol.getAddress());
			function.setLines(symbol.getStartLine(), symbol.getEndLine());
			info.addChild(function);
		}
		//		if (function != null) {
		//			if (!external) {
		//				function.getFunctionInfo().setAccessControl(IConstants.AccStatic);
		//			}
		//		}
	}

	private void addVariable(OpenableInfo info, ISymbol symbol, Map hash) throws CModelException {
		IPath filename = filename = symbol.getFilename();
		BinaryVariable variable = null;
		if (filename != null && !filename.isEmpty()) {
			BinaryModule module = null;
			if (hash.containsKey(filename)) {
				module = (BinaryModule)hash.get(filename);
			} else {
				module = new BinaryModule(this, filename);
				hash.put(filename, module);
				info.addChild(module);
			}
			variable = new BinaryVariable(module, symbol.getName(), symbol.getAddress());
			variable.setLines(symbol.getStartLine(), symbol.getEndLine());
			module.addChild(variable);
		} else {
			variable = new BinaryVariable(this, symbol.getName(), symbol.getAddress());
			variable.setLines(symbol.getStartLine(), symbol.getEndLine());
			info.addChild(variable);
		}
		
		//if (variable != null) {
		//	if (!external) {
		//		variable.getVariableInfo().setAccessControl(IConstants.AccStatic);
		//	}
		//}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#openBuffer(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {

		// create buffer -  translation units only use default buffer factory
		BufferManager bufManager = getBufferManager();		
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null) 
			return null;
		
		// set the buffer source
		if (buffer.getCharacters() == null){
			IBinaryObject bin = getBinaryObject();
			if (bin != null) {
				StringBuffer sb = new StringBuffer();
				try {
					BufferedReader stream = new BufferedReader(new InputStreamReader(bin.getContents()));
					char[] buf = new char[512];
					int len;
					while ((len = stream.read(buf, 0, buf.length)) != -1) {
						sb.append(buf, 0, len);
					}
				} catch (IOException e) {
					// nothint.
				}
				buffer.setContents(sb.toString());
			} else {
				IResource file = this.getResource();
				if (file != null && file.getType() == IResource.FILE) {
					buffer.setContents(Util.getResourceContentsAsCharArray((IFile)file));
				}
			}
		}

		// add buffer to buffer cache
		bufManager.addBuffer(buffer);
		
		// listen to buffer changes
		// buffer.addBufferChangedListener(this);
		
		return buffer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#hasBuffer()
	 */
	protected boolean hasBuffer() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#closing(java.lang.Object)
	 */
	protected void closing(Object info) throws CModelException {
		ICProject cproject = getCProject();
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(cproject);
		if (pinfo != null && pinfo.vBin != null) {
			pinfo.vBin.removeChild(this);
		}
		super.closing(info);
	}
}
