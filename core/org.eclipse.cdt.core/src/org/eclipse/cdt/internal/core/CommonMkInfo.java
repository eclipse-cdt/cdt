package org.eclipse.cdt.internal.core;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

public class CommonMkInfo {

	public final static String COMMON_MK = "common.mk";

	IPath dir;
	long modification;
	IPath[] includePaths = new IPath[0];
	IPath[] libs = new IPath[0];
	IPath[] libPaths = new IPath[0];

	public CommonMkInfo(IPath dir) {
		this.dir = dir;
		modification = 0;
	}

	public CommonMkInfo () {
		dir = new Path("");
		String def = EnvironmentReader.getEnvVar("QNX_TARGET");
		if (def != null) {
			IPath defaultPath = new Path(def);
			includePaths = new IPath[] {defaultPath.append("/usr/include")};
			libPaths = new IPath[] {defaultPath.append("/usr/lib"),
									defaultPath.append("/x86/usr/lib")};
		}
		libs = new IPath[] {new Path("libc.so")}; 
	}
	
	public IPath[] getIncludePaths() {
		parse();
		return includePaths;
	}

	public IPath[] getLibs() {
		boolean hasLibC = false;
		parse();
		for (int i = 0; i < libs.length; i++) {
			String name = libs[i].toOSString();
			if (!(name.indexOf(IPath.SEPARATOR) != -1
					|| name.indexOf(IPath.DEVICE_SEPARATOR) != -1
					|| name.indexOf('.') != -1)) {
				if (!name.startsWith("lib")) {
					libs[i] = new Path("lib" + name + ".so");
				}
			}
			if (libs[i].toOSString().equals("libc.so"))
				hasLibC = true;
		}
		if (!hasLibC) {
			IPath[] newlibs = new IPath[libs.length + 1];
			int i = 0;;
			for (; i < libs.length; i++) {
				newlibs[i] = libs[i];
			}
			newlibs[i] = new Path("libc.so"); 
			libs = newlibs;
		}
		return libs;
	}

	public IPath[] getLibPaths() {
		parse();
		return libPaths;
	}

	public boolean hasChanged() {
		File prj = new File(dir.toOSString());
		File common = new File(prj, COMMON_MK);
		if (!prj.exists() || prj.isFile() || !common.exists())
			return false;
		long modif = common.lastModified();
		return (modif > modification);
	}

	void parse() {
		File makefile = null;
		try {
			if (hasChanged()) {
				File prj = new File(dir.toOSString());
				File common = new File(prj, COMMON_MK);
				modification = common.lastModified();
				makefile = File.createTempFile("QMakefile", null, prj);
				OutputStream fout = new FileOutputStream(makefile);
				DataOutputStream out = new DataOutputStream(fout);

				out.writeBytes("LIST=OS CPU VARIANT\n");
				out.writeBytes("include common.mk\n");
				out.writeBytes("\n");

				out.writeBytes("LIBS:\n");
				out.writeBytes("\t@echo $(LIBS)\n");
				out.writeBytes("\n");

				out.writeBytes("INCVPATH:\n");
				out.writeBytes("\t@echo $(INCVPATH)\n");
				out.writeBytes("\n");

				out.writeBytes("SRCVPATH:\n");
				out.writeBytes("\t@echo $(SRCVPATH)\n");
				out.writeBytes("\n");

				out.writeBytes("LIBVPATH:\n");
				out.writeBytes("\t@echo $(LIBVPATH)\n");
				out.writeBytes("\n");

				out.flush();
				out.close();

				// FIXME: Use the proper os and CPU
				Properties envp = EnvironmentReader.getEnvVars();
				envp.setProperty("OS", "nto");
				envp.setProperty("CPU", "x86");
				IPath[] incVPath = spawn("INCVPATH", envp, makefile, prj);
				parseIncVPath(incVPath);
				IPath[] libNames = spawn("LIBS", envp, makefile, prj);
				parseLibs(libNames);
				IPath[] libVPath = spawn("LIBVPATH", envp, makefile, prj);
				parseLibVPath(libVPath);
			}
		} catch (IllegalArgumentException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (makefile != null)
					makefile.delete();
			} catch (SecurityException e) {
			}
		}
	}

	IPath[] spawn (String target, Properties envp, File makefile, File dir) {
		// FIXME: Use the proper MakeCommand from the builder.
		String[] args = new String[] {"make", "-f", makefile.getName(), target}; 
		BufferedReader stdout = null;
		Process make = null;
		StringBuffer buffer = new StringBuffer();

		try {
			ArrayList envList = new ArrayList();

			// Turn the environment Property to an Array.
			Enumeration names = envp.propertyNames();
			if (names != null) {
				while (names.hasMoreElements()) {
					String key = (String) names.nextElement();
					envList.add(key + "=" + envp.getProperty(key));
				}
			}

			String[] env = (String[]) envList.toArray(new String[envList.size()]);
			make = ProcessFactory.getFactory().exec(args, env, dir);
			stdout = new BufferedReader(new InputStreamReader(make.getInputStream()));
			String s;
			while ((s = stdout.readLine ()) != null) {
				buffer.append(s);
			}
			stdout.close();
		} catch (SecurityException e) {
		} catch (IndexOutOfBoundsException e) {
		} catch (NullPointerException e) {
		} catch (IOException e) {
		} finally {
			if (make != null) {
				make.destroy();
			}
		}

		// FIXME: This not quite right some of the include may contains
		// things like double quotes with spaces.
		StringTokenizer st = new StringTokenizer(buffer.toString());
		IPath[] p = new IPath[st.countTokens()];

		for(int i = 0; st.hasMoreTokens(); i++) {
			p[i] = new Path((String)st.nextToken());
		}

		return p;
	}

	void parseLibVPath(IPath[] array) {
		ArrayList list = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++) {
			if (array[i].toString().charAt(0) != '-') {
				list.add (array[i]);
			}
		}
		libPaths = (IPath[])list.toArray(new IPath[list.size()]);
	}

	void parseIncVPath(IPath[] array){
		ArrayList list = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++) {
			if (array[i].toString().charAt(0) != '-') {
				list.add (array[i]);
			}
		}
		includePaths = (IPath[])list.toArray(new IPath[list.size()]);
	}

	void parseLibs(IPath[] array){
		ArrayList list = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++) {
			if (array[i].toString().charAt(0) != '-') {
				list.add (array[i]);
			}
		}
		libs = (IPath[])list.toArray(new IPath[list.size()]);
	}
}
