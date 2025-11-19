package SwingTest;

import java.awt.Color;

public class MyPoint{
	
	int x = 0;
	int y = 0;
	Color pointColor;
	int width = 0;
	
	MyPoint(){
	}
	
	MyPoint(int x, int y){
    	this.x = x;
    	this.y = y;
	}
	MyPoint(MyPoint other){
		this.x = other.x;
		this.y = other.y;
        this.pointColor = other.pointColor;
        this.width = other.width;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public double getDist(MyPoint other) {
		return Math.sqrt((x - other.x)*(x - other.x) + (y - other.y)*(y - other.y));
	}
	
	public void setColor(Color c) {
		this.pointColor = c;
	}
	
	public void setWidth(int w) {
		this.width = w;
	}
}
