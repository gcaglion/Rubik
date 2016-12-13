// https://kogs-www.informatik.uni-hamburg.de/~neumann/BV-WS-2007/BV-3-07.pdf
// https://github.com/ssloy/tinyrenderer/wiki/Lesson-4:-Perspective-projection


package my3d;

public class Core {
		
	static MyPoint Rotate(MyPoint p, int alpha, int beta, int gamma){
		MyPoint ret=new MyPoint();
		double cx=Math.cos(Math.toRadians(alpha));
		double cy=Math.cos(Math.toRadians(beta));
		double cz=Math.cos(Math.toRadians(gamma));
		double sx=Math.sin(Math.toRadians(alpha));
		double sy=Math.sin(Math.toRadians(beta));
		double sz=Math.sin(Math.toRadians(gamma));
		
		ret.x_3d=cy*(sz*p.y_3d+cz*p.x_3d)-sy*p.z_3d;
		ret.y_3d=sx*(cy*p.z_3d+sy*(sz*p.y_3d+cx*p.x_3d))+cx*(cz*p.y_3d-sz*p.x_3d);
		ret.z_3d=cx*(cy*p.z_3d+sy*(sz*p.y_3d+cz*p.x_3d))-sx*(cz*p.y_3d-sz*p.x_3d);
		
		return ret;
	}
/*
	static MyPoint TraslatePoint(MyPoint p, int tx, int ty, int tz){
		p.x_3d+=tx;
		p.y_3d+=ty;
		p.z_3d+=tz;
		return ( p );
	}
*/

	static MyPoint RotatePoint(MyPoint p, int alpha, int beta, int gamma){

		MyPoint vo=Rotate(p, alpha, beta, gamma);
		
		p.x_3d=vo.x_3d;
		p.y_3d=vo.y_3d;
		p.z_3d=vo.z_3d;
		
		return( p );
	}
/*	
	static Vector3d Rotate_MATRIX(Vector3d p, double alpha, double beta, double gamma){

		Vector3d ret=new Vector3d();
		
		Matrix3d Rx=new Matrix3d(
				1,0,0,
				0,Math.cos(Math.toRadians(alpha)),Math.sin(Math.toRadians(alpha)),
				0,-Math.sin(Math.toRadians(alpha)),Math.cos(Math.toRadians(alpha))
				);
		Matrix3d Ry=new Matrix3d(
				Math.cos(Math.toRadians(beta)),0,-Math.sin(Math.toRadians(beta)),
				0,1,0,
				Math.sin(Math.toRadians(beta)),0,Math.cos(Math.toRadians(beta))
				);
		Matrix3d Rz=new Matrix3d(
				Math.cos(Math.toRadians(gamma)),Math.sin(Math.toRadians(gamma)),0,
				-Math.sin(Math.toRadians(gamma)),Math.cos(Math.toRadians(gamma)),0,
				0,0,1
				);
		Matrix3d R=new Matrix3d();
		R.mul(Rz,Ry);
		R.mul(Rx);
		ret=MbyV(R,p);
		
		return ret;
	}

	static Vector3d MbyV(Matrix3d m, Vector3d v){
		Vector3d retv=new Vector3d();
		retv.x=m.m00*v.x+m.m01*v.y+m.m01*v.z;
		retv.y=m.m10*v.x+m.m11*v.y+m.m12*v.z;
		retv.z=m.m20*v.x+m.m21*v.y+m.m22*v.z;
		return retv;
	}

	static double Vdist(Vector3d v1, Vector3d v2){
		double ret;
		ret=Math.sqrt( Math.pow((v2.x-v1.x),2) + Math.pow((v2.y-v1.y),2) + Math.pow((v2.z-v1.z),2) );
		return ret;
	}
	static double Vdist(Vector3d v1, Point3d p2){
		double ret;
		Vector3d v2=new Vector3d((double)p2.x, (double)p2.y, (double)p2.z);
		ret=Math.sqrt( Math.pow((v2.x-v1.x),2) + Math.pow((v2.y-v1.y),2) + Math.pow((v2.z-v1.z),2) );
		return ret;
	}
	static double Vdist(Vector3d v1, MyPoint p2){
		double ret;
		Vector3d v2=new Vector3d((double)p2.x_3d, (double)p2.y_3d, (double)p2.z_3d);
		ret=Math.sqrt( Math.pow((v2.x-v1.x),2) + Math.pow((v2.y-v1.y),2) + Math.pow((v2.z-v1.z),2) );
		return ret;
	}
	static double Vdist(MyPoint p1, Point3d p2){
		double ret;
		Vector3d v1=new Vector3d(p1.x_3d, p1.y_3d, p1.z_3d);
		Vector3d v2=new Vector3d((double)p2.x, (double)p2.y, (double)p2.z);
		ret=Math.sqrt( Math.pow((v2.x-v1.x),2) + Math.pow((v2.y-v1.y),2) + Math.pow((v2.z-v1.z),2) );
		return ret;
	}
*/
	
	static double Vdist(MyPoint p1, MyPoint p2){
		return Math.sqrt( Math.pow((p2.x_3d-p1.x_3d),2) + Math.pow((p2.y_3d-p1.y_3d),2) + Math.pow((p2.z_3d-p1.z_3d),2) );
	}
}
