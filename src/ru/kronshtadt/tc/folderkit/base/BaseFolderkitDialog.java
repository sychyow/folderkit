package ru.kronshtadt.tc.folderkit.base;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentPseudoFolder;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.rac.util.VerticalLayout;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.utils.WindowsUtils;

/**
 * Base dialog class for CreateFolders and CreateItems functions. Contains
 * methods shared between those two.
 * 
 * 
 * @author Dmitriy.Sychev
 * 
 */
public abstract class BaseFolderkitDialog extends BasicFolderkitDialog
{
	
	public AIFComponentContext target_comps[];
	protected List<String> Items;	
	protected JTextField txtFilePath;
	protected JLabel lblStatus;
	protected JProgressBar pbBar;
	private JButton btnBrowse;
	protected JButton okButton;
	private boolean isFile = true; 
	private String folderPath = "";

	/**
	 * DocumentListener implementation for file name selected
	 * 
	 * @author Dmitriy.Sychev
	 * 
	 */
	private class FileChanger implements DocumentListener
	{
		public void removeUpdate(DocumentEvent arg0)
		{
		}

		@Override
		public void insertUpdate(DocumentEvent arg0)
		{
			loadFile();
		}

		@Override
		public void changedUpdate(DocumentEvent arg0)
		{
		}
	}
	
	/**
	 * Private class to run an action and show the result
	 * 
	 * @author Dmitriy.Sychev
	 * 
	 */
	public class onOKAction implements Runnable
	{

		@SuppressWarnings("deprecation")
		@Override
		public void run()
		{
			okButton.setEnabled(false);
			try
			{
				for (int i = 0; i < Items.size(); i++)
				{
					if (processItem(Items.get(i)))
					{
						pbBar.setValue(i + 1);
					} else
					{
						parentFolder.unlock();
						pbBar.setString(Messages.BaseDlg_Failed);
						okButton.setEnabled(true);
						return;
					}
				}
				parentFolder.unlock();
				if (showLog && log.size() > 0)
				{
					StringViewerDialog resDlg = new StringViewerDialog(log
							.toArray(new String[log.size()]), false);
					resDlg.setTitle(Messages.BaseDlg_LogTitle);
					resDlg.setVisible(true);
					dispose();
				} else
				{
					okButton.setText(strClose);
					okButton.setEnabled(true);
				}
			} catch (TCException e)
			{
				MessageBox.post(e);
			}
		}
	}
	
	/**
	 * Private class to run an action and show the result
	 * 
	 * @author Ann
	 * 
	 */
	private class onOKFolderAction implements Runnable
	{

		@SuppressWarnings("deprecation")
		@Override
		public void run()
		{
			okButton.setEnabled(false);				
			try
			{
				processItem(folderPath);
				parentFolder.unlock();
				if (showLog && log.size() > 0)
				{
					StringViewerDialog resDlg = new StringViewerDialog(log.toArray(new String[log.size()]), false);
					resDlg.setTitle(Messages.BaseDlg_LogTitle);
					resDlg.setVisible(true);
					dispose();
				} else
				{
					okButton.setText(strClose);
					okButton.setEnabled(true);
				}
			} catch (TCException e)
			{
				MessageBox.post(e);
			}
		}
	}
	

	private static final long serialVersionUID = -4272809214108341366L;

	/**
	 * Standard constructor
	 * 
	 * @param aaifcomponentcontext
	 * @param frame
	 */
	public BaseFolderkitDialog(AIFComponentContext aaifcomponentcontext[],
			Frame frame)
	{
		super(frame);
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		target_comps = aaifcomponentcontext;
		parentFolder = (TCComponent) aaifcomponentcontext[0].getComponent();
		if (parentFolder instanceof TCComponentPseudoFolder) {
			try {
				addProperty = ((TCComponentPseudoFolder)parentFolder).getDefaultPasteRelation();
				parentFolder = ((TCComponentPseudoFolder)parentFolder).getOwningComponent();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		initializeDialog(aaifcomponentcontext);
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param aaifcomponentcontext
	 * @param frame
	 */
	public BaseFolderkitDialog(AIFComponentContext aaifcomponentcontext[],
			Frame frame, boolean isFile)
	{
		super(frame);
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		target_comps = aaifcomponentcontext;
		parentFolder = (TCComponent) aaifcomponentcontext[0].getComponent();
		if (parentFolder instanceof TCComponentPseudoFolder) {
			try {
				addProperty = ((TCComponentPseudoFolder)parentFolder).getDefaultPasteRelation();
				parentFolder = ((TCComponentPseudoFolder)parentFolder).getOwningComponent();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		this.isFile = isFile;
		initializeDialog(aaifcomponentcontext);
	}

	/**
	 * A helper constructor from standard template
	 * 
	 * @param aaifcomponentcontext
	 */
	public BaseFolderkitDialog(AIFComponentContext aaifcomponentcontext[])
	{
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		target_comps = aaifcomponentcontext;
		initializeDialog(aaifcomponentcontext);
	}

	/**
	 * Initialises dialog data and constructs the visual components
	 * 
	 * @param aaifcomponentcontext
	 * 
	 * Изменено аней
	 */
	private void initializeDialog(AIFComponentContext aaifcomponentcontext[])
	{
		Items = new ArrayList<>();
		pathLastDir = WindowsUtils.getCurrentUserDesktopPath();

		final Image img = new ImageIcon(this.getClass().getResource(
				Messages.BaseDlg_FolderkitIcon)).getImage();
		setIconImage(img);

		final Container pnl = this.getContentPane();
		pnl.setLayout(new VerticalLayout());

		final JLabel lblPath = new JLabel();
		lblPath.setText(Messages.BaseDlg_Path);

		txtFilePath = new JTextField();
		txtFilePath.setPreferredSize(new Dimension(250, 20));
		txtFilePath.getDocument().addDocumentListener(new FileChanger());

		btnBrowse = new JButton("..."); //$NON-NLS-1$
		btnBrowse.setSize(30, 20);
		
		JPanel pnlFile = new JPanel();
		pnlFile.setLayout(new HorizontalLayout());
		pnlFile.add("left.nobind.left.center", lblPath); //$NON-NLS-1$
		pnlFile.add("unbound.nobind.left.center", txtFilePath); //$NON-NLS-1$
		pnlFile.add("right.nobind.right.center", btnBrowse); //$NON-NLS-1$
		pnl.add("top.bind.left.center", pnlFile); //$NON-NLS-1$
		okButton = new JButton(Messages.BaseDlg_Create);
		
		if(this.isFile) {
			btnBrowse.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					runFileChooser();
				}
			});
						
			lblStatus = new JLabel(""); //$NON-NLS-1$
			pnl.add("top.nobind.left.center", lblStatus); //$NON-NLS-1$
			
			pbBar = new JProgressBar();
			pbBar.setStringPainted(true);
			pnl.add("top.bind.center.center", pbBar); //$NON-NLS-1$
			
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onOK();
				}
			});
		}
		else {
			btnBrowse.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					runFolderChooser();
				}
			});
			
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onIMP();
				}
			});
		}
		
		final JPanel addPanel = getAdditionalPanel();
		if (addPanel != null)
			pnl.add("top.nobind.left.center", addPanel); //$NON-NLS-1$
		
		final JPanel buttPanel = new JPanel(new FlowLayout());
		buttPanel.add(okButton);
		pnl.add("bottom.bind.left.center", buttPanel); //$NON-NLS-1$

		if (addPanel==null)
			setSize(400, 300);
		centerToScreen();
		setModal(true);
	}

	protected JPanel getAdditionalPanel() {
		return null;
	}

	/**
	 * Runs "Open file..." dialog and populates data
	 */
	protected void runFileChooser()
	{
		final JFileChooser fc = new JFileChooser(pathLastDir);
		fc.setFileFilter(new TxtFileFilter());
		final int ret = fc.showDialog(this, Messages.BaseDlg_OpenFile);
		if (ret == JFileChooser.APPROVE_OPTION)
		{
			final File file = fc.getSelectedFile();
			txtFilePath.setText(file.getAbsolutePath());
			pathLastDir = file.getParent();
		}
	}
	
	/**
	 * Добавлено аней
	 * Runs "Open folder..." dialog and populates data
	 */
	protected void runFolderChooser()
	{
		final JFileChooser fc = new JFileChooser(pathLastDir);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
		//fc.setCurrentDirectory(new File("."));
		fc.setDialogTitle("Выбор директории");
		final int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			final File file = fc.getSelectedFile();
			folderPath = file.getAbsolutePath();
			txtFilePath.setText(folderPath);
			pathLastDir = file.getParent();
		}
	}

	/**
	 * Loads data from file selected and shows count of items loaded
	 */
	private void loadFile()
	{
		if (!isFile) return;
		Items.clear();
		try
		{
			final BufferedReader reader = new BufferedReader(new FileReader(txtFilePath
					.getText()));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
					Items.add(line);
			}
			reader.close();
		} catch (Exception e)
		{
			lblStatus.setText(e.getMessage());
		}
		lblStatus.setText(String
				.format(Messages.BaseDlg_FoundMsg, Items.size()));
	}
	
	/**
	 * Loads data from file selected and shows count of items loaded
	 */
	/*private void loadFolder()
	{
		Items.clear();
		Folders.clear();
		
		try {
			File folder = new File(folderPath);
			
			File[] folderEntries = folder.listFiles();
		    for (File entry : folderEntries)
		    {
		        if (entry.isDirectory())
		        {
		           
		            continue;
		        } else 
		    }
		}
		catch (Exception e) {
			
		}
	}*/

	/**
	 * OK button handler. Sets up the progress bar and runs a new thread with
	 * action.
	 */
	@SuppressWarnings("deprecation")
	public void onOK()
	{
		if (Items.isEmpty()) return;
		final int len = Items.size();
		if (okButton.getText().equals(strClose))
		{
			dispose();
			return;
		}
		pbBar.setMinimum(0);
		pbBar.setMaximum(len);
		try
		{
			parentFolder.lock();
		} catch (TCException e)
		{
			MessageBox.post(e);
			okButton.setText(strClose);
			pbBar.setString(Messages.BaseDlg_Failed);
			return;
		}
		Thread t = new Thread(new onOKAction());
		t.start();
	}
	
	/**
	 * OK button handler. Sets up the progress bar and runs a new thread with
	 * action.
	 */
	@SuppressWarnings("deprecation")
	public void onIMP()
	{
		//if (Items.isEmpty()) return;
		//final int len = Items.size();
		if (okButton.getText().equals(strClose))
		{
			dispose();
			return;
		}
		try
		{
			parentFolder.lock();
		} catch (TCException e)
		{
			MessageBox.post(e);
			okButton.setText(strClose);
			return;
		}
		Thread t = new Thread(new onOKFolderAction());
		t.start();
	}

	/**
	 * A place-holder to process an element form the input list
	 * 
	 * @param item
	 *            - A string from input list
	 * @return True if success, false otherwise
	 */
	protected abstract boolean processItem(String item);


}
