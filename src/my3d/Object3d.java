package my3d;

public abstract class Object3d {
	//int subCount;	// number of sub-Objects this object is made of
	//int[] pCount;	// [subObject], and is common for any shape
	//MyPoint[][] p;	// [subObject][PointId]	all the points   making up the object

	public MyPoint[] p;	// [PointId]	all the points   making up the object
	public int pCount;
	public ObjectGroup ensemble;	// this is used by composite objects like Ccube, Rubik3d

	public abstract void Rotate(int alpha, int beta, int gamma);
	public abstract void Traslate(int tx, int ty, int tz);
	public abstract void Project(Scene3D s);
	
}
