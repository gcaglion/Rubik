package my3d;

public class MyDimension {
	int x;
	int y;
	int z;

	public MyDimension(int px, int py, int pz){
		x=px;
		y=py;
		z=pz;
	}
	public MyDimension(int px, int py){
		x=px;
		y=py;
	}
	void set(int px, int py, int pz){
		x=px;
		y=py;
		z=pz;
	}
	void set(int px, int py){
		x=px;
		y=py;
	}
}
