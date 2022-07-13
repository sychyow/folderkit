package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

public final class RenameProcessDlg extends AbstractAIFDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5236788500122555910L;
	private static final int FIELD_LEN = 32;
	private TCComponent process;
	private JTextField tfItemName;
	

	public RenameProcessDlg(AIFComponentContext[] ccs, Frame parent) {
		super(parent);
		process = (TCComponentItem) ccs[0].getComponent();
		initDialog();
	}

	public RenameProcessDlg(TCComponent inputProcess, Frame parent) {
		super(parent);
		process = inputProcess;
		initDialog();
	}

	private void initDialog() {	
		this.setTitle("Переименование процесса");
		JPanel parentPanel = new JPanel(new VerticalLayout(5,2,2,2,2));
		this.getContentPane().add(parentPanel);
		parentPanel.add("top", getPropsPanel());
		parentPanel.add("top", getButtonPanel(false));
		fillText();
	}
	
	private void setProp(JTextField field, String prop) throws TCException {
		String strVal = process.getStringProperty(prop);
		field.setText(strVal);
		if (strVal.length()<=FIELD_LEN)
			field.setColumns(FIELD_LEN);
	}
	
	private void fillText() {
		try {
			setProp(tfItemName, "object_name");
		} catch (TCException e) {
			e.printStackTrace();
			tfItemName.setText(e.getLocalizedMessage());
		}		
	}

	private JPanel getPropsPanel() {
		JPanel propsPanel = new JPanel(new PropertyLayout());
		tfItemName = new JTextField();
		propsPanel.add("1.1", new JLabel("Наименование"));
		propsPanel.add("1.2", tfItemName);
		return propsPanel;
	}
	
	@Override
	protected JPanel getButtonPanel(final boolean hasApplyButton) {
		    Registry registry = Registry.getRegistry(this);
		    JPanel jPanel = new JPanel((LayoutManager)new ButtonLayout());
		    final JButton okButton = new JButton(registry.getString("ok"));
		    final JButton applyButton = new JButton(registry.getString("apply"));
		    JButton jButton3 = new JButton(registry.getString("close"));
      	    jPanel.add(okButton);
		    if (hasApplyButton) {
		        jPanel.add(applyButton); 
		    } 
		    jPanel.add(jButton3);
		    okButton.setEnabled(true);
		    
		    okButton.addActionListener(new ActionListener() {
		          public void actionPerformed(ActionEvent param1ActionEvent) {
		        	  okAction();
		          }
		        });
		    if (hasApplyButton)
		      applyButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent param1ActionEvent) {
		              applyAction();
		              okApplyAction();
		            }
		          }); 
		    jButton3.addActionListener(new ActionListener() {
		          public void actionPerformed(ActionEvent param1ActionEvent) {
		        	setVisible(false);
		            dispose();
		          }
		        });
	 
		    return jPanel;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void okAction() {
		if (tfItemName.getText().length()==0) {
			MessageBox.post("Имя не может быть пустым!","Ошибка",MessageBox.ERROR);
			return;
		}
		try {
			process.lock();
			process.setStringProperty("object_name", tfItemName.getText());
			process.save();
			process.unlock();
		} catch (TCException e) {
			MessageBox.post(e);
			return;
		}
		dispose();
	}
	
	
}
