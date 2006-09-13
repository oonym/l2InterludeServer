/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-03-12
*/

package net.sf.l2j.gameserver.model;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.lib.Rnd;

public class L2Territory
{
	private static Logger _log = Logger.getLogger(L2Territory.class.getName());

	protected class Point
	{
		protected int x, y, zmin, zmax, proc;
		Point (int _x, int _y, int _zmin, int _zmax, int _proc) {x=_x; y=_y; zmin=_zmin; zmax=_zmax; proc=_proc;}
	}

	private List<Point> _points;
	private int _terr;
	private int _x_min;
	private int _x_max;
	private int _y_min;
	private int _y_max;
	private int _z_min;
	private int _z_max;
	private int _proc_max;

	public L2Territory(int terr)
	{
		_points = new FastList<Point>();
		_terr = terr;
		_x_min = 999999;
		_x_max =-999999;
		_y_min = 999999;
		_y_max =-999999;
		_z_min = 999999;
		_z_max =-999999;
		_proc_max = 0;
	}

	public void add(int x, int y, int zmin, int zmax, int proc)
	{
		_points.add(new Point(x,y,zmin,zmax,proc));
		if(x<_x_min) _x_min = x;
		if(y<_y_min) _y_min = y;
		if(x>_x_max) _x_max = x;
		if(y>_y_max) _y_max = y;
		if(zmin<_z_min) _z_min = zmin;
		if(zmax>_z_max) _z_max = zmax;
		_proc_max += proc;
	}

	public void print()
	{
		for(Point p : _points)
			System.out.println("("+p.x+","+p.y+")");
	}

	public boolean isIntersect(int x, int y, Point p1, Point p2)
	{
		double dy1 = p1.y - y;
		double dy2 = p2.y - y;

		if(Math.signum(dy1) == Math.signum(dy2))
			return false;
		
		double dx1 = p1.x - x;
		double dx2 = p2.x - x;

		if(dx1 >= 0 && dx2 >= 0)
			return true;
		
		if(dx1 < 0 && dx2 < 0)
			return false;

		double dx0 = (dy1 * (p1.x-p2.x))/(p1.y-p2.y);

		return dx0 <= dx1;
	}

	public boolean isInside(int x, int y)
	{
		int intersect_count = 0;
		for(int i=0; i<_points.size(); i++)
		{
			Point p1 = _points.get(i>0 ? i-1 : _points.size()-1);
			Point p2 = _points.get(i);

			if(isIntersect(x,y,p1,p2))
				intersect_count++;
	   	}

		return intersect_count%2 == 1;
	}

	public int[] getRandomPoint()
	{
		int i;
		int[] p = new int[4];
		if ( _proc_max>0) {
		    int pos = 0;
		    int rnd = Rnd.nextInt(_proc_max);
		    for( i=0; i<_points.size(); i++){
			Point p1 = _points.get(i);
			pos += p1.proc;
			if ( rnd <= pos ){
			    p[0] = p1.x; p[1] = p1.y;
			    p[2] = p1.zmin; p[3] = p1.zmax;
			    return p;
			}
		    }		    
		    
		}
		for( i=0; i<100; i++)
		{
			p[0] = Rnd.get(_x_min, _x_max);
			p[1] = Rnd.get(_y_min, _y_max);
			if(isInside(p[0],p[1])){
			    double curdistance = 0;
			    p[2] = _z_min+100;p[3] = _z_max;
			    for( i=0; i<_points.size(); i++){
				Point p1 = _points.get(i);
				long dx = p1.x-p[0];
				long dy = p1.y-p[1];
				double distance = Math.sqrt(dx*dx+dy*dy);
				if (curdistance == 0 || distance<curdistance){
				    curdistance = distance;
				    p[2] = p1.zmin+100;
				}
			    }
			    return p;
			}
		}
		_log.warning("Can't make point for territory"+_terr);
		return p;
	}
    	public int getProcMax()
	{
	    return _proc_max;
	}

}
