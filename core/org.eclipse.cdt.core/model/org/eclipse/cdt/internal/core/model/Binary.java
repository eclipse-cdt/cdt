/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

 
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.ISourceFinder;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.BinaryFilePresentation;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

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

	private IBinaryObject binaryObject;
	private boolean showInBinaryContainer;

	public Binary(ICElement parent, IFile file, IBinaryObject bin) {
		super(parent, file, ICElement.C_BINARY);
		binaryObject = bin;
		showInBinaryContainer= determineShowInBinaryContainer(bin);
	}

	private boolean determineShowInBinaryContainer(IBinaryObject bin) {
		BinaryFilePresentation presentation= (BinaryFilePresentation) bin.getAdapter(BinaryFilePresentation.class);
		if (presentation != null) {
			return presentation.showInBinaryContainer();
		}
		return BinaryFilePresentation.showInBinaryContainer(bin);
	}

	public Binary(ICElement parent, IPath path, IBinaryObject bin) {
		super (parent, path, ICElement.C_BINARY);
		binaryObject = bin;
		showInBinaryContainer= determineShowInBinaryContainer(bin);
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
	@SuppressWarnings("rawtypes")
	@Override
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

	@Override
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
	@Override
	public boolean isReadOnly() {
		return true;
	}

	 @Override
	public CElementInfo createElementInfo() {
		return new BinaryInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements, IResource underlyingResource)
		throws CModelException {
		return computeChildren(info, underlyingResource);
	}

	boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
		boolean ok = false;
		if (isObject() || isExecutable() || isSharedLib()) {
			Map<IPath, BinaryModule> hash = new HashMap<IPath, BinaryModule>();
			IBinaryObject obj = getBinaryObject();
			if (obj != null) {
				// First check if we can get the list of source
				// files used to build the binary from the symbol
				// information.  if not, fall back on information from the binary parser.
				boolean showSourceFiles = CCorePlugin.getDefault().getPluginPreferences().getBoolean( CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES );
				if (!showSourceFiles ||
						!addSourceFiles(info, obj, hash))
				{
					ISymbol[] symbols = obj.getSymbols();
					for (ISymbol symbol : symbols) {
						switch (symbol.getType()) {
							case ISymbol.FUNCTION :
								addFunction(info, symbol, hash);
							break;

							case ISymbol.VARIABLE :
								addVariable(info, symbol, hash);
							break;
						}
					}
				}				
				ok = true;
			}
		}
		return ok;
	}

	private boolean addSourceFiles(OpenableInfo info, IBinaryObject obj,
			Map<IPath, BinaryModule> hash) throws CModelException {
		// Try to get the list of source files used to build the binary from the
		// symbol information.

		ISymbolReader symbolreader = (ISymbolReader)obj.getAdapter(ISymbolReader.class);
		if (symbolreader == null)
			return false;

		String[] sourceFiles = symbolreader.getSourceFiles();
		if (sourceFiles != null && sourceFiles.length > 0) {
			ISourceFinder srcFinder = (ISourceFinder) getAdapter(ISourceFinder.class);
			try {
				for (String filename : sourceFiles) {
					
					// Find the file locally
					if (srcFinder != null) {
						String localPath = srcFinder.toLocalPath(filename);
						if (localPath != null) {
							filename  = localPath; 
						}
					}
	
					// Be careful how you use this File object. If filename is a relative path, the resulting File
					// object will apply the relative path to the working directory, which is not what we want.
					// Stay away from methods that return or use the absolute path of the object. Note that
					// File.isAbsolute() returns false when the object was constructed with a relative path.
					File file = new File(filename);
	
					// Create a translation unit for this file and add it as a child of the binary
					String id = CoreModel.getRegistedContentTypeId(getCProject().getProject(), file.getName());
					if (id == null) {
						// Don't add files we can't get an ID for.
						continue;
					}
					
					// See if this source file is already in the project.
					// We check this to determine if we should create a TranslationUnit or ExternalTranslationUnit
					IFile wkspFile = null;
					if (file.isAbsolute()) {
						IFile[] filesInWP = ResourceLookup.findFilesForLocation(new Path(filename));
		
						for (IFile element : filesInWP) {
							if (element.isAccessible()) {
								wkspFile = element;
								break;
							}
						}
					}
					
					TranslationUnit tu;
					if (wkspFile != null)
						tu = new TranslationUnit(this, wkspFile, id);
					else {
						// If we have an absolute path (for the host file system), then use an IPath to create the
						// ExternalTranslationUnit, as that is the more accurate way to specify the file. If it's
						// not, then use the path specification we got from the debug information. We want to
						// avoid, e.g., converting a UNIX path to a Windows one when debugging a UNIX-built binary
						// on Windows. The opportunity to remap source paths was taken above, when we called
						// ISourceFinder. If a mapping didn't occur, we want to preserve whatever the debug
						// information told us. See bugzilla 297781
						if (file.isAbsolute()) {
							tu = new ExternalTranslationUnit(this, Path.fromOSString(filename), id);
						}
						else {
							tu = new ExternalTranslationUnit(this, URIUtil.toURI(filename), id);						
						}
					}
	
					if (! info.includesChild(tu))
						info.addChild(tu);					
				}
				return true;
			}
			finally {
				if (srcFinder != null) {
					srcFinder.dispose();
				}
			}
			
		}
		return false;
	}
	
	private void addFunction(OpenableInfo info, ISymbol symbol, Map<IPath, BinaryModule> hash) throws CModelException {
		IPath filename= symbol.getFilename();
		BinaryFunction function = null;

		if (filename != null && !filename.isEmpty()) {
			BinaryModule module = null;
			if (hash.containsKey(filename)) {
				module = hash.get(filename);
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

	private void addVariable(OpenableInfo info, ISymbol symbol, Map<IPath, BinaryModule> hash) throws CModelException {
		IPath filename= symbol.getFilename();
		BinaryVariable variable = null;
		if (filename != null && !filename.isEmpty()) {
			BinaryModule module = null;
			if (hash.containsKey(filename)) {
				module = hash.get(filename);
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

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#getBuffer()
	 * 
	 * overridden from default as we do not need to create our children to provider a buffer since the buffer just contains
	 * IBinaryOject contents which is not model specific.
	 */
	@Override
	public IBuffer getBuffer() throws CModelException {
		if (hasBuffer()) {
			IBuffer buffer = getBufferManager().getBuffer(this);
			if (buffer == null) {
				// try to (re)open a buffer
				buffer = openBuffer(null);
			}
			return buffer;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#openBuffer(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
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
		buffer.addBufferChangedListener(this);
		
		return buffer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#hasBuffer()
	 */
	@Override
	protected boolean hasBuffer() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	@Override
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#closing(java.lang.Object)
	 */
	@Override
	protected void closing(Object info) throws CModelException {
		ICProject cproject = getCProject();
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(cproject);
		if (pinfo != null && pinfo.vBin != null) {
			pinfo.vBin.removeChild(this);
		}
		super.closing(info);
	}

	public boolean showInBinaryContainer() {
		return showInBinaryContainer;
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		return null;
	}

	@Override
	public String getHandleMemento() {
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
		return 0;
	}

}
