package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BasicFolderkitDialog;
import ru.kronshtadt.tc.folderkit.utils.DDInserter;
import ru.kronshtadt.tc.folderkit.utils.FldUtil;
import ru.kronshtadt.tc.folderkit.utils.FolderkitStringViewerDlg;
import ru.kronshtadt.tc.folderkit.utils.WindowsUtils;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * Assigns DD to revisions
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class AssignDocumentDlg extends BasicFolderkitDialog 
{

	private static final long serialVersionUID = -6008596115143489087L;
	private final static Logger logger = Logger.getLogger(AssignDocumentDlg.class);
	
	public AIFComponentContext target_comps[];
	protected JLabel lblStatus;
	protected JProgressBar pbBar;
	protected JButton okButton;

	private TCComponentDataset dirDoc;
	private List<TCComponent> revs;
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
			pbBar.setValue(0);
			pbBar.setMaximum(revs.size());
			String objId; 
			for (TCComponent rev : revs)
			{
				objId = rev.getUid(); //we need to show something if rev is broken
				try {
					objId = rev.getStringProperty("object_string"); //$NON-NLS-1$
					lblStatus.setText(objId);
					
					DDInserter.createRelation(rev, dirDoc);
										
					//rev.add("I8_Collection", dirDoc); //$NON-NLS-1$
				} catch (TCException e) {
					
					log.add(String.format("%s\t%s", objId, e.getLocalizedMessage())); //$NON-NLS-1$
				}
				pbBar.setValue(pbBar.getValue()+1);
			}
			
			pbBar.setValue(0);
			if (log.size() > 0)
			{ // We don't need an empty results dialog, do we?
				FolderkitStringViewerDlg resDlg = new FolderkitStringViewerDlg(
						log.toArray(new String[0]), false);
				resDlg.setTitle(Messages.AssignDocumentDlg_ErrorAssign);
				resDlg.setVisible(true);
			}
			dispose();
		}
	}

	/**
	 * Standard AIF constructor and visual components initialisation
	 * 
	 * @param aaifcomponentcontext
	 * @param frame
	 * @param pdfDD 
	 */
	public AssignDocumentDlg(AIFComponentContext aaifcomponentcontext[],
			Frame frame, TCComponentDataset pdfDD)
	{
		super(frame);
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		target_comps = aaifcomponentcontext;
		parentFolder = (TCComponent) aaifcomponentcontext[0]
				.getComponent();
		dirDoc = pdfDD;
		initializeDialog(aaifcomponentcontext);
	}

	/**
	 * Standard AIF constructor and visual components initialisation
	 * 
	 * @param aaifcomponentcontext
	 */
	public AssignDocumentDlg(AIFComponentContext aaifcomponentcontext[])
	{
		session = (TCSession) aaifcomponentcontext[0].getComponent().getSession();
		target_comps = aaifcomponentcontext;
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
		
		parentFolder = (TCComponent) aaifcomponentcontext[0].getComponent();
		
		try {
			revs = extractRevisions(parentFolder);
		} catch(TCException ex) {
			logger.error(ex.getLocalizedMessage());			
		}
		
		setTitle(Messages.AssignDocumentDlg_AssignDD);

		Container pnl = this.getContentPane();
		pnl.setLayout(new VerticalLayout(2, 2, 2, 2, 2));

		makeUIcontrols(pnl);

		lblStatus = new JLabel(""); //$NON-NLS-1$
		pnl.add("unbound.nobind.left.center", lblStatus); //$NON-NLS-1$

		pbBar = new JProgressBar();
		pbBar.setStringPainted(false);
		pnl.add("unbound.bind.center.center", pbBar); //$NON-NLS-1$

		okButton = new JButton(Messages.AssignDocumentDlg_Assign);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				onOK();
			}
		});
		JPanel buttPanel = new JPanel(new FlowLayout());
		buttPanel.add(okButton);
		pnl.add("bottom.bind.left.center", buttPanel); //$NON-NLS-1$

		setMinimumSize(new Dimension(300, 190));
		setMaximumSize(new Dimension(400, 200));
		centerToScreen();
		pack();
		okButton.requestFocusInWindow();
		setModal(true);
	}
	
	
	public static List<TCComponent> extractRevisions(TCComponent parentFolder) throws TCException {
		List<TCComponent> revs = new ArrayList<>();
		TCComponent[] children = FldUtil.getChildren(parentFolder);
		for (TCComponent c : children) { //$NON-NLS-1$
				if (c instanceof TCComponentItemRevision)
					revs.add(c);
				else
					throw new TCException(Messages.AssignDocumentDlg_RevsOnly);
			}
		return revs;
	}

	private void makeUIcontrols(Container pnl) {
		final JPanel pnlDD = new JPanel();
		pnlDD.setLayout(new HorizontalLayout());
		final JLabel lblDDtitle = new JLabel();
		lblDDtitle.setText(Messages.AssignDocumentDlg_Assignee);
		final JLabel lblDDName = new JLabel();
		try {
			lblDDName.setText(dirDoc.getStringProperty("object_name")); //$NON-NLS-1$
		} catch (TCException e1) {
			lblDDName.setText(Messages.AssignDocumentDlg_ERROR);
		}
		pnlDD.add("left.nobind.left.center", lblDDtitle); //$NON-NLS-1$
		pnlDD.add("left.bind.left.center", lblDDName); //$NON-NLS-1$
		pnl.add("top.bind.center.center", pnlDD); //$NON-NLS-1$

		final JPanel pnlFolder = new JPanel();
		pnlFolder.setLayout(new HorizontalLayout());
		final JLabel lblFolderTitle = new JLabel();
		lblFolderTitle.setText(Messages.AssignDocumentDlg_DesignDocFolder);
		final JLabel lblFolderName = new JLabel();
		try {
			lblFolderName.setText(FldUtil.getName(parentFolder)); //$NON-NLS-1$
		} catch (TCException e1) {
			lblFolderName.setText(Messages.AssignDocumentDlg_ERROR);
		}
		pnlFolder.add("left.nobind.left.center", lblFolderTitle); //$NON-NLS-1$
		pnlFolder.add("left.bind.left.center", lblFolderName); //$NON-NLS-1$
		pnl.add("unbound.bind.center.center", pnlFolder); //$NON-NLS-1$
		
		final JPanel pnlCount = new JPanel();
		pnlCount.setLayout(new HorizontalLayout());
		final JLabel lblCountTitle = new JLabel();
		lblCountTitle.setText(Messages.AssignDocumentDlg_RevCount);
		final JLabel lblCount = new JLabel();
		lblCount.setText(Integer.toString(revs.size()));
		
		pnlCount.add("left.nobind.left.center", lblCountTitle); //$NON-NLS-1$
		pnlCount.add("left.bind.left.center", lblCount); //$NON-NLS-1$
		pnl.add("unbound.bind.center.center", pnlCount); //$NON-NLS-1$
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

}
