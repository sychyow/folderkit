package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BasicFolderkitDialog;
import ru.kronshtadt.tc.folderkit.utils.DDInserter;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.rac.util.VerticalLayout;

public class InsertDDDlg extends BasicFolderkitDialog {

	/**
	 * 
	 */
	DDInserter di;
	private final static Logger logger = Logger.getLogger(InsertDDDlg.class);

	private static final long serialVersionUID = -7464264571910012964L;
	private static final int  DELAY_THRESHOLD = 20;
	private static final int  DELAY_VALUE = 100;

	public InsertDDDlg(Frame parent, DDInserter ddi) {
		super(parent);
		setTitle(Messages.InsertDDDlg_Title);
		showLog = true;
		log = new ArrayList<>();
		di = ddi;
		Items = di.getKD();
		initialiseDialog();
	}

	/**
	 * Makes visual components for dialog
	 */
	private void initialiseDialog() {
		final Image img = new ImageIcon(this.getClass().getResource(Messages.BaseDlg_FolderkitIcon)).getImage();
		setIconImage(img);

		final Container pnl = this.getContentPane();
		pnl.setLayout(new VerticalLayout());
		final JLabel lblDoc = new JLabel(di.getTitle());
		pnl.add("top.bind.left.nobind", lblDoc); //$NON-NLS-1$

		final JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new PropertyLayout(5, 5, 5, 5, 5, 5));

		final JLabel lblRevs = new JLabel(Messages.AssignDocumentDlg_RevCount);
		final JLabel lblCount = new JLabel();
		lblCount.setText(Integer.toString(Items.size()));
		infoPanel.add("1.1.left.top", lblRevs); //$NON-NLS-1$
		infoPanel.add("1.2.left.top.resizable.resizable", lblCount); //$NON-NLS-1$
		pnl.add("top.bind.left.nobind", infoPanel); //$NON-NLS-1$

		// Progress and buttons
		lblStatus = new JLabel(""); //$NON-NLS-1$
		pnl.add("top.bind.left.nobind", lblStatus); //$NON-NLS-1$

		pbBar = new JProgressBar();
		pbBar.setStringPainted(true);
		pbBar.setPreferredSize(new Dimension(200, 16));
		pnl.add("top.bind.center.nobind", pbBar); //$NON-NLS-1$

		final JPanel buttPanel = new JPanel(new FlowLayout());
		okButton = new JButton(Messages.InsertDDDlg_Insert);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				onOK();
			}
		});

		buttPanel.add(okButton);
		pnl.add("bottom.nobind.center.center", buttPanel); //$NON-NLS-1$

		// misc rendering
		this.pack();
		this.validate();
		this.setMinimumSize(new Dimension(240, 150));
		centerToScreen();
		setModal(true);
	}

	/**
	 * Sets up the progress bar and runs a new thread with action.
	 */
	void onOK() {
		if (Items.isEmpty()) return;
		if (okButton.getText().equals(strClose))
		{
			dispose();
			return;
		}
		log.clear();
		log.addAll(di.getErrors());

		Thread t = new Thread(new onOKAction());
		t.start();
	}

	/**
	 * Private class to run an action and show the result
	 * 
	 * @author Dmitriy.Sychev
	 * 
	 */
	private class onOKAction implements Runnable {
		@Override
		public void run() {
			int len = Items.size();
			pbBar.setMinimum(0);
			pbBar.setMaximum(len);

			for (int i = 0; i < len; i++) {
				if (processItem(Items.get(i))) {
					pbBar.setValue(i + 1);
				} else {
					pbBar.setString(Messages.BaseDlg_Failed);
					return;
				}

				try {  //cause we need to show we are working on it
					if (len < DELAY_THRESHOLD)
						Thread.sleep(DELAY_VALUE);
				} catch (InterruptedException e) {
					logger.warn(e.getStackTrace());
				}
			}
			if (showLog && log.size() > 0) {
				StringViewerDialog resDlg = new StringViewerDialog(log.toArray(new String[0]), false);
				resDlg.setTitle(Messages.BaseDlg_LogTitle);
				resDlg.setVisible(true);
			}
			dispose();
		}

		/**
		 * Performs a required action against an object from queue and logs
		 * errors if any.
		 * 
		 * @param tcComponentItem
		 * @return
		 */
		protected boolean processItem(TCComponentItemRevision kdRev) {
			String objId = kdRev.getObjectString();
			try {
				objId = kdRev.getStringProperty("object_string"); //$NON-NLS-1$
				DDInserter.createRelation(kdRev, di.getPDF());
				//kdRev.add("I8_Collection", di.getPDF()); //$NON-NLS-1$
			} catch (TCException e) {
				logger.error(e.getStackTrace());
				log.add(String.format("%s\t%s", objId, e.getError())); //$NON-NLS-1$
			}
			return true;
		}
	}

	protected JLabel lblStatus;
	protected JProgressBar pbBar;
	protected JButton okButton;
	protected List<TCComponentItemRevision> Items;
	protected JCheckBox cbName;

}
