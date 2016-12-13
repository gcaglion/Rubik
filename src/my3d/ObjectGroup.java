package my3d;

import java.util.ArrayList;

public class ObjectGroup extends Object3d{
	ArrayList<Object3d> item;
	
	// constructor
	public ObjectGroup(){
		item=new ArrayList<Object3d>();
		pCount=0;
		p=new MyPoint[pCount];
	}
	
	public void add(Object3d o){
		item.add(o);
		pCount+=o.pCount;
		p=concat(p, o.p);
	}
	
	
	public void clear(){
		item.clear();
		pCount=0;
	}
	
	
	public void Rotate(int alpha, int beta, int gamma){
		for(int i=0; i<item.size(); i++) item.get(i).Rotate(alpha, beta, gamma);
	}
	public void Traslate(int tx, int ty, int tz){
		for(int i=0; i<item.size(); i++) item.get(i).Traslate(tx, ty, tz);
	}
	public void Project(Scene3D s){
		for(int i=0; i<item.size(); i++) item.get(i).Project(s);
	}
	
	public MyPoint[] concat(MyPoint[] a, MyPoint[] b) {
		   int aLen = a.length;
		   int bLen = b.length;
		   MyPoint[] c= new MyPoint[aLen+bLen];
		   System.arraycopy(a, 0, c, 0, aLen);
		   System.arraycopy(b, 0, c, aLen, bLen);
		   return c;
		}

}
