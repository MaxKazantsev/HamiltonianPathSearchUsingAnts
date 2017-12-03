package edu.eom;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class SceneController  implements Initializable{

	@FXML
	private Button calculateButton;
	@FXML
	private TextArea logArea;
	@FXML
	private Pane graphPane;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private ScrollBar scrollBar;
	@FXML
	private TextField antsNumTF;
	
	private int antsNum;
	private int vertNum;
	private Vertex[] graphVertices;
	private double[][] defaultPaths;
	private double[][] possiblePaths;
	private Random rnd = new Random();
	
	@FXML
	private void calculateFileButtonHandler(ActionEvent event) throws FileNotFoundException {
		
	
		if(antsNumTF.getText().trim().isEmpty()) {
			logArea.appendText("\nNot all field are filled.\n");
			return;
		}
	
		if(!antsNumTF.getText().matches("[-+]?\\d+")) {
			logArea.appendText("\nWrong format.\n");
			return;
		}
		
		antsNum = Integer.parseInt(antsNumTF.getText());
		
		
		ACO(antsNum);
	}
	
	private void ACO(int antsNumber) {
		
		double pheromone = 0.1;
		double pherVolat = 0.0;

		possiblePaths = defaultPaths;
		ArrayList<Vertex> bestPath = new ArrayList<Vertex>();
		for(int a = 0; a < antsNumber; a++) {
			bestPath = pathFinder(pheromone, pherVolat);
			if(bestPath != null) {
				logArea.appendText("Ïóòü");
				for(int i = 0; i < bestPath.size(); i++) 
					logArea.appendText(" " + bestPath.get(i).getVertexNumber());
				logArea.appendText(" found by ant " + a + ".\n");
				
				graphPane.getChildren().clear();
				initDefaultGraph();
				drawPath(bestPath);
				break;
			}
			
		}
		
	}
	
	private ArrayList<Vertex> pathFinder(double p, double pv) {
		
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		ArrayList<Vertex> candidates = new ArrayList<Vertex>();
		
		boolean[] flags = new boolean[vertNum];
		
		for(int i = 0; i < vertNum; i++)
			flags[i] = false;
		
		boolean found;
		
		int curVertex = 0;
		path.add(graphVertices[0]);
		for(int i = 1; i < vertNum; i++) {
				
			found = false;
			if(!whereToGo(possiblePaths[curVertex], flags)) {
				return null;
			}
			while(!found) {
				
				double rate = rnd.nextDouble();
				for(int j = 0; j < vertNum; j++) {			
					if(rate < possiblePaths[curVertex][j] && !flags[j] && possiblePaths[curVertex][j] > 0) {						
						candidates.add(graphVertices[j]); 						
					}					
				}						
				if(candidates.size() != 0) {
					int curWinner = randInt(0, candidates.size()-1);
					path.add(candidates.get(curWinner));
					possiblePaths[curVertex][candidates.get(curWinner).getVertexNumber()-1] += p * i;// adding pheromone 
					curVertex = candidates.get(curWinner).getVertexNumber()-1;					
					flags[curVertex] = true;
					found = true;
					candidates.clear();				
				}				
			}
			
			for(int row = 0; row < vertNum; row++)
				for(int col = 0; col < vertNum; col++) {
					if(possiblePaths[row][col] > 0)
						possiblePaths[row][col] -= pv;// evaporation of pheromone
				}
		}
		
		return path;
	}
	
	private void drawPath(ArrayList<Vertex> path) {
		
		boolean revers = false;
		for(int i = 0; i < path.size()-1; i++) {			
				if(i%2 == 0)
					revers = false;
				else 
					revers = true;
				path.get(i).drawPath(path.get(i+1), graphPane, revers);
		}	
	}
	
	private boolean whereToGo(double[] possiblePaths, boolean[] flags) {
				
		for(int i = 0; i < vertNum; i++) {
			if(possiblePaths[i] > 0 && !flags[i])
				return true;	
		}		
		return false;
	}
	
	private void initDefaultGraph() {
			
		graphVertices = new Vertex[7];
		defaultPaths = new double[][]{
			//1    2     3   4    5    6    7
			{0.0, 0.3, 0.0, 0.3, 0.0, 0.0, 0.3},//1
			{0.0, 0.0, 0.5, 0.0, 0.5, 0.0, 0.0},//2
			{0.0, 0.0, 0.0, 0.5, 0.0, 0.5, 0.0},//3
			{0.0, 0.5, 0.0, 0.0, 0.5, 0.0, 0.0},//4
			{0.0, 0.5, 0.0, 0.5, 0.0, 0.0, 0.0},//5
			{0.0, 0.0, 0.0, 0.3, 0.3, 0.0, 0.3},//6
			{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0} //7
			};
		
		int standartSize = 10;
		int borderSize = 12;
		int scale = 20;
		graphVertices[0] = new Vertex(100, 350, borderSize, graphPane, Color.GREEN, defaultPaths[0], 1);// 1 START
				
		graphVertices[1] = new Vertex(200, 100, standartSize + scale, graphPane, Color.BLUE, defaultPaths[1], 2);// 2
				
		graphVertices[2] = new Vertex(600, 50, standartSize, graphPane, Color.BLUE, defaultPaths[2], 3);// 3
		
		graphVertices[3] = new Vertex(1000, 100, standartSize + scale, graphPane, Color.BLUE, defaultPaths[3], 4);// 4
		
		graphVertices[4] = new Vertex(250, 600, standartSize + scale, graphPane, Color.BLUE, defaultPaths[4], 5);// 5
				
		graphVertices[5] = new Vertex(950, 600, standartSize, graphPane, Color.BLUE, defaultPaths[5], 6);// 6
				
		graphVertices[6] = new Vertex(1100, 350, borderSize, graphPane, Color.RED, defaultPaths[6], 7);// 7 END
		
		vertNum = graphVertices.length;
	}
	
	private int randInt(int min, int max) {

	    int randomNum = rnd.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		antsNumTF.setText("50");
		initDefaultGraph();
	}

}
