package edu.eom;

import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class Vertex {

	private int X;
	private int Y;
	private double[] possiblePaths;
	private Circle vertexCircle;
	private int vertexNumber;
	
	public Vertex(int x, int y, int size, Pane pane,  Color value, double[] paths, int number) {
		this.X = x;
		this.Y = y;
		this.possiblePaths = paths;
		this.vertexNumber = number;
		vertexCircle = new Circle(x, y, size);
		vertexCircle.setFill(value);
		pane.getChildren().add(vertexCircle);
		
	}
	
	public void drawPath(Vertex dest, Pane pane, boolean reverse) {
		
		if(possiblePaths[dest.getVertexNumber()-1] > 0.0) {
			double cp1x = (dest.getX() + X)/2;
			double cp2x = (dest.getX() + X)/2;
			double cp1y = (dest.getY() + Y)/2;
			double cp2y = (dest.getY() + Y)/2;
			
			CubicCurve path = new CubicCurve(X, Y, cp1x, cp1y, cp2x, cp2y, dest.getX(), dest.getY());
			
			double size = Math.max(path.getBoundsInLocal().getWidth(), path.getBoundsInLocal().getHeight());
			double scale=size/15d;	
			if(reverse) {
				cp1x = (dest.getX() + X)/2 - scale;
				cp1y = (dest.getY() + Y)/2 - scale;
				cp2x = (dest.getX() + X)/2 - scale;
				cp2y = (dest.getY() + Y)/2 - scale;		
			}
			else {
				cp1x = (dest.getX() + X)/2 + scale;
				cp1y = (dest.getY() + Y)/2 + scale;
				cp2x = (dest.getX() + X)/2 + scale;
				cp2y = (dest.getY() + Y)/2 + scale;				
			}
			
			path = new CubicCurve(X, Y, cp1x, cp1y, cp2x, cp2y, dest.getX(), dest.getY());
			
			path.setStrokeWidth(1);
			path.setStroke(Color.BLACK);
	        path.setFill(null);
	        Circle cp1 = new Circle(cp1x, cp1y, 3);
	        cp1.setStroke(Color.BLACK);
	        Circle cp2 = new Circle(cp2x, cp2y, 3);
	        cp2.setStroke(Color.BLACK);
	        connect(getCircle(), dest.getCircle(), path);
            pane.getChildren().addAll(path, addArrow(path, pane));//, cp1, cp2);           
		}		
	}
	
	public Path addArrow(CubicCurve curve1, Pane pane) {
		
		double scale=30;//size/12d;

		Point2D ori=eval(curve1,0);
		Point2D tan=evalDt(curve1,0).normalize().multiply(scale);
		
		ori=eval(curve1,1);
		tan=evalDt(curve1,1).normalize().multiply(scale);
		Path arrowEnd=new Path();
		arrowEnd.getElements().add(new MoveTo(ori.getX()-0.2*tan.getX()-0.2*tan.getY(),
		                               ori.getY()-0.2*tan.getY()+0.2*tan.getX()));
		arrowEnd.getElements().add(new LineTo(ori.getX(), ori.getY()));
		arrowEnd.getElements().add(new LineTo(ori.getX()-0.2*tan.getX()+0.2*tan.getY(),
		                               ori.getY()-0.2*tan.getY()-0.2*tan.getX()));

		return arrowEnd;
	}
	
	public double[] getPossiblePaths() {
		return possiblePaths;
	}
	public int getVertexNumber() {
		return vertexNumber;
	}
	
	public int getX() {
		return X;
	}
	
	public int getY() {
		return Y;
	}
	
	public Circle getCircle() {
		return vertexCircle;
	}
	
	public static Point2D getDirection(Circle c1, Circle c2) {
	    return new Point2D(c2.getCenterX() - c1.getCenterX(), c2.getCenterY() - c1.getCenterY()).normalize();
	}

	public static void connect(Circle c1, Circle c2, CubicCurve line) {
	    InvalidationListener startInvalidated = observable -> {
	        Point2D dir = getDirection(c1, c2);
	        Point2D diff = dir.multiply(c1.getRadius());
	        line.setStartX(c1.getCenterX() + diff.getX());
	        line.setStartY(c1.getCenterY() + diff.getY());
	    };
	    InvalidationListener endInvalidated = observable -> {
	        Point2D dir = getDirection(c2, c1);
	        Point2D diff = dir.multiply(c2.getRadius());
	        line.setEndX(c2.getCenterX() + diff.getX());
	        line.setEndY(c2.getCenterY() + diff.getY());
	    };
	    c1.centerXProperty().addListener(startInvalidated);
	    c1.centerYProperty().addListener(startInvalidated);
	    c1.radiusProperty().addListener(startInvalidated);

	    startInvalidated.invalidated(null);

	    c2.centerXProperty().addListener(endInvalidated);
	    c2.centerYProperty().addListener(endInvalidated);
	    c2.radiusProperty().addListener(endInvalidated);

	    endInvalidated.invalidated(null);
	}
	
	private Point2D eval(CubicCurve c, float t){
	    Point2D p=new Point2D(Math.pow(1-t,3)*c.getStartX()+
	            3*t*Math.pow(1-t,2)*c.getControlX1()+
	            3*(1-t)*t*t*c.getControlX2()+
	            Math.pow(t, 3)*c.getEndX(),
	            Math.pow(1-t,3)*c.getStartY()+
	            3*t*Math.pow(1-t, 2)*c.getControlY1()+
	            3*(1-t)*t*t*c.getControlY2()+
	            Math.pow(t, 3)*c.getEndY());
	    return p;
	}

	private Point2D evalDt(CubicCurve c, float t){
	    Point2D p=new Point2D(-3*Math.pow(1-t,2)*c.getStartX()+
	            3*(Math.pow(1-t, 2)-2*t*(1-t))*c.getControlX1()+
	            3*((1-t)*2*t-t*t)*c.getControlX2()+
	            3*Math.pow(t, 2)*c.getEndX(),
	            -3*Math.pow(1-t,2)*c.getStartY()+
	            3*(Math.pow(1-t, 2)-2*t*(1-t))*c.getControlY1()+
	            3*((1-t)*2*t-t*t)*c.getControlY2()+
	            3*Math.pow(t, 2)*c.getEndY());
	    return p;
	}
	
}
