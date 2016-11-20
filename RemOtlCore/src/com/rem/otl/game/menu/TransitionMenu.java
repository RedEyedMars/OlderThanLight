package com.rem.otl.game.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rem.core.gui.graphics.MenuButton;
import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.graphics.GraphicText;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.duo.client.Client;
import com.rem.duo.messages.PassMessage;
import com.rem.otl.duo.messages.SaveGameMessage;
import com.rem.otl.duo.messages.StartGameMessage;
import com.rem.otl.editor.TextWriter;
import com.rem.otl.game.Game;
import com.rem.core.gui.inputs.ClickEvent;

public class TransitionMenu extends Menu{
	private TextWriter playerName;
	private boolean isBest;
	private boolean isWinner;
	private boolean isCompetitive;
	private int minutes;
	private int seconds;
	private int millis;
	private boolean canProceed;
	private MenuButton winnerButton;
	private GraphicEntity returnButton;
	private Game game;
	public TransitionMenu(
			Game game, boolean isCompetitive,boolean isWinner,long millisecondsToComplete,
			final String previousMapName, final String nextMapName, final boolean myColour, boolean canProceed){
		super();
		this.game = game;
		this.isCompetitive = isCompetitive;
		this.isWinner = isWinner;
		this.minutes = (int) (millisecondsToComplete/1000/60);
		this.seconds = (int) ((millisecondsToComplete/1000)%60);
		this.millis = (int) ((millisecondsToComplete)%1000);
		this.canProceed = canProceed;
		//if(!competitive){
		final File saveFile = new File("data"+File.separator+"save.data");
		String lastName = Hub.defaultPlayerName;
		long bestWin = Long.MAX_VALUE;
		long bestFail = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(saveFile));
			String line = reader.readLine();
			while(line!=null){
				int firstTab = line.indexOf('\t');
				if(firstTab==-1){
					line=reader.readLine();
					continue;
				}
				else if(firstTab==0){
					lastName="";
				}
				else {
					lastName=line.substring(0, firstTab);
				}
				if(previousMapName.equals(
						line.substring(firstTab+1,line.indexOf('\t',firstTab+1)))){
					String[] split = line.split("\\tIN\\t");
					if(split!=null&&split.length>1){
						Long millis = Long.parseLong(split[1].substring(0, split[1].indexOf("ms")));
						if(line.contains("\tW\t")){
							if(millis<bestWin){
								bestWin=millis;
							}
						}
						else if(line.contains("\tL\t")){
							if(millis>bestFail){
								bestFail=millis;
							}
						}
					}
				}
				line=reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		winnerButton = new MenuButton(isCompetitive?isWinner?"Victory!":"Defeat":isWinner?"Completed!":"Failed");
		winnerButton.reposition(0.2f,0.83f);
		addChild(winnerButton);
		boolean won = bestWin!=Long.MAX_VALUE;
		isBest = false;
		if((won&&millisecondsToComplete<=bestWin&&isWinner)||
				(!won&&(isWinner||millisecondsToComplete>=bestFail))){
			isBest = true;			
			playerName = new TextWriter("impact",lastName){
				{
					setFontSize(GraphicText.FONT_SIZE_LARGE);
					resize(getWidth(), getHeight());
					charIndex=text.length();
					index=text.length();
				}
				@Override
				public void onType(KeyBoardEvent event){
					if(event.is(KeyBoardEvent.BACKSPACE)){
						super.onType(event);
					}
					else if(event.getChar()>=32){
						if(lines.get(0).size()==0||
								(lines.get(0).getChild(lines.get(0).size()-1).getX()<0.9f-0.1f)){
							super.onType(event);
						}
					}
				}
			};
			GraphicEntity nameButton = new MenuButton("Name:"){
				private boolean listening = false;
				{
					text.setJustified(GraphicText.LEFT_JUSTIFIED);
				}
				@Override
				public void performOnRelease(ClickEvent e){
					playerName.change("");
					if(listening){
						Hub.handler.removeOnType(playerName);
					}
					else {
						Hub.handler.giveOnType(playerName);
					}
					listening = !listening;
				}				
			};
			nameButton.resize(0.8f, 0.15f);
			nameButton.reposition(0.1f,0.67f);


			playerName.reposition(0.325f,0.69f);
			addChild(nameButton);
			addChild(playerName);

		}
		//}
		GraphicEntity time = new MenuButton((minutes>0?(minutes+"m "):" ")+seconds+"s"){
			@Override
			public void performOnRelease(ClickEvent e){
				Hub.creator.copyToClipboard(this.getText());
			}
		};
		time.reposition(0.2f,0.51f);
		addChild(time);

		returnButton = new MenuButton("Return to Main Menu"){
			@Override
			public void performOnRelease(ClickEvent e){
				if(Client.isConnected()){
					Client.pass(new PassMessage(
							new SaveGameMessage(previousMapName,minutes,seconds)));
				}
				saveTime(previousMapName);
				returnToMain();
			}
		};
		returnButton.reposition(0.2f,0.19f);
		addChild(returnButton);
		if(isBest){
			GraphicEntity bestIndicator = new GraphicEntity("menu_best_indicator",Hub.MID_LAYER);
			bestIndicator.resize(0.075f, 0.075f);
			bestIndicator.reposition(0.6f,0.56f);
			addChild(bestIndicator);
		}
		else {
			winnerButton.reposition(winnerButton.getX(),0.67f);
		}
	}

	public void saveTime(String previousMapName){
		if(isBest){
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data"+File.separator+"save.data"),true));
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				writer.write("\n"+playerName.getText()+"\t"+previousMapName+(isWinner?"\tW\t":"\tL\t")+"on "+dateFormat.format(date)+"\tIN\t"+(((minutes*60)+seconds)*1000+millis)+"ms");
				writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(playerName!=null){
			Hub.defaultPlayerName = playerName.getText();
		}
	}
	private void returnToMain() {
		Client.endConnectionToTheServer();
		Hub.gui.setView(game.getParentView());
	}
	@Override
	public void end() {
		returnButton.performOnRelease(null);
	}
	public void verifyWhoWon(boolean successful, long theirTime) {
		if(!isCompetitive){
			if(isWinner&&!successful){
				isWinner=false;
			}
		}
		else {
			if(isWinner&&successful){
				long myTime = this.millis+1000*(this.seconds+60*this.minutes);
				if(myTime==theirTime){
					winnerButton.changeText("Tie");
					return;
				}
				else if(myTime>theirTime){
					isWinner = false;
				}
			}
		}
		winnerButton.changeText(isCompetitive?isWinner?"Victory!":"Defeat":isWinner?"Completed!":"Failed");
	}
	public void canProceed(final String previousMapName,String nextMapName,final boolean myColour){
		if(canProceed){
			GraphicEntity proceedButton = new MenuButton(nextMapName){
				@Override
				public void performOnRelease(ClickEvent e){
					long seed = Hub.getNewRandomSeed();
					if(Client.isConnected()){
						Client.pass(new SaveGameMessage(previousMapName,minutes,seconds));						
						Client.pass(new StartGameMessage(!myColour,Hub.seed, 0L));
					}
					saveTime(previousMapName);
					Game newGame = new Game(myColour,seed,game.getParentView());
					Hub.gui.setView(newGame);
				}
			};
			proceedButton.reposition(0.2f,0.35f);
			addChild(proceedButton);
		}
	}

	public KeyBoardListener getDefaultKeyBoardListener(){
		return null;
	}

	public int getMinutes() {
		return minutes;
	}
	public int getSeconds() {
		return seconds;
	}
	public int getMillis() {
		return millis;
	}
	
}
