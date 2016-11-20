package com.rem.core.gui.graphics;

import com.rem.core.Hub;

public class GraphicLine extends GraphicEntity{

	public static final int SEGMENT_COUNT = 10;
	public GraphicLine(int layer) {
		super("squares", layer);
		this.entity = Hub.creator.createGraphicLine("blank", this);
		this.entity.setLayer(layer);
	}
}
