package ru.kronshtadt.tc.folderkit;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ru.kronshtadt.tc.folderkit"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		
		/*
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		try {
			String syslog = session.getLogInfo()[1];
			String raclog = TcLogger.getCurrentLoggingFile();
			String basePath = System.getenv().get("USERPROFILE");
			File prevFile = new File(String.format("%s\\Teamcenter\\prev.session", basePath));
			File currFile = new File(String.format("%s\\Teamcenter\\curr.session", basePath));
			if (prevFile.exists()) prevFile.delete();
			if (currFile.exists())
				Files.copy(currFile.toPath(), prevFile.toPath());
			PrintWriter writer = new PrintWriter(new FileWriter(currFile));
		    writer.println(syslog);
		    writer.println(raclog);
		    writer.close();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}



}
