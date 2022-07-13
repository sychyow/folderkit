package ru.kronshtadt.tc.folderkit.utils;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.dialogs.InsertDDDlg;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOrUpdateRelationsInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOrUpdateRelationsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.SecondaryData;
import com.teamcenter.soa.client.model.ErrorStack;

/**
 * @author Dmitriy.Sychev
 *
 */
public class DDInserter {
	
	private static final String DD_RELATION = "I8_Collection";
	private final static Logger logger = Logger.getLogger(InsertDDDlg.class );
	private static DataManagementService dms = null;
	
	private static final int MODE_CN = 1;
	private static final int MODE_AN = 2;
	private static String relations[] = {"I8_DDReleased", "I8_DDLimited", "I8_DDCancelled", "I8_DDClarified", "I8_DDRestored"};
	private int mode;
	private TCComponentDataset pdf;
	private final TCComponentItemRevision dirDoc;
	private final List<String> errLog = new ArrayList<>();
	private String title; 
	
	public DDInserter(final TCComponentItemRevision ddRev)
	{
		dirDoc = ddRev;
		try {
			title = ddRev.getItem().getStringProperty("object_string");
		} catch (TCException e) {
			logger.error(e.getStackTrace());
			title = Messages.AssignDocumentDlg_ERROR;
		}
		try {
			final String type = ddRev.getStringProperty("object_type");
			if (type.contains("I8_ChangeNote")) {
				mode  = MODE_CN;
				return;
			}
			if (type.contains("I8_AddNote")) {
				mode  = MODE_AN;
				return;
			}
			mode = 0;
		}catch(TCException ex) {
			logger.error(ex.getStackTrace());
			mode = 0;
		}
		
	}
	
	public static DataManagementService getDMS() 
	{
		if (dms==null) {
			TCSession tcSession = (TCSession) AIFUtility.getDefaultSession();
			dms = DataManagementService.getService(tcSession);
		}
		return dms;
	}
	
	public static void createRelation(TCComponent parent, TCComponent child) throws TCException
	{
		CreateOrUpdateRelationsInfo ri = new CreateOrUpdateRelationsInfo();
		ri.primaryObject = parent;
		ri.secondaryData = new SecondaryData[1];
		ri.secondaryData[0] = new SecondaryData();
		ri.secondaryData[0].secondary = child;
		ri.relationType = DD_RELATION;
		CreateOrUpdateRelationsResponse res = getDMS().createOrUpdateRelations(new CreateOrUpdateRelationsInfo[]{ri}, false);
		
		if (res.serviceData.sizeOfPartialErrors()>0) {
			ErrorStack err = res.serviceData.getPartialError(0);
			throw new TCException(err.getErrorValues()[0].getMessage());
		}
	}
	
	public boolean hasErrors() 
	{
		return !errLog.isEmpty();
	}
	
	public String getTitle() { return title;}
	
	/**
	 * Returns error log
	 * @return log lines
	 */
	public List<String> getErrors() { return errLog;}
	
	public TCComponentDataset getPDF() {return pdf;}
	
	public boolean loadPDF()
	{
		try {
			final TCComponent dsets[] = dirDoc.getRelatedComponents("IMAN_specification");
			pdf = null;
			for (final TCComponent dset : dsets) {
				String type;
				try {
					type = dset.getStringProperty("object_type");
					if ("PDF".equals(type)) {
						pdf = (TCComponentDataset) dset;
						return true;
					}
				} catch (TCException e1) {
					continue;
				} 
			}
		} catch (TCException e) {
			logger.error(e.getStackTrace());
			return false;
		} 
		return false;
	}
	
	private void insertCN(final TCComponentItemRevision cnRev)
	{
		for(final String relation : relations) {
			TCComponent[] kdRevs;
			try {
				kdRevs = cnRev.getRelatedComponents(relation);
			} catch (TCException e) {
				logger.error(e.getStackTrace());
				errLog.add(e.getLocalizedMessage());
				continue;
			}
			for (final TCComponent kd : kdRevs) {
				try {
					kd.add(DD_RELATION, pdf);
				} catch (TCException e) {
					logger.error(e.getError());
					errLog.add(e.getLocalizedMessage());
					continue;
				}
			}
		}
	}
	
	/** 
	 * Returns ChangeNote revision, either generic or linked to AddNote
	 * @return CN revision component
	 */
	public TCComponentItemRevision getCN()
	{
		if (mode==MODE_CN) return dirDoc;
		
		TCComponentItemRevision changeNote = null;
		try {
			changeNote = (TCComponentItemRevision) dirDoc.getRelatedComponent("I8_DDCNRelation");
		} catch (TCException e) {
			logger.error(e.getStackTrace());
			errLog.add(e.getLocalizedMessage());
		}
		return changeNote;
	}
	
	public List<TCComponentItemRevision> getKD()
	{
		final TCComponentItemRevision cnRev = getCN();
		final List<TCComponentItemRevision> res = new ArrayList<>();
		for(final String relation : relations) {
			TCComponent[] kdRevs;
			try {
				kdRevs =  cnRev.getRelatedComponents(relation);
			} catch (TCException e) {
				logger.error(e.getStackTrace());
				errLog.add(e.getLocalizedMessage());
				continue;
			}
			for (final TCComponent rev : kdRevs) {
				res.add((TCComponentItemRevision)rev);
			}
		}
		return res;
	}
	
	/**
	 * Inserts PDF into KD linked with corresponding CN
	 */
	public void insert()
	{
		switch (mode) {
		case MODE_CN: insertCN(dirDoc);
			break;
		case MODE_AN: 
			final TCComponentItemRevision changeNote = getCN();
			if (changeNote==null) {
				errLog.add("Связанное извещение об изменении не найдено!");
			}else{
				insertCN(changeNote);
			}
			break;
		default:
			logger.error("Wrong mode found!!!");
			break;
		}
	
	}
}
