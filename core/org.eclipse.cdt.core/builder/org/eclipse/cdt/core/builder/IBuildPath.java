package org.eclipse.cdt.core.builder;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

/**
 * A BuildPath can contain the following:<ul>
 * <li> EXTRA_INCVPATH: a list of path to look for include files
 * <li> EXTRA_LIBVPATH: a list of path to search for libraries
 * <li> EXTRA_SRCVPATH: a list of path to search for files
 * <li> LIBS: a list of libraries to link

 * <li> CPPFLAGS: C Preprocessor options
 * <li> CPP: C Preprocessor cmd

 * <li> CFLAGS: C options
 * <li> CC: C cmd

 * <li> CXXFLAGS: C++ Preprocessor options
 * <li> CXX: C++ cmd

 * <li> ARFLAGS: archive options
 * <li> AR: archiver cmd

 * <li> LDFLAGS: linker options
 * <li> LD: linker cmd

 * <li> ASFLAGS: assembly options
 * <li> AS: assembly cmd

 * </ul>
 */

public interface IBuildPath {

	public String[] getCPPOpts();
	public String[] getCPPFlags();
	public void setCPP(IPath p);
	public void setCPPOpts(String[] s);
	public void setCPPFlags(String[] s);

	public IPath getCXX();
	public String[] getCXXOPT();
	public String[] getCXXFlags();
	public void setCXX(IPath p);
	public void setCXXOpts(String[] s);
	public void setCXXFlags(String[] s);

	public IPath getCC();
	public String[] getCFLAGS();
	public String[] getCOpts();
	public void setCFLAGS(String[] s);
	public void setCOpts(String[] s);
	public void setCC(IPath p);

	public IPath getAS();
	public String[] getASFlags();
	public String[] getASOpts();
	public void getAS(IPath p);
	public void getASOpts(String[] s);
	public void getASFlags(String[] s);

	public IPath getLD();
	public String[] getLDOpts();
	public String[] getLDFlags();
	public void setLD(IPath p);
	public void setLDOpts(String[] s);
	public void setLDFlags(String[] s);

	public IPath getAR();
	public String[] getARFlags();
	public String[] getAROpts();
	public void setAR(IPath p);
	public void setAROpts(String[] s);
	public void setARFlags(String[] s);

	public IPath[] getIncVPath();
	public void setIncVPath(IPath[] p);

	public IPath[] getLibs();
	public void setLibs(IPath[] p);

	public IPath[] getLibVPath();
	public void setLibVPath(IPath[] p);

	public IPath[] getSRCVPath();
	public void setSRCVPath(IPath[] p);
}
