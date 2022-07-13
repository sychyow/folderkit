package ru.kronshtadt.tc.folderkit.utils;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class ProcessHelper {
	private TCComponent c;
	private TCComponent theProcess;
	private String errorMsg;
	private boolean hasProcess;
	
	public ProcessHelper(AIFComponentContext[] ccs) {
		
		c = (TCComponent) ccs[0].getComponent();
		hasProcess = false;
		errorMsg = "�� ����������� ��������� ��������";
	}
	
	public void acquireProcess() {
		if (c instanceof TCComponentItemRevision) {
			try {
				TCComponent[] activeTasks = c.getReferenceListProperty("process_stage_list");
				if (activeTasks.length==0) {
					errorMsg = "������� �� ��������� � ����� ��������.";
					return;
				}
				theProcess = activeTasks[0].getReferenceProperty("parent_process");
				if (theProcess!=null) {
					errorMsg = "";
					hasProcess = true;
				} else {
					errorMsg = "������� ������, ������� �� ����������";
				}
			} catch (TCException e) {
				errorMsg = e.getLocalizedMessage();
				return;
			}
		} else {
			errorMsg = "�������� �������, ����������� � ��������.";
		}
	}
	
	public boolean gotProcess() {
		return hasProcess;
	}
	
	public String getErrorString() {
		return errorMsg;
	}
	
	public TCComponent getProcess() {
		return theProcess;
	}

}
