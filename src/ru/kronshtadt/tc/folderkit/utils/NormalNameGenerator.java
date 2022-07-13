package ru.kronshtadt.tc.folderkit.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

import java.util.function.Function;

public final class NormalNameGenerator {
	private String format; 
	private boolean useRules = false;
	private Function<TCComponent, String> nameMethod = this::defaultName;
	public NormalNameGenerator(TCComponent c) {
		String compType = c.getType();
		if (compType.startsWith("BOM"))
			format = "%s-Состав";
		else
			format = "%s";
		if (compType.equals("UGPART"))
			useRules = true;
	}
	
	public String getName(TCComponent parent) throws TCException {
	
		if (useRules) {
			String parentType = parent.getType();
			switch (parentType) {
			case "Bs7_AssyRevision": nameMethod = (p) -> formatReplace(p, "0SB0");
				break;
			case "Bs7_DetailRevision": nameMethod = (p) -> formatReplace(p, "DCH0");
				break;
			}
		}
		
		String baseId = nameMethod.apply(parent);
		
		return String.format(format, baseId);
	}
	
	private String defaultName(TCComponent parent)  {
		String itemId;
		String revId;
		try {
			itemId = parent.getStringProperty("item_id");
			revId = parent.getStringProperty("item_revision_id");
		} catch (TCException e) {
			return e.getLocalizedMessage();
		}
		
		return revId==null?String.format("%s", itemId):String.format("%s/%s", itemId, revId);
	}
	
	private String formatReplace(TCComponent parent, String toStr)
	{
		String itemId;
		String revId;
		try {
			itemId = parent.getStringProperty("item_id");
			revId = parent.getStringProperty("item_revision_id");
			itemId = itemId.substring(0, itemId.length() - toStr.length()) + toStr;
		} catch (TCException e) {
			return e.getLocalizedMessage();
		}
		return revId==null?String.format("%s", itemId):String.format("%s/%s", itemId, revId);
	}
	
}
