package com.rem.otl.editor.program;

import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.otl.editor.Button;
import com.rem.otl.editor.ButtonAction;
import com.rem.otl.editor.field.FieldEditor;
import com.rem.otl.editor.field.FloatFieldComponent;
import com.rem.otl.editor.field.OnClickFieldComponent;
import com.rem.otl.editor.field.StringFieldComponent;
import com.rem.otl.editor.field.TextFieldComponent;
import com.rem.otl.game.environment.program.action.ProgramAction;

public abstract class ActionEditor extends Button implements DataRetriever, Cloneable{
	@SuppressWarnings("rawtypes")
	protected ProgramAction action;
	private StateSquare state = null;

	private GraphicEntity arrow;
	protected ProgramSquareEditor editor;
	private ArrowButton parentArrow = null;

	private FieldEditor<Settable> valueEditor;
	protected Settable target;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ActionEditor(ProgramSquareEditor editor,
			String textureName, Integer frame, 
			final ProgramAction action, Settable initialTarget) {
		super(textureName, frame,
				" - "+ProgramAction.actionNames.get(action.getIndex())+" action", null,null);
		this.editor = editor;
		final ActionEditor self = this;
		this.action = action;
		this.arrow = new GraphicEntity("editor_arrows",Hub.MID_LAYER);
		this.arrow.setFrame(0);
		addChild(arrow);
		this.target = initialTarget;
		valueEditor =  new FieldEditor<Settable>("",
				new TextFieldComponent[]{},
				new OnClickFieldComponent[]{});
		valueEditor.setVisible(false);
		addChild(valueEditor);
		setOnClick(new ButtonAction(){

			@Override
			public void act(ClickEvent event) {
				self.retrieveData();
			}
		});

		if(initialTarget!=null){
			setOnRelease(new ButtonAction(){
				@Override
				public void act(ClickEvent event){
					//Summon UpdateStyle updateField
					valueEditor.clearOnClicks();
					valueEditor.clearOnTypes();
					valueEditor.changeText("");

					StringBuilder text = new StringBuilder();
					for(int i=0;i<target.copiableValueIds().length;++i){
						final int valueId = target.copiableValueIds()[i];
						text.append(target.copiableValueNames()[i]);
						text.append(":\n");
						if(target.getValueType(valueId) == Settable.FLOAT){
							valueEditor.addOnType(new FloatFieldComponent<Settable>("impact"){
								@Override
								public void act(Float subject) {
									target.setValue(valueId,subject);
								}

								@Override
								public Settable updateWith(Settable subject) {
									changeTextOnLine(""+subject.getValue(valueId), 0);
									return subject;
								}});
						}
						else if(target.getValueType(valueId) == Settable.STRING){
							valueEditor.addOnType(new StringFieldComponent<Settable>("impact"){
								@Override
								public void act(String subject) {
									target.setValue(valueId,subject);
								}

								@Override
								public Settable updateWith(Settable subject) {
									changeTextOnLine(""+subject.getStringValue(valueId), 0);
									return subject;
								}});
						}
					}
					for(int i=0;i<target.copiableIntIds().length;++i){
						final int intId = target.copiableIntIds()[i];
						valueEditor.addOnClick(new OnClickFieldComponent<Settable>(
								target.copiableIntTextureNames()[i],
								target.copiableIntTextureRanges()[i*2],target.copiableIntTextureRanges()[i*2+1]){
							@Override
							public void act(Integer subject) {
								target.setValue(intId,(int)subject);
							}

							@Override
							public void updateWith(Settable subject) {
								setFrame(subject.getInt(intId));
							}});
					}

					valueEditor.changeText(text.toString());

					valueEditor.updateWith(target);
					valueEditor.setVisible(true);
					valueEditor.resize(0.3f, 0f);
					valueEditor.reposition(self.getX(), self.getY()+self.getHeight());
					Hub.handler.giveOnClick(valueEditor);
					Hub.handler.giveOnType(valueEditor.getDefaultKeyBoardListener());

				}
			});
		}
	}

	@Override
	public boolean onClick(ClickEvent e){
		if(ClickEvent.ACTION_UP==e.getAction()){
			if(state==null){
				reposition(e.getX()-getWidth()/2f,
						e.getY()-getHeight()/2f);
				editor.removeTransitioningActionEditor(this);
				this.state = editor.getStateRoot().addActionEditor(this);
				Hub.handler.removeOnClick(this);
			}
			if(parentArrow!=null){
				parentArrow.free();
			}
		}
		return true;
	}

	public boolean onHover(ClickEvent e){
		reposition(e.getX()-getWidth()/2f,
				e.getY()-getHeight()/2f);
		return true;
	}
	@Override
	public float offsetX(int index){
		if(index>1){
			return -getChild(index).getWidth()-0.005f;
		}
		else {
			return super.offsetX(index);
		}
	}
	@Override
	public void resize(float dx, float dy){
		super.resize(dx,dy);
		arrow.resize(dx*2f,dy);
	}

	public void setParentArrowButton(ArrowButton arrow) {
		this.parentArrow = arrow;
	}

	@SuppressWarnings("rawtypes")
	public ProgramAction getAction() {
		return action;
	}


}
