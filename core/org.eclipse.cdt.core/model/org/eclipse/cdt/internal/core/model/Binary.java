package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
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

	IBinaryFile binaryFile;

	public Binary(ICElement parent, IFile file, IBinaryFile bin) {
		super(parent, file, ICElement.C_BINARY);
		binaryFile = bin;
	}

	public Binary(ICElement parent, IPath path, IBinaryFile bin) {
		super (parent, path, ICElement.C_BINARY);
		binaryFile = bin;
	}

	public boolean isSharedLib() {
		return getType() == IBinaryObject.SHARED;
	}

	public boolean isExecutable() {
		return getType() == IBinaryObject.EXECUTABLE;
	}

	public boolean isObject() {
		return getType() == IBinaryObject.OBJECT;
	}

	public boolean isCore() {
		return getType() == IBinaryObject.CORE;
	}

	public boolean hasDebug() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (hasDebug == null || hasChanged()) {
				hasDebug = Boolean.toString(((IBinaryObject)getBinaryFile()).hasDebug());
			}
		}
		return Boolean.valueOf(hasDebug).booleanValue();
	}

	public String getCPU() {
		if (isObject() || isExecutable() || isSharedLib() || isCore()) {
			if (cpu == null || hasChanged()) {
				cpu = ((IBinaryObject)getBinaryFile()).getCPU();
			}
		}
		return (cpu == null ? "" : cpu);
	}

	public String[] getNeededSharedLibs() {
		if (isExecutable() || isSharedLib()) {
			if (needed == null || hasChanged()) {
				needed = ((IBinaryExecutable)getBinaryFile()).getNeededSharedLibs();
			}
		}
		return (needed == null ? new String[0] : needed);
	}

	public long getText() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (longText == -1 || hasChanged()) {
				longText = ((IBinaryObject)getBinaryFile()).getText();
			}
		}
		return longText;
	}

	public long getData() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (longData == -1 || hasChanged()) {
				longData = ((IBinaryObject)getBinaryFile()).getData();
			}
		}
		return longData;
	}

	public long getBSS() {
		if (isObject() || isExecutable() || isSharedLib()) {
			if (longBSS == -1 || hasChanged()) {
				longBSS = ((IBinaryObject)getBinaryFile()).getBSS();
			}
		}
		return longBSS;
	}

	public String getSoname() {
		if (isSharedLib()) {
			if (soname == null || hasChanged()) {
				soname = ((IBinaryShared)getBinaryFile()).getSoName();
			}
		}
		return (soname == null ? "" : soname);
	}

	public boolean isLittleEndian() {
		if (isObject() || isExecutable() || isSharedLib() || isCore()) {
			if (endian == null || hasChanged()) {
				endian = Boolean.toString(((IBinaryObject)getBinaryFile()).isLittleEndian());
			}
		}
		return Boolean.valueOf(endian).booleanValue();
	}

	protected IBinaryFile getBinaryFile() {
		return binaryFile;
	}
	
	protected int getType() {
		if (getBinaryFile() != null && (fBinType == 0 || hasChanged())) {
			fBinType = getBinaryFile().getType();
		}
		return fBinType;
	}

	protected boolean hasChanged() {
		long modification = getModificationStamp();
		boolean changed = modification != fLastModification;
		fLastModification = modification;
		hasDebug = null;
		needed = null;
		cpu = null;
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
	 * @see org.eclipse.cdt.internal.core.model.Openable#generateInfos(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		CModelManager.getDefault().putInfo(this, info);
		return computeChildren(info, underlyingResource);
	}


	boolean computeChildren(OpenableInfo info, IResource res) {
		if (isObject() || isExecutable() || isSharedLib()) {
			Map hash = new HashMap();
			ISymbol[] symbols = ((IBinaryObject)getBinaryFile()).getSymbols();
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
		} else {
			return false;
		}
		return true;
	}

	private void addFunction(OpenableInfo info, ISymbol symbol, Map hash) {
		IPath filename = filename = symbol.getFilename();
		BinaryFunction function = null;

		if (filename != null) {
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

	private void addVariable(OpenableInfo info, ISymbol symbol, Map hash) {
		IPath filename = filename = symbol.getFilename();
		BinaryVariable variable = null;
		if (filename != null) {
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

}
