package org.eclipse.cdt.internal.core.win32;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.model.PluginDescriptorModel;
import org.eclipse.core.runtime.model.PluginFragmentModel;

/*
 * Currently this will only work for Windows XP since tasklist
 * is only shipped on XP. This could change to some JNI
 * call out to get the list since the source to 'tlist' is
 * on the msdn web site but that can be done later.
 */

public class ProcessList implements IProcessList {

	private IProcessInfo[] NOPROCESS = new IProcessInfo[0];

	public IProcessInfo[] getProcessList() {
		Process p = null;
		String command = null;
		InputStream in = null;
			IPluginDescriptor desc = CCorePlugin.getDefault().getDescriptor();
			if (desc instanceof PluginDescriptorModel) {
				PluginDescriptorModel model = (PluginDescriptorModel) desc;
				PluginFragmentModel[] fragments = model.getFragments();
				for (int i = 0; i < fragments.length; i++) {
					String location = fragments[i].getLocation();
					try {
						URL url = new URL(location + "/os/" + BootLoader.getOS() + "/" + BootLoader.getOSArch());
						File path = new File(url.getFile(), "listtasks.exe");
						if (path.exists()) {
							command = path.getCanonicalPath();
							break;
						}
					} catch (MalformedURLException e1) {
					} catch (IOException e) {
					}
				}
			}
			if (command != null) {
				try {
					p = ProcessFactory.getFactory().exec(command);
					in = p.getInputStream();
					InputStreamReader reader = new InputStreamReader(in);
					return parseListTasks(reader);
				} catch (IOException e) {
				}
			}
		return NOPROCESS;
	}
 
	public IProcessInfo[] parseListTasks(InputStreamReader reader) {
		BufferedReader br = new BufferedReader(reader);
		ArrayList processList = new ArrayList();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				int tab = line.indexOf('\t');
				if (tab != -1) {
					String proc = line.substring(0, tab).trim();
					String name = line.substring(tab).trim();
					try {
						int pid = Integer.parseInt(proc);
						processList.add(new ProcessInfo(pid, name));
					} catch (NumberFormatException e) {
						name = null;
					}
				}
			}
		} catch (IOException e) {
		}
		return (IProcessInfo[]) processList.toArray(new IProcessInfo[processList.size()]);
	}
}
