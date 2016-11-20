package com.rem.otl.editor.field;

import com.rem.core.Action;
import com.rem.core.Hub;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.otl.editor.TextWriter;

public abstract class TextFieldComponent<TargetType extends Object,SubjectType extends Object> extends TextWriter implements Action<SubjectType>{

	protected TextFieldComponent<TargetType,?> next=null;
	protected TargetType target;
	protected FieldEditor<TargetType> parentField;
	public TextFieldComponent(String font) {
		super(font, " ");	
		charIndex=0;
		index=0;
	}

	public void setNext(TextFieldComponent<TargetType,?> next) {
		this.next = next;
	}
	public void setTarget(TargetType target) {
		this.target=target;
	}
	public void setParent(FieldEditor<TargetType> parentField) {
		this.parentField = parentField;
	}
	@Override
	public void onType(KeyBoardEvent event){
		if(legalKey(event.getChar(),event.getKeyCode())){
			super.onType(event);
		}
		else if(event.keyUp()&&(event.is(15)||event.is(28))){
			advance(this.getText());
			Hub.handler.removeOnType(this);
			if(next!=null){
				Hub.handler.giveOnType(next);
				this.parentField.nextType();
			}
			else {
				Hub.handler.removeOnClick(parentField);
				parentField.setVisible(false);
			}
		}
	}
	protected abstract boolean legalKey(char c, int keycode);
	protected abstract void advance(String text);
	public abstract TargetType updateWith(TargetType subject);
	public void updateChain(TargetType subject){
		setTarget(subject);
		TargetType forNext = updateWith(subject);
		if(next!=null){
			next.updateChain(forNext);
		}
	}

}
