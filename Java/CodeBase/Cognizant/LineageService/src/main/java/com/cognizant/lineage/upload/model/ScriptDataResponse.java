package com.cognizant.lineage.upload.model;

import java.util.List;

public class ScriptDataResponse {
	
	private List<ScriptDetails> popupDetailsForScript;
	private List<TableDetails> popupDetailsForTable;
	
	public List<ScriptDetails> getPopupDetailsForScript() {
		return popupDetailsForScript;
	}
	public void setPopupDetailsForScript(List<ScriptDetails> popupDetailsForScript) {
		this.popupDetailsForScript = popupDetailsForScript;
	}
	public List<TableDetails> getPopupDetailsForTable() {
		return popupDetailsForTable;
	}
	public void setPopupDetailsForTable(List<TableDetails> popupDetailsForTable) {
		this.popupDetailsForTable = popupDetailsForTable;
	}

}
