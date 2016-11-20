package com.rem.core.gui.graphics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rem.core.Action;
import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.storage.Resource;



public abstract class GraphicRenderer {

	protected float viewX;

	protected float viewY;

	protected float viewZ=0f;

	protected LinkedList<GraphicElementArrayChangeEvent> changeEvents = new LinkedList<GraphicElementArrayChangeEvent>();
	protected List<GraphicElement> drawBotLayer = new ArrayList<GraphicElement>();
	protected List<GraphicElement> drawMidLayer = new ArrayList<GraphicElement>();
	protected List<GraphicElement> drawTopLayer = new ArrayList<GraphicElement>();

	protected Map<String,Action<ClickEvent>> loadImageFromTextureName = new HashMap<String,Action<ClickEvent>>();
	protected Map<String,Integer> texMap = new HashMap<String,Integer>();
	protected Map<String,String> sizMap = new HashMap<String,String>();
	protected Map<String,FloatBuffer[]> textureBuffers = new HashMap<String,FloatBuffer[]>();

	protected List<String> toLoadtext = new ArrayList<String>();


	public boolean animate;
	protected long last = System.currentTimeMillis();
	private boolean loaded = false;

	public long animationInterval = 100;

	public GraphicRenderer() {
	}
	public void setupTextureBuffer(int xMax, int yMax) {
		float length = (float)xMax;
		float height = (float)yMax;
		float xFactor = 0.02f/xMax;
		float yFactor = 0.02f/yMax;
		FloatBuffer[] textureBuffer = new FloatBuffer[xMax*yMax];
		float textures[];
		StringBuilder builder = new StringBuilder();
		for(int y=0;y<yMax;++y){
			for(int x=0;x<xMax;++x){
				textures= new float[]{
						// Mapping coordinates for the vertices
						x/length+xFactor, (y+1)/height-yFactor,		// top left		(V2)
						x/length+xFactor, (y)/height+yFactor,		// bottom left	(V1)
						(x+1)/length-xFactor, (y+1)/height-yFactor,		// top right	(V4)
						(x+1)/length-xFactor, (y)/height+yFactor		// bottom right	(V3)
				};
				//builder.append((x)/length+","+(y)/height+"|");

				ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2 * 4 * 4);
				byteBuffer.order(ByteOrder.nativeOrder());
				textureBuffer[y*xMax+x] = byteBuffer.asFloatBuffer();
				textureBuffer[y*xMax+x].put(textures);
				textureBuffer[y*xMax+x].position(0);
			}
			builder.append("\n");
		}
		textureBuffers.put(xMax+"x"+yMax, textureBuffer);

		//Hub.log.debug("graphicRenderer:"+xMax+"x"+yMax, builder.toString());
	}
	public boolean isLoaded(){
		return this.loaded;
	}
	public void display(){

		while(!changeEvents.isEmpty()){
			synchronized(changeEvents){
				changeEvents.removeFirst().act();
			}
		}

		render();
	}

	protected abstract void render();
	protected abstract void bindTexture(GraphicElement d);
	protected abstract void createFont(String texName, String fontName, int fontStyle,int size, float[] foreGroundColour, float[] backgroundColour);

	public void drawTexture(GraphicElement d){
		if(!texMap.containsKey(d.getTextureName())){
			System.err.println(d.getTextureName()+" not a recognized texture name");
		}
		bindTexture(d);
		d.display();
	}

	public boolean buffersInclude(int sizeX, Integer sizeY) {
		return textureBuffers.containsKey(sizeX+"x"+sizeY);
	}

	public void addElement(GraphicElement e){
		synchronized(changeEvents){
			changeEvents.add(new AddGraphicElementEvent(e));
		}
	}
	public void removeElement(GraphicElement e){
		synchronized(changeEvents){
			changeEvents.add(new RemoveGraphicElementEvent(e));
		}
	}

	public void clearAdditions(){

		synchronized(changeEvents){
			for(int i=0;i<changeEvents.size();++i){
				if(changeEvents.get(i) instanceof AddGraphicElementEvent){
					changeEvents.remove(i);
					--i;
				}
			}
		}
	}
	public void loadImages(){
		if(!loaded ){
			InputStream url = (Hub.manager.createInputStream("images"+File.separator+"image.list",IFileManager.FROM_IMAGE_RESOURCE)).get();
			//Hub.log.debug("loadImages",url);
			StringBuilder fileBuilder = new StringBuilder();
			try {
				int c = url.read();
				while(c!=-1){
					fileBuilder.append((char)c);
					c = url.read();
				}
				url.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			int currentSizeX = 1;
			int currentSizeY = 1;
			for(final String line:fileBuilder.toString().split("\n")){
				if(line.matches("\\s*"))continue;

				StringBuilder lineBuilder = new StringBuilder();
				for(char c:line.toCharArray()){
					if(c>31){
						lineBuilder.append(c);
					}
				}
				final String filename = lineBuilder.toString();
				if(filename.matches("\\d+:")){
					currentSizeX = Integer.parseInt(filename.substring(0,filename.length()-1));
					currentSizeY = 1;
				}
				else if(filename.matches("\\d+x\\d+:")){
					currentSizeX = Integer.parseInt(filename.substring(0,filename.indexOf('x')));
					currentSizeY = Integer.parseInt(filename.substring(filename.indexOf('x')+1,filename.length()-1));
				}
				else {
					final String imageName = filename.substring(0,filename.lastIndexOf('.'));
					final Integer sizeX = currentSizeX;
					final Integer sizeY = currentSizeY;
					final String imageFilename = currentSizeY==1?("images/"+sizeX+"/"+imageName+".png"):("images/"+sizeX+"x"+sizeY+"/"+imageName+".png");

					loadImageFromTextureName.put(imageName,new Action<ClickEvent>(){
						@Override
						public void act(ClickEvent event) {
							loadImageFromPath(imageName,imageFilename,sizeX,sizeY);
						}				
					});
				}
			}
			loadImageFromTextureName.put("timesnewroman",new Action<ClickEvent>(){
				@Override
				public void act(ClickEvent event) {					
					loadText("timesnewroman","Times New Roman", Hub.creator.getPlainFontStyle(),16,new float[]{0f,0.75f,0.75f,1}, new float[]{0,0,0,0f});
				}});
			loadText("impact","Cooper Black", Hub.creator.getPlainFontStyle(), 32,new float[]{0f,0f,0f,1}, new float[]{0,0,0,0f});
			loadText("impactWhite","Cooper Black", Hub.creator.getPlainFontStyle(), 32,new float[]{1f,1f,1f,1}, new float[]{0,0,0,0f});


			loaded = true;
		}
	}

	public void prepareCustomLoader(final String imageName, String dimension) {

		if(!contains(imageName)&&!loadImageFromTextureName.containsKey(imageName)){
			final int sizeX = Integer.parseInt(dimension.substring(0,dimension.indexOf('x')));
			final int sizeY = Integer.parseInt(dimension.substring(dimension.indexOf('x')+1,dimension.length()));

			final String imageFilename = "res/images/"+imageName+".png";

			loadImageFromTextureName.put(imageName,new Action<ClickEvent>(){
				@Override
				public void act(ClickEvent event) {
					loadImageFromExternalPath(imageFilename,sizeX,sizeY,imageName);
				}
			});
		}
	}
	public boolean contains(String key) {
		return texMap.containsKey(key);
	}

	public Map<String,List<Float>> letterWidths= new HashMap<String,List<Float>>();
	private void loadText(String texName, String fontName, int fontStyle, int size, float[] foregroundColour, float[] backgroundColour){
		if(!buffersInclude(16,16)){
			setupTextureBuffer(16,16);			
		}
		letterWidths.put(texName, new ArrayList<Float>());
		createFont(texName, fontName, fontStyle, size, foregroundColour, backgroundColour);
	}

	public void loadImageFromPath(String imageName, String path, int sizeX, Integer sizeY){
		if(!buffersInclude(sizeX,sizeY)){
			setupTextureBuffer(sizeX,sizeY);			
		}
		int tex = Hub.gui.createTexture(Hub.manager.createImageResource(imageName, path));
		texMap.put(imageName, tex);
		sizMap.put(imageName, sizeX+"x"+sizeY);
	}
	public void loadImageFromExternalPath(String path, int sizeX, Integer sizeY, String name){
		if(!buffersInclude(sizeX,sizeY)){			
			setupTextureBuffer(sizeX,sizeY);			
		}
		int tex = Hub.gui.createTexture(new Resource<File>(name,path,new File(path)));
		texMap.put(name, tex);
		sizMap.put(name, sizeX+"x"+sizeY);
	}
	public void translate(float x, float y, float z){
		viewX+=x;
		viewY+=y;
		viewZ+=z;
	}
	public void translateTo(float x, float y){
		viewX=x;
		viewY=y;
	}

	public float getViewX() {
		return viewX;
	}
	public float getViewY() {
		return viewY;
	}

	public void loadFont(String font) {
		if(loadImageFromTextureName.containsKey(font)){
			loadImageFromTextureName.remove(font).act(null);
		}
	}

	public int getFrameLimit(String textureName) {
		return sizMap.get(textureName).length();
	}

	private interface GraphicElementArrayChangeEvent {
		public void act();
	}
	private class AddGraphicElementEvent implements GraphicElementArrayChangeEvent{
		private GraphicElement element;

		public AddGraphicElementEvent(GraphicElement element){
			this.element = element;
		}

		@Override
		public void act() {
			if(element.getLayer()==Hub.TOP_LAYER){
				drawTopLayer.add(element);
			}
			else if(element.getLayer()==Hub.MID_LAYER){
				drawMidLayer.add(element);
			}
			else if(element.getLayer()==Hub.BOT_LAYER){
				drawBotLayer.add(element);
			}
			if(loadImageFromTextureName.containsKey(element.getTextureName())){
				loadImageFromTextureName.remove(element.getTextureName()).act(null);
			}
		}
	}
	private class RemoveGraphicElementEvent implements GraphicElementArrayChangeEvent{
		private GraphicElement element;

		public RemoveGraphicElementEvent(GraphicElement element){
			this.element = element;
		}

		@Override
		public void act() {
			if(element==null)return;
			if(element.getLayer()==Hub.TOP_LAYER){
				drawTopLayer.remove(element);
			}
			else if(element.getLayer()==Hub.MID_LAYER){
				drawMidLayer.remove(element);
			}
			else if(element.getLayer()==Hub.BOT_LAYER){
				drawBotLayer.remove(element);
			}
		}
	}
}
