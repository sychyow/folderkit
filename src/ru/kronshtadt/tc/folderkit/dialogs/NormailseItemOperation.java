package ru.kronshtadt.tc.folderkit.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;

import ru.kronshtadt.tc.folderkit.utils.ItemNormaliser;

public final class NormailseItemOperation extends AbstractAIFOperation {

	private TCComponent comp;
	private ItemNormaliser normaliser;
	
	private Set<String> excludeTypes = new HashSet<>(Arrays.asList("Bs7_AltGeom", "Bs7_Standart"));
	
	public NormailseItemOperation(AIFComponentContext aContext) throws Exception {
		comp = (TCComponent) aContext.getComponent();		
	}

	@Override
	public void executeOperation() throws Exception {
		TCSession session = comp.getSession();
		session.setStatus("Нормализуем " + " "
				+ comp.getStringProperty("object_string"));
		TCComponentItem item;
		if (comp instanceof TCComponentItem) {
			item = (TCComponentItem) comp;
		} else {
			throw new Exception("Этот вид объектов не поддерживатся");
		}
		if (excludeTypes.contains(comp.getType()))
			throw new Exception("Нормализация не нужна");
		normaliser = new ItemNormaliser(item);		
		normaliser.normalise();
		session.setReadyStatus();
	}

}
