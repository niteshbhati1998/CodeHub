package com.cognizant.lineage.upload.model;

import java.util.List;

import lombok.Data;

@Data
public class SaveDataOrExecuteScriptForShiftNode {

	private List<WaveDetailUi> waveDetailUiList;
	private ShiftNodeDetails shiftNodeDetails;
}
