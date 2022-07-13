package ru.kronshtadt.tc.folderkit.utils;

import java.util.List;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;

public abstract class BaseUtil {
	public final static Logger logger = Logger.getLogger(BaseUtil.class);
	protected List<String> log = null;
	protected TCComponentItemType itt;
	protected TCComponentItemRevisionType irt;
	protected static DataManagementService dms = null;
	protected static TCSession tcSession;
	protected String item;
	
	public BaseUtil() {
		tcSession = (TCSession) AIFUtility.getDefaultSession();
		try {
			itt = (TCComponentItemType) tcSession.getTypeComponent("Item");
			irt = (TCComponentItemRevisionType) tcSession.getTypeComponent("ItemRevision");
		} catch (TCException e) {
			logger.error(e.getStackTrace());
		}
	}
	
	protected static DataManagementService getDMS() {
		if (dms == null) {
			dms = DataManagementService.getService(tcSession);
		}
		return dms;
	}

	public void setLog(List<String> strLog) {
		log = strLog;
	}

	protected void addLog(String val) {
		if (log == null)
			return;
		log.add(String.format("%s\t%s", item, val));
	}



	
}
