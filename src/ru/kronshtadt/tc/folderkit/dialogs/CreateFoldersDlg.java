package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;

import org.apache.log4j.Logger;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Create Folders functionality user interface
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class CreateFoldersDlg extends BaseFolderkitDialog
{

	private static final long serialVersionUID = -1674263869832624382L;
	public final static Logger logger = Logger.getLogger(CreateFoldersDlg.class);
	private  TCComponentFolderType fldr;
	private static final String strFolderType = "Folder"; //$NON-NLS-1$

	/**
	 * Constructs the class with the folder selected
	 * 
	 * @param cc
	 * @param parent
	 */
	public CreateFoldersDlg(AIFComponentContext[] cc, Frame parent)
	{
		super(cc, parent);
		setTitle(Messages.CreateFoldersDlg_Title);
		fldr = new TCComponentFolderType();
		try
		{
			fldr = (TCComponentFolderType) ((TCSession) session)
					.getTypeComponent(strFolderType);
		} catch (TCException e)
		{
			logger.error(e.getStackTrace());
		}

	}

	/**
	 * Creates folder by the name given Input string could contain one or two
	 * fields: the first one is always the name of a new folders, and the
	 * second, optional, is the name of a new folder's type
	 */
	@Override
	protected
	boolean processItem(String item)
	{
		String[] fields = item.split("\t"); //$NON-NLS-1$
		try
		{
			TCComponentFolder newFolder;
			switch (fields.length)
			{
			case 1:
				newFolder = fldr.create(fields[0], "", strFolderType); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case 2:
				newFolder = fldr.create(fields[0], fields[1], strFolderType); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			default:
				throw new TCException(Messages.CreateFoldersDlg_InvaildLine
						+ item);
			}
			parentFolder.add("contents", newFolder); //$NON-NLS-1$
		} catch (TCException e)
		{
			MessageBox.post(e);
			return false;
		}
		return true;
	}

}
