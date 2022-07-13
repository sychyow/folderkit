package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;

import org.apache.log4j.Logger;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Load Items/Revisions functionality user interface
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class LoadItemsDlg extends BaseFolderkitDialog
{

	private static final long serialVersionUID = 5150530991991672511L;
	public final static Logger logger = Logger.getLogger(LoadItemsDlg.class);
	private TCComponentItemType itt;
	private TCComponentItemRevisionType irt;
	private static final String strItemType = "Item"; //$NON-NLS-1$
	private static final String strItemRevType = "ItemRevision"; //$NON-NLS-1$

	/**
	 * Initialises class with Item factory Parameters are AIF standard
	 * 
	 * @param cc
	 * @param parent
	 */
	public LoadItemsDlg(AIFComponentContext[] cc, Frame parent)
	{
		super(cc, parent);
		setTitle(Messages.LoadItemsDlg_Title);
		okButton.setText(Messages.LoadItemsDlg_Load);
		try
		{
			itt = (TCComponentItemType) ((TCSession) session)
					.getTypeComponent(strItemType);
			irt = (TCComponentItemRevisionType) ((TCSession) session)
			.getTypeComponent(strItemRevType);
		} catch (TCException e)
		{
			logger.error(e.getStackTrace());
		}
		showLog = true;
	}

	/**
	 * Processes the input string. The string must contain a correct Item ID or Item ID/Rev ID
	 */
	@Override
	protected
	boolean processItem(String item)
	{
		try
		{
			//Temporary disable
			/*if (hasCyr(item))
				throw new TCException(Messages.CreateItemsDlg_CyrillicChars);*/
			int revDivInd = item.lastIndexOf('/');
			TCComponent newItem = null;
			if (revDivInd == -1 ) {
				TCComponentItem[] items = itt.findItems(item);
				if (items.length>0) newItem = items[0];
			} else {
				TCComponentItemRevision[] revs = irt.findRevisions(item.substring(0,revDivInd), item.substring(revDivInd+1));
				if (revs.length>0) newItem = revs[0];
			}
				
			if (newItem != null) {
				parentFolder.add(addProperty, newItem); //$NON-NLS-1$
			} else {
				log.add(item);
				log.add("\t"+Messages.LoadItemsDlg_NotFound);
			}
			
		} catch (TCException e)
		{
			log.add(item);
			log.add(String.format("\t%s", e.getError())); //$NON-NLS-1$
		}
		return true;
	}

	/**
	 * Checks if the given id has cyrillic symbols
	 * @param id
	 *            item id
	 * @return true when cyrillic symbol found, false otherwise
	 */
	/*private boolean hasCyr( String id)
	{
		for (char c : id.toCharArray())
		{
			boolean isCyrillic = (0x400 <= c && c <= 0x4ff); // Cyrillic range,
																// as per The
																// Unicode
																// Standard,
																// Version 6.2
			if (isCyrillic)
				return true;
		}
		return false;
	}*/

}
