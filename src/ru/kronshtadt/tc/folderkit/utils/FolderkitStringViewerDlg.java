package ru.kronshtadt.tc.folderkit.utils;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.ItemsSaver;
import com.teamcenter.rac.util.StringViewerDialog;

/**
 * Standard StringViewerDialog customisation with an additional 'Save to folder'
 * button. Customer's wish is the law, you know.
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class FolderkitStringViewerDlg extends StringViewerDialog
{
	private static final long serialVersionUID = 3214545733712520806L;
	
	private JButton btnFolder;

	/**
	 * Inherited constructor stub
	 */
	public FolderkitStringViewerDlg()
	{
		super(new String[0]);
	}

	/**
	 * Inherited constructor stub
	 */
	public FolderkitStringViewerDlg(final String[] arg0)
	{
		super(arg0);
	}

	/**
	 * Inherited constructor stub
	 */
	public FolderkitStringViewerDlg(final String[] array, final boolean b)
	{
		super(array, b);
	}



	/**
	 * Adds a 'Save to folder' button with a handled implemented in a class
	 * passed through the argument
	 * 
	 * @param saver
	 */
	public void enableSaveInFolder(final ItemsSaver saver)
	{
		final ItemsSaver itemSaver = saver;
		btnFolder = new JButton();
		btnFolder = new JButton(new ImageIcon(this.getClass().getResource(
				"/icons/ToFolder.png"))); //$NON-NLS-1$
		btnFolder.setMargin(new Insets(0, 0, 0, 0));
		btnFolder
				.setToolTipText(Messages.FolderkitStringViewerDlg_SaveItemsToolTip);
		btnFolder.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent event)
			{
				itemSaver.saveInFolder();
				toFront(); // We have a MessageBox in saveInFolder
							// implementation, so have to restore focus now
			}
		});
		viewerPanel.buttonPanel.add("Right", btnFolder); //$NON-NLS-1$
	}

	
}
