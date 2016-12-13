package my3d;

public class MyPoint {

	public double x_3d;
	public double y_3d;
	public double z_3d;
	double x_2d, y_2d, z_2d;
	int x_2ds, y_2ds;	// 'screen' x,y,z (centered into container)
	int clr;
	
	//-- constructor
	MyPoint(int px, int py, int pz){
		x_3d=px; y_3d=py; z_3d=pz;
	}
	MyPoint(double px, double py, double pz){
		x_3d=px; y_3d=py; z_3d=pz;
	}
	MyPoint(){
		x_3d=0; y_3d=0; z_3d=0;
	}
	void setX(int x){ x_3d=x; }
	void setY(int y){ y_3d=y; }
	void setZ(int z){ z_3d=z; }
	void setColor(int c){ clr=c; }
	
	void Traslate(int tx, int ty, int tz){
		x_3d+=tx;
		y_3d+=ty;
		z_3d+=tz;
	}
	
	void Project(Scene3D s){

		//-- difference vector
		//Vector3d v3= new Vector3d(x_3d-s.viewPoint.Position.x, y_3d-s.viewPoint.Position.y, z_3d-s.viewPoint.Position.z);
		
		//Vector3d v3= new Vector3d(x_3d, y_3d, z_3d);
		
		//-- first, rotate
		MyPoint v3r=Core.Rotate(this, s.viewPoint.Alpha, s.viewPoint.Beta, s.viewPoint.Gamma);

		//************************
		s.viewPoint.FocalDistance=(int)Core.Vdist(this, s.viewPoint.Position);
		//************************

		//-- then, project onto 2D plane (from https://github.com/ssloy/tinyrenderer/wiki/Lesson-4:-Perspective-projection)
		x_2d=(v3r.x_3d/(1-v3r.z_3d/s.viewPoint.FocalDistance));
		y_2d=(v3r.y_3d/(1-v3r.z_3d/s.viewPoint.FocalDistance));
		z_2d=(Core.Vdist(this, s.viewPoint.Position)/(1-v3r.z_3d/s.viewPoint.FocalDistance));

		//-- Center onto display
		x_2ds = (int)x_2d + s.getWidth()/2;
		y_2ds = (int)y_2d + s.getHeight()/2;
		
	}
}
