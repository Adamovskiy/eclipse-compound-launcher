package info.adamovskiy.compound;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "info.adamovskiy.compound.ui.messages"; //$NON-NLS-1$
	public static String CompoundLaunchConfigurationDelegate_task_name;
	public static String CompoundLaunchConfigurationDelegate_subtask;
	public static String CompoundLaunchConfigurationDelegate_no_config_error;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
