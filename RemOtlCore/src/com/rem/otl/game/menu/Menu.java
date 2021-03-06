package com.rem.otl.game.menu;

import java.util.ArrayList;
import java.util.List;

import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.gui.inputs.HoverEvent;
import com.rem.otl.game.environment.Square;

public class Menu extends GraphicView{
	private static List<Square> squares=new ArrayList<Square>();
	protected double since = 0;
	public Menu(){
		super();
		if(Hub.music!=null){
			addChild(Hub.music);		
		}
		addChild(new GraphicEntity("squares"));
		getChild(size()-1).setFrame(15);
		for(int i=0;i<squares.size();++i){
			addChild(squares.get(i));
		}
	}
	@Override
	public boolean onHover(HoverEvent e){
		return super.onHover(e);
	}
	@Override
	public void update(double seconds){
		if(seconds>0.2f)return;
		since+=seconds;
		if(since>0.1f){
			since-=0.1f;
			float w = (float) (0.4*Math.random());
			squares.add(new Square((int)(16*Math.random()),w,w));
			squares.get(squares.size()-1).reposition(
					   (float) (-0.2f+1.4*Math.random()),
						1f+1.5f*(float)Math.random());
			addChild(squares.get(squares.size()-1));
			squares.get(squares.size()-1).getGraphicElement().rotate((float)(Math.random()-0.5f));
		}
		for(int i=0;i<squares.size();++i){
			Square square = squares.get(i);
			square.getGraphicElement().rotate((float) (square.getGraphicElement().getAngle()+Math.signum(square.getGraphicElement().getAngle())*seconds*10f/square.getWidth()));
			if(square.getY()>1f){
			//	square.setX((float) (square.getX()+(mouseX-square.getX())/2f*seconds));
			}
			square.reposition(square.getX(),(float) (square.getY()-0.1f*seconds));
			if(square.getY()<-1f){
				removeChild(square);
				squares.remove(square);
				--i;
			}
		}		
	}	
}
