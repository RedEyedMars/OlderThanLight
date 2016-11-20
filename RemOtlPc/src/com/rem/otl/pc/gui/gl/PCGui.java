package com.rem.otl.pc.gui.gl;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.rem.core.gui.IGui;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.gui.inputs.HoverEvent;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.core.gui.inputs.WheelEvent;
import com.rem.core.Hub;
import com.rem.core.Setupable;
import com.rem.core.storage.Resource;

public class PCGui extends GLApp implements IGui {

	// Light position: if last value is 0, then this describes light direction.  If 1, then light position.

	@SuppressWarnings("unused")
	private Setupable main;

	private boolean drag = true;
	

	public PCGui(Setupable setupable){
		super();
		this.main = setupable;
		Hub.gui = this;

		window_title = "OTL";
		displayWidth = 800;
		displayHeight = 600;
	}


	/**
	 * Initialize the scene.  Called by GLApp.run()
	 */
	public void setup()
	{
    	// hold onto application class in case we need to load images from jar (see getInputStream())
    	setRootClass();
        init();
        setPerspective();
	}


	@Override
	public void update(){
		Hub.view.update(secondsSinceLastFrame); 
	}

	/**
	 * Set the camera position, field of view, depth.
	 */
	public void setPerspective(){
		// select projection matrix (controls perspective)
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		// fovy, aspect ratio, zNear, zFar
		GLU.gluPerspective(30f, aspectRatio, 1f, 100f);
		// return to modelview matrix
		GL11.glMatrixMode(GL11.GL_MODELVIEW);		


		// no overall scene lighting
		setAmbientLight(new float[] { 10f, 10f, 10f, 0f });

		// enable lighting and texture rendering
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		// select model view for subsequent transforms
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

	}

	/**
	 * Render one frame.  Called by GLApp.run().
	 */
	public void draw() {
		if(Hub.renderer!=null){
			Hub.renderer.display();
		}

	}

	@Override
	public void mouseWheel(int amount) {
		Hub.handler.handleEvent(new WheelEvent(amount));
	}

	public void mouseMove(int x, int y) {
		Hub.handler.handleEvent(new HoverEvent(x/Hub.width,y/Hub.height));
	}

	public void dragLeftDown(int x, int y) {
		if(drag){
			Hub.handler.handleEvent(new ClickEvent(x/Hub.width,y/Hub.height,ClickEvent.ACTION_DOWN,ClickEvent.MOUSE_LEFT));
		}
	}

	public void mouseLeftDown(int x, int y) {
		Hub.handler.handleEvent(new ClickEvent(x/Hub.width,y/Hub.height,ClickEvent.ACTION_DOWN,ClickEvent.MOUSE_LEFT));

	}

	public void mouseLeftUp(int x, int y) {
		Hub.handler.handleEvent(new ClickEvent(x/Hub.width,y/Hub.height,ClickEvent.ACTION_UP,ClickEvent.MOUSE_LEFT));

	}

	public void dragRightDown(int x, int y) {
		if(drag){
			Hub.handler.handleEvent(new ClickEvent(x/Hub.width,y/Hub.height,ClickEvent.ACTION_DOWN,ClickEvent.MOUSE_RIGHT));
		}
	}
	public void mouseRightDown(int x, int y) {
		Hub.handler.handleEvent(new ClickEvent(x/Hub.width,y/Hub.height,ClickEvent.ACTION_DOWN,ClickEvent.MOUSE_RIGHT));

	}

	public void mouseRightUp(int x, int y) {
		Hub.handler.handleEvent(new ClickEvent(x/Hub.width,y/Hub.height,ClickEvent.ACTION_UP,ClickEvent.MOUSE_RIGHT));

	}
	@Override
	public void keyDown(char c, int keycode) {
		Hub.handler.handleEvent(new KeyBoardEvent(KeyBoardEvent.KEY_DOWN,c,keycode));
	}
	@Override
	public void keyUp(int keycode) {
		Hub.handler.handleEvent(new KeyBoardEvent(KeyBoardEvent.KEY_UP,' ',keycode));
	}

	@Override
	public void end(){
		super.cleanup();
	}
	@Override
	public void cleanup(){
		Hub.cleanup();
	}


	public void setView(GraphicView view) {
		if(Hub.view!=null){
			Hub.view.onRemoveFromDrawable();
		}
		Hub.handler.clear();
		Hub.view = view;
		Hub.handler.giveOnClick(view);
		KeyBoardListener keyListener = view.getDefaultKeyBoardListener();
		if(keyListener!=null){
			Hub.handler.giveOnType(keyListener);
		}
		view.onAddToDrawable();
	}


	public void disableDrag() {
		drag = false;
	}

	public void enableDrag(){
		drag = true;
	}

	public void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void setFinished(boolean finished) {
		GLApp.finished = finished;
	}


	public int createTexture(@SuppressWarnings("rawtypes") Resource resource){
		if(resource.type()==Resource.INPUT_STREAM){
			return GLApp.makeTexture((InputStream)resource.get());
		}
		else if(resource.type()==Resource.FILE){
			return GLApp.makeTexture(resource.getPath());
		}
		else return -1;
	}

	public int createTexture(BufferedImage textureImg){
		GLImage image = new GLImage(textureImg);
		return GLApp.makeTexture(image);
	}
	
	public float getDisplayWidth(){
		return displayWidth;
	}
	public float getDisplayHeight(){
		return displayHeight;
	}


	@Override
	public void showKeyBoardDisplay(KeyBoardListener listener) {
	}


	@Override
	public void hideKeyBoardDisplay(KeyBoardListener listener) {
	}


	@Override
	public boolean isKeyBoardShowing() {
		return true;
	}

}
