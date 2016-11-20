package com.rem.otl.pc.gui.graphics;

import org.lwjgl.opengl.GL11;

import com.rem.core.gui.graphics.GraphicElement;
import com.rem.core.gui.graphics.GraphicView;

public class PCGraphicElement extends GraphicElement {
	public PCGraphicElement(String textureName, GraphicView view) {
		super(textureName, view);
	}

	@Override
	protected void draw(){
		GL11.glPushMatrix();
		GL11.glTranslatef(visualX, visualY, 0.0f);
		if(angle!=0f){
			GL11.glTranslatef(visualW/2f, visualH/2f, 0.0f);
			GL11.glRotatef(angle, 0, 0, 1);	
			GL11.glTranslatef(-visualW/2f, -visualH/2f, 0.0f);
		}
		GL11.glScalef(visualW, visualH, 1f);
		GL11.glVertexPointer(3, 0, vertexBuffer);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, vertexNumber);
		GL11.glPopMatrix();
	}
}
