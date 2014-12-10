package org.opencv.samples.imagemanipulations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Point;

public class Marker {
	private List<Point> innerBorder;
	private List<Point> outerBorder;
	private Point center;
	
	public Marker(List<Point> innerBorder, List<Point> outerBorder) {

		if(!isClockwise(outerBorder)){
			Collections.reverse(innerBorder);
			Collections.reverse(outerBorder);
		}
		
		this.innerBorder = innerBorder;
		this.outerBorder = outerBorder;
		
		center = findCenter();
		int i = indexOfClosestPoint(innerBorder, center);

		 Collections.rotate(innerBorder,-i);
		 Collections.rotate(outerBorder,-i);
	}

	private Point findCenter() {
		double x = 0;
		double y = 0;

		for (Point p : outerBorder) {
			x += p.x;
			y += p.y;
		}
		x /= outerBorder.size();
		y /= outerBorder.size();
		return new Point(x, y);
	}
	
	public Point getCenter(){
		return center;
	}

	public int indexOfClosestPoint(List<Point> poly, Point p) {
		int min_index = 0;
		double min_dist = Double.MAX_VALUE;

		for (int i = 0; i < poly.size(); i++) {
			Point polyPt = poly.get(i);
			double dist = Math.pow(polyPt.x - p.x, 2) + Math.pow(polyPt.y - p.y, 2);
			if (dist < min_dist) {
				min_dist = dist;
				min_index = i;
			}
		}
		return min_index;
	}

	private boolean isClockwise(List<Point> line) {
		Point p0 = line.get(0);
		Point p1 = line.get(1);
		Point p2 = line.get(2);

		Point v1 = new Point(p1.x - p0.x, p1.y - p0.y);
		Point v2 = new Point(p2.x - p0.x, p2.y - p0.y);
		//checks if z element of vector product is > 0
		return (v1.x * v2.y) - (v2.x * v1.y) > 0;
	}

	public void sortClockwise(int startIndex) {

		// Collections.rotate(innerBorder, startIndex);
		// Collections.rotate(outerBorder, startIndex);
	}

	public List<Point> getSortedPoints() {
		List<Point> sorted = new ArrayList<Point>(innerBorder);
		//sorted.addAll(outerBorder);
		return sorted;
	}
}
