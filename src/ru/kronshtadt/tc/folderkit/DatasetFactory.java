package ru.kronshtadt.tc.folderkit;

import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

public class DatasetFactory {

	private static String typeDataset;

	private static TCSession session;
	private static TCComponentDatasetType dst;

	public DatasetFactory(TCSession session) {
		DatasetFactory.session = session;
	}

	public void addDataSet(TCComponentItemRevision Rev, String filePathName) throws TCException {

		TCComponent[] revChildren = Rev.getReferenceListProperty("IMAN_specification");
		TCComponentDataset NewDataSet = null;

		String typeRef = dst.getReferenceNames().get(0);

		if (revChildren.length != 0) {
			for (TCComponent child : revChildren) {
				if (child.getType().contains(typeDataset)) {
					NewDataSet = (TCComponentDataset) child;
					break;
				}
			}
		}
		if (NewDataSet == null) {
			NewDataSet = dst.create(Rev.getStringProperty("item_id") + "/" + Rev.getStringProperty("item_revision_id"),
					"", typeDataset);
			Rev.add("IMAN_specification", NewDataSet);
		}
		NewDataSet.setFiles(new String[] { filePathName }, new String[] { typeRef });
	}

	public static String getFileExtension(String filePathName) {
		int dotIndex = filePathName.lastIndexOf(".");
		if (dotIndex > 0) {
			return filePathName.substring(dotIndex + 1);
		} else
			return "";
	}

	public static class PreferenceMap {

		public static HashMap<String, TCComponentDatasetType> UsedDatasetMap = new HashMap<>();
		public static final HashMap<String, String> DatasetTypeMap = new HashMap<>();

		static {
			TCPreferenceService prefService = session.getPreferenceService();
			String[] preferenceArray = prefService.getStringValues("DRAG_AND_DROP_default_dataset_type");
			for (String element : preferenceArray) {
				String[] map = element.split(":");
				DatasetTypeMap.put(map[0], map[1]);
			}
		}

		public static boolean setType(String[] fields) throws TCException {

			if (fields.length > 3) {
				typeDataset = getDtsType(getFileExtension(fields[3]));
				if (typeDataset == null)
					return false;

				TCComponentDatasetType currentDst = UsedDatasetMap.get(typeDataset);
				if (currentDst == null) {
					dst = (TCComponentDatasetType) session.getTypeComponent(typeDataset);
					UsedDatasetMap.put(typeDataset, dst);
				} else {
					dst = currentDst;
				}
				return true;

			}
			return true;
		}

		public static String getDtsType(String key) {
			return DatasetTypeMap.get(key);
		}
	}

}