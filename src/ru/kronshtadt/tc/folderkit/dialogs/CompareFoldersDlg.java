package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.util.ArrayList;
import java.util.List;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BasicFolderkitDialog;
import ru.kronshtadt.tc.folderkit.base.ItemsSaver;
import ru.kronshtadt.tc.folderkit.utils.FldUtil;
import ru.kronshtadt.tc.folderkit.utils.FolderComparator;
import ru.kronshtadt.tc.folderkit.utils.FolderkitStringViewerDlg;
import ru.kronshtadt.tc.folderkit.utils.WindowsUtils;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * Compare Folders functionality user interface
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class CompareFoldersDlg extends BasicFolderkitDialog implements
		ItemsSaver
{

	private static final long serialVersionUID = -6008596115143489087L;
	private static final String FOLDER_TYPE = "Folder"; //$NON-NLS-1$
	private static final int MAX_NAME_LEN = 128;

	public AIFComponentContext targetComps[];
	
	private TCComponent cFirstFolder;
	private TCComponent cSecondFolder;
	private TCComponent diffItems[];

	private JLabel lblFirstName;
	private JLabel lblSecondName;
	protected JLabel lblStatus;
	protected JProgressBar pbBar;
	private JButton btnSwap;
	protected JButton okButton;
	

	/**
	 * Class to run the comparison in its own thread
	 * 
	 * @author Dmitriy.Sychev
	 * 
	 */
	private class onOKAction implements Runnable
	{

		/**
		 * The method sets up the progress bar and calls the comparator, then
		 * displays the results
		 */
		@Override
		public void run()
		{
			pbBar.setIndeterminate(true);
			FolderComparator fc = new FolderComparator(cFirstFolder,
					cSecondFolder);
			diffItems = fc.compareContent();
			final List<String> log = new ArrayList<>();
			for (TCComponent item : diffItems)
			{
				try
				{
					log.add(item.getStringProperty("object_string")); //$NON-NLS-1$
				} catch (TCException e)
				{
					log.add(item.getUid() + " " + e.getError()); //$NON-NLS-1$
				}

			}
			pbBar.setIndeterminate(false);
			pbBar.setValue(0);
			if (!log.isEmpty())
			{ // We don't need an empty results dialog, do we?
				log.add(0, String.format(
						Messages.CompareFoldersDlg_ReportHeader, lblFirstName
								.getText(), lblSecondName.getText()));
				FolderkitStringViewerDlg resDlg = new FolderkitStringViewerDlg(
						log.toArray(new String[0]), false);
				resDlg.setTitle(Messages.CompareFoldersDlg_ResultTitle);
				resDlg.enableSaveInFolder(CompareFoldersDlg.this);
				resDlg.setVisible(true);
			} else
			{
				MessageBox.post(AIFDesktop.getActiveDesktop().getShell(),Messages.CompareFoldersDlg_AllFound,
						Messages.CompareFoldersDlg_ResultTitle,
						MessageBox.INFORMATION);
			}
			dispose();
		}
	}

	/**
	 * Standard AIF constructor and visual components initialisation
	 * 
	 * @param aaifcomponentcontext
	 * @param frame
	 */
	public CompareFoldersDlg(AIFComponentContext aaifcomponentcontext[],
			Frame frame)
	{
		super(frame);
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		targetComps = aaifcomponentcontext;
		parentFolder = (TCComponent) aaifcomponentcontext[0]
				.getComponent();
		initializeDialog(aaifcomponentcontext);
	}

	/**
	 * Standard AIF constructor and visual components initialisation
	 * 
	 * @param aaifcomponentcontext
	 */
	public CompareFoldersDlg(AIFComponentContext aaifcomponentcontext[])
	{
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		targetComps = aaifcomponentcontext;
		initializeDialog(aaifcomponentcontext);
	}

	/**
	 * Visual components initialisation
	 * 
	 * @param aaifcomponentcontext
	 */
	private void initializeDialog(AIFComponentContext aaifcomponentcontext[])
	{
		pathLastDir = WindowsUtils.getCurrentUserDesktopPath();

		cFirstFolder = (TCComponent) aaifcomponentcontext[0]
				.getComponent();
		cSecondFolder = (TCComponent) aaifcomponentcontext[1]
				.getComponent();

		final Image img = new ImageIcon(this.getClass().getResource(
				Messages.BaseDlg_FolderkitIcon)).getImage();
		setIconImage(img);

		setTitle(Messages.CompareFoldersDlg_Title);

		final Container pnl = this.getContentPane();
		pnl.setLayout(new VerticalLayout(2, 2, 2, 2, 2));

		final JPanel pnlFirst = new JPanel();
		pnlFirst.setLayout(new HorizontalLayout());
		final JLabel lblFirstTitle = new JLabel();
		lblFirstTitle.setText(Messages.CompareFoldersDlg_FirstFolderTitle);
		lblFirstName = new JLabel();
		lblFirstName.setPreferredSize(new Dimension(150, 20));
		pnlFirst.add("left.nobind.left.center", lblFirstTitle); //$NON-NLS-1$
		pnlFirst.add("left.bind.left.center", lblFirstName); //$NON-NLS-1$
		pnl.add("top.bind.center.center", pnlFirst); //$NON-NLS-1$

		btnSwap = new JButton();
		final JPanel pnlSwap = new JPanel();
		pnlSwap.setLayout(new VerticalLayout());
		final ImageIcon imgSwap = new ImageIcon(this.getClass().getResource("/icons/Swap16.png")); //$NON-NLS-1$
		btnSwap.setIcon((Icon) imgSwap);
		btnSwap.setMinimumSize(new Dimension(imgSwap.getIconWidth() + 5,
				imgSwap.getIconHeight() + 5));
		btnSwap.setMargin(new Insets(0, 0, 0, 0));
		btnSwap.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				doSwap();
			}
		});
		pnlSwap.add("top.nobind.center.center", btnSwap); //$NON-NLS-1$
		pnlSwap.setMinimumSize(new Dimension(450, btnSwap.getHeight() + 2));
		pnl.add("unbound.nobind.center.center", pnlSwap); //$NON-NLS-1$

		final JPanel pnlSecond = new JPanel();
		pnlSecond.setLayout(new HorizontalLayout());
		final JLabel lblSecondTitle = new JLabel();
		lblSecondTitle.setText(Messages.CompareFoldersDlg_SecondFolderTitle);
		lblSecondName = new JLabel();
		lblSecondName.setPreferredSize(new Dimension(150, 20));
		pnlSecond.add("left.nobind.left.center", lblSecondTitle); //$NON-NLS-1$
		pnlSecond.add("left.bind.left.center", lblSecondName); //$NON-NLS-1$
		pnl.add("unbound.bind.center.center", pnlSecond); //$NON-NLS-1$

		updateFolderNames();

		lblStatus = new JLabel(""); //$NON-NLS-1$
		pnl.add("unbound.nobind.left.center", lblStatus); //$NON-NLS-1$

		pbBar = new JProgressBar();
		pbBar.setStringPainted(false);
		pnl.add("unbound.bind.center.center", pbBar); //$NON-NLS-1$

		okButton = new JButton(Messages.CompareFoldersDlg_CheckBtn);
		okButton.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e)
			{
				onOK();
			}
		});
		final JPanel buttPanel = new JPanel(new FlowLayout());
		buttPanel.add(okButton);
		pnl.add("bottom.bind.left.center", buttPanel); //$NON-NLS-1$

		setMinimumSize(new Dimension(300, 190));
		setMaximumSize(new Dimension(400, 200));
		centerToScreen();
		pack();
		okButton.requestFocusInWindow();
		setModal(true);

	}

	/**
	 * Puts the names of the selected objects into the dialog controls
	 */
	private void updateFolderNames()
	{
		try
		{
			lblFirstName.setText(FldUtil.getName(cFirstFolder));
			lblSecondName.setText(FldUtil.getName(cSecondFolder)); 
		} catch (TCException e1)
		{
			MessageBox.post(e1);
		}
	}

	/**
	 * Swaps source and destination folders
	 */
	private void doSwap()
	{
		TCComponent tmp;
		tmp = cSecondFolder;
		cSecondFolder = cFirstFolder;
		cFirstFolder = tmp;
		updateFolderNames();
	}

	/**
	 * OK button handler, runs the action
	 */
	void onOK()
	{
		if (okButton.getText().equals(strClose))
		{
			dispose();
			return;
		}
		Thread t = new Thread(new onOKAction());
		t.start();

	}
	
	/**
	 * Auxiliary routine to ensure our name for the result store folder will be no longer than 128 chars,
	 * so there will be no object_name violation to happen. 
	 * @return folder name
	 */
	private String getResFolderName(){
		String fldName = String.format(Messages.CompareFoldersDlg_StoreFolderTitle, lblFirstName.getText(), lblSecondName.getText()); 
		if (fldName.length() > MAX_NAME_LEN) {
			int deltaLen = (int) Math.round(Math.ceil((fldName.length()-128)/2.0));
			
			String firstName;
			String secondName;
			firstName = lblFirstName.getText();
			int nameLen = firstName.length();
			if (nameLen>deltaLen+1) firstName = firstName.substring(0, nameLen - deltaLen-1);
			secondName = lblSecondName.getText();
			nameLen = secondName.length();
			if (nameLen>deltaLen+1) secondName = secondName.substring(0, nameLen - deltaLen-1);
			
			fldName = String.format(Messages.CompareFoldersDlg_StoreFolderTitle, firstName, secondName);
		}
		return fldName;
	}

	/**
	 * Saves the items found by comparison into a specifically created folder
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void saveInFolder()
	{
		try
		{
			final TCSession s = cFirstFolder.getSession();
			final TCComponentFolder homeFolder = s.getUser().getHomeFolder();
			final TCComponentFolderType flt = (TCComponentFolderType) s
					.getTypeComponent(FOLDER_TYPE);
			final TCComponentFolder newFolder = flt.create(getResFolderName(), "", //$NON-NLS-1$
					FOLDER_TYPE);
			homeFolder.add("contents", newFolder); //$NON-NLS-1$
			homeFolder.unlock();
			newFolder.lock();
			for (TCComponent item : diffItems)
			{
				newFolder.add("contents", item); //$NON-NLS-1$
			}
			newFolder.unlock();
			MessageBox.post(Messages.CompareFoldersDlg_ItemsSaved,
					Messages.CompareFoldersDlg_InfoTitle,
					MessageBox.INFORMATION);
		} catch (TCException e)
		{
			MessageBox.post(e);
		}

	}



}
