package com.rem.otl.game.environment.program.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rem.otl.game.environment.SquareAction;
import com.rem.otl.game.environment.program.DataHolder;
import com.rem.otl.game.environment.program.ProgramState;

public abstract class ProgramAction <SubjectType extends Object> extends DataHolder implements SquareAction<SubjectType, ProgramState>{

	@SuppressWarnings("rawtypes")
	private static List<ProgramAction> actions = new ArrayList<ProgramAction>();
	public static List<String> actionNames = new ArrayList<String>();
	
	public static final BaseProgramAction base = new BaseProgramAction();
	public static final SetColourProgramAction set_colour = new SetColourProgramAction();
	public static final SetUpdateActionProgramAction set_update_action = new SetUpdateActionProgramAction();
	public static final DefineVariableProgramAction define_variable = new DefineVariableProgramAction();
	public static final IncrementVariableProgramAction increment_variable = new IncrementVariableProgramAction();
	public static final DisplayImageProgramAction display_image = new DisplayImageProgramAction();
	
	private ProgramState state;
	
	@Override
	public void saveTo(List<Object> saveTo) {
		saveTo.add(getIndex());
		super.saveTo(saveTo);
	}
	@Override
	public int saveType() {
		return 7;
	}
	@Override
	public void setTarget(ProgramState target) {
		this.state = target;
	}
	@Override
	public ProgramState getState() {
		return state;
	}
	@Override
	public void setState(ProgramState state) {
		this.state = state;
	}
	public abstract ProgramAction<?> create();
	static {
		try {
			Map<Integer,ProgramAction<?>> osas = new HashMap<Integer,ProgramAction<?>>();
			Map<Integer,String> names = new HashMap<Integer,String>();
			
			for(Field field:ProgramAction.class.getFields()){
				Object obj = field.get(ProgramAction.class);
				if(obj instanceof ProgramAction){
					//System.out.println(field.getName());
					ProgramAction<?> osa = (ProgramAction<?>) obj;
					osas.put(osa.getIndex(),osa);
					names.put(osa.getIndex(),field.getName().replace('_', ' '));
				}
			}
			for(int i=0;i<osas.size();++i){
				actions.add(osas.get(i));
				actionNames.add(names.get(i));
			}
		}
		catch (IllegalArgumentException e){			
			e.printStackTrace();
		}
		catch  (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	public static ProgramAction<?> getAction(Integer i) {
		//System.out.println(i);
		if(i==-1||i>=actions.size()){
			return null;
		}
		else {
			return actions.get(i);
		}
	}
}
