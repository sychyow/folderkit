package ru.kronshtadt.tc.folderkit.base;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import ru.kronshtadt.tc.folderkit.Messages;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Base class for all the dialogs in the package
 * 
 * @author Dmitriy.Sychev
 * 
 */
public abstract class BasicFolderkitDialog extends AbstractAIFDialog
{

	private static final long serialVersionUID = -4768422673551490228L;
	
	protected TCSession session;
	
	protected TCComponent parentFolder;
	protected String addProperty = "contents";
	
	protected String strClose = Messages.BaseDlg_Close;
	protected List<String> log = new ArrayList<>();
	protected boolean showLog = false;
	protected String pathLastDir;

	/**
	 * File filter on text files (*.txt) for "Open file..." dialogs
	 * 
	 * @author Dmitriy.Sychev
	 * 
	 */
	protected class TxtFileFilter extends FileFilter
	{
		@Override
		public String getDescription()
		{
			return Messages.BaseDlg_FilterName;
		}

		@Override
		public boolean accept(final File arg0)
		{
			if (arg0.isDirectory())
				return true;
			if (arg0.getName().endsWith(".txt"))return true; //$NON-NLS-1$
			return false;
		}
	}



	/**
	 * A basic constructor just to shut the compiler up
	 */
	public BasicFolderkitDialog()
	{

	}

	/**
	 * Interface for standard JDialog constructor
	 * 
	 * @param frame
	 */
	public BasicFolderkitDialog(Frame frame)
	{
		super(frame);
	}
}
