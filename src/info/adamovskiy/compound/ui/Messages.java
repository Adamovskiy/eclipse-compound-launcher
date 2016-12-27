package info.adamovskiy.compound.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "info.adamovskiy.compound.ui.messages"; //$NON-NLS-1$
	public static String AddConfigDialog_shell_text;
	public static String AddConfigDialog_title;
	public static String CompoundElementsTab_add_button;
	public static String CompoundElementsTab_async_checkbox;
	public static String CompoundElementsTab_clear_button;
	public static String CompoundElementsTab_error_empty_configs;
	public static String CompoundElementsTab_error_self_nesting;
	public static String CompoundElementsTab_name;
	public static String ConfigsTable_error_no_config;
	public static String ConfigsTable_error_unsupported_mode;
	public static String ConfigsTable_message_invalid_configs;
	public static String ConfigsTable_mode_column;
	public static String ConfigsTable_name_column;
	public static String ConfigsTable_no_override;
	public static String ConfigsTable_type_column;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
