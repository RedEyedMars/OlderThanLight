package com.rem.otl.editor.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicLine;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.otl.editor.Button;
import com.rem.otl.editor.ButtonAction;
import com.rem.otl.editor.field.FieldEditor;
import com.rem.otl.editor.field.FloatFieldComponent;
import com.rem.otl.editor.field.IntegerFieldComponent;
import com.rem.otl.editor.field.OnClickFieldComponent;
import com.rem.otl.editor.field.StringFieldComponent;
import com.rem.otl.editor.field.TextFieldComponent;
import com.rem.otl.game.environment.program.ProgramState;
import com.rem.otl.game.environment.program.condition.ProgramCondition;

@SuppressWarnings("rawtypes")
public class ConditionArrow extends GraphicLine{
	private static Map<String,Integer> eventNames = new HashMap<String,Integer>();
	static {
		int i=0;
		for(String event:ProgramState.eventNames){
			eventNames.put(event,i++);
		}
	}
	private ProgramCondition condition;
	private String event;

	private FieldEditor<ProgramCondition> conditionEditor;
	private Button openFieldsButton;

	@SuppressWarnings("unchecked")
	public ConditionArrow(ProgramSquareEditor editor,ProgramCondition cond, String eve) {
		super(1);
		this.condition = cond;
		this.event = eve;
		this.openFieldsButton = new Button("editor_arrows",0,"Edit this condition",null,
				new ButtonAction(){
			@Override
			public void act(ClickEvent event) {
				conditionEditor.changeText("");
				conditionEditor.clearOnTypes();
				conditionEditor.clearOnClicks();
				setupConditionEditor();
				conditionEditor.updateWith(condition);
				conditionEditor.setVisible(true);
				conditionEditor.resize(0.3f, 0f);
				conditionEditor.reposition(openFieldsButton.getX()+openFieldsButton.getWidth(),
						openFieldsButton.getY()+openFieldsButton.getHeight());
				Hub.handler.giveOnClick(conditionEditor);
				Hub.handler.giveOnType(conditionEditor.getDefaultKeyBoardListener());
			}
		});
		conditionEditor = new FieldEditor<ProgramCondition>("",
				new TextFieldComponent[]{},
				new OnClickFieldComponent[]{});
		setupConditionEditor();
		conditionEditor.setVisible(false);
		addChild(conditionEditor);
		addChild(openFieldsButton);
		editor.addButton(openFieldsButton);
	}

	public void setupConditionEditor(){
		StringFieldComponent<ProgramCondition> baseCondition = new StringFieldComponent<ProgramCondition>("impact") {
			private List<TextFieldComponent<ProgramCondition,?>> args = new ArrayList<TextFieldComponent<ProgramCondition,?>>();
			private ProgramCondition myCondition;
			@SuppressWarnings("unchecked")
			@Override
			public void act(String subject) {
				boolean replaceMainCondition = false;
				ProgramState state = condition.getState();
				ProgramCondition nextCondition = myCondition.getNext();
				if(myCondition==condition){
					replaceMainCondition = true;
				}
				myCondition = ProgramCondition.getCondition(subject).create();
				myCondition.setState(state);
				myCondition.setNext(nextCondition);
				if(replaceMainCondition){
					condition = myCondition;
				}
			}
			@Override
			public ProgramCondition updateWith(ProgramCondition subject) {
				this.args.clear();
				StringBuilder indentation = new StringBuilder();
				myCondition = subject;
				for(final String dataName:subject.getDataKeys()){
					indentation.append("\n   ");
					TextFieldComponent<ProgramCondition,?> arg = null;
					if(myCondition.getData(dataName) instanceof String){
						arg = new StringFieldComponent<ProgramCondition>("impact"){
							@Override
							public void act(String subject) {
								myCondition.setData(dataName, subject);
							}

							@Override
							public ProgramCondition updateWith(ProgramCondition subject) {
								return subject;
							}
						};
					}
					else if(myCondition.getData(dataName) instanceof Integer){
						arg = new IntegerFieldComponent<ProgramCondition>("impact"){
							@Override
							public void act(Integer subject) {
								myCondition.setData(dataName, subject);
							}

							@Override
							public ProgramCondition updateWith(ProgramCondition subject) {
								return subject;
							}
						};
					}
					else if(myCondition.getData(dataName) instanceof Float){
						arg = new FloatFieldComponent<ProgramCondition>("impact"){
							@Override
							public void act(Float subject) {
								myCondition.setData(dataName, subject);
							}

							@Override
							public ProgramCondition updateWith(ProgramCondition subject) {
								return subject;
							}
						};
					}
					args.add(arg);
					conditionEditor.addOnType(arg);
				}
				changeTextOnLine(subject.getName(), 0);
				condition = subject;
				return subject.getNext();
			}
		};
		OnClickFieldComponent<ProgramCondition> eventClicker = new OnClickFieldComponent<ProgramCondition>("editor_program_events",0,4){
			@Override
			public void act(Integer subject) {
				event = ProgramState.eventNames[subject];
			}

			@Override
			public void updateWith(ProgramCondition subject) {
				setFrame(eventNames.get(event));
			}
		};
		conditionEditor.addOnClick(eventClicker);
		conditionEditor.addOnType(baseCondition);
	}

	public ProgramCondition getCondition() {
		return condition;
	}

	public String getEvent() {
		return event;
	}
	@Override
	public void resize(float w, float h){
		super.resize(w, h);
		if(openFieldsButton!=null){
			openFieldsButton.resize(0.025f, 0.025f);
		}
	}
	@Override
	public  float offsetX(int index){
		if(getChild(index)==openFieldsButton){
			return getWidth()-0.0125f;
		}
		else if(getChild(index)==conditionEditor){
			return openFieldsButton.getX()+openFieldsButton.getWidth()-getX();
		}
		return super.offsetX(index);
	}
	@Override
	public  float offsetY(int index){
		if(getChild(index)==openFieldsButton){
			return getHeight()-0.0125f;
		}
		else if(getChild(index)==conditionEditor){
			return openFieldsButton.getY()+openFieldsButton.getHeight()-getY();
		}
		return super.offsetY(index);
	}
}
