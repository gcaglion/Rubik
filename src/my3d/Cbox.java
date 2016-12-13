package my3d;

import rubik.CubeFace;

public class Cbox extends Object3d {

	int cSizeX, cSizeY, cSizeZ;
	int currPosX, currPosY, currPosZ;
	int borderWidth;
	Crectangle faceRectangle[];
	Crectangle borderRectangle[][];
	ObjectGroup borderedFace[];
	String tag;
	public int clr;
	
	// constructors
	public Cbox(int cSize, int bWidth, String boxTag){
		cSizeX=cSize; cSizeY=cSize; cSizeZ=cSize;
		borderWidth=bWidth;
		tag=boxTag;		
		CcubeBuilder(cSizeX, cSizeY, cSizeZ, boxTag);
	}
	public Cbox(int cSize, int bWidth){
		cSizeX=cSize; cSizeY=cSize; cSizeZ=cSize;
		borderWidth=bWidth;
		tag=null;
		CcubeBuilder(cSizeX, cSizeY, cSizeZ, tag);
	}
	public Cbox(int sx, int sy, int sz, int bWidth, String boxTag){
		cSizeX=sx; cSizeY=sy; cSizeZ=sz;
		borderWidth=bWidth;
		tag=boxTag;
		CcubeBuilder(cSizeX, cSizeY, cSizeZ, boxTag);
	}
	public Cbox(int sx, int sy, int sz, int bWidth){
		cSizeX=sx; cSizeY=sy; cSizeZ=sz;
		borderWidth=bWidth;
		tag=null;
		CcubeBuilder(cSizeX, cSizeY, cSizeZ, tag);
	}
	
	void CcubeBuilder(int sx, int sy, int sz, String boxTag){
		
		cSizeX=sx; cSizeY=sy; cSizeZ=sz;
		clr=MyColor.BLACK;
		ensemble=new ObjectGroup();
		faceRectangle=new Crectangle[6];
		borderedFace=new ObjectGroup[6];
		borderRectangle=new Crectangle[6][4];

		borderedFace[CubeFace.F]=new ObjectGroup();
		faceRectangle[CubeFace.F]=new Crectangle(sz-2*borderWidth, sx-2*borderWidth, CubeFace.GetDefaultColor(CubeFace.F), boxTag);		faceRectangle[CubeFace.F].Traslate(borderWidth,  0,  borderWidth);
		borderRectangle[CubeFace.F][0]=new Crectangle(borderWidth, sx, MyColor.BLACK);
		borderRectangle[CubeFace.F][1]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.F][1].Rotate(0,90,0);	borderRectangle[CubeFace.F][1].Traslate((sx-1), 0, 0);
		borderRectangle[CubeFace.F][2]=new Crectangle(borderWidth, sx, MyColor.BLACK); 													borderRectangle[CubeFace.F][2].Traslate(0,0 ,sz-borderWidth);
		borderRectangle[CubeFace.F][3]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.F][3].Rotate(0,90,0);	borderRectangle[CubeFace.F][3].Traslate((borderWidth), 0, 0);

		borderedFace[CubeFace.R]=new ObjectGroup();
		faceRectangle[CubeFace.R]=new Crectangle(sz-2*borderWidth, sy-2*borderWidth, CubeFace.GetDefaultColor(CubeFace.R));				faceRectangle[CubeFace.R].Traslate(borderWidth,  0,  borderWidth);
		borderRectangle[CubeFace.R][0]=new Crectangle(borderWidth, sy, MyColor.BLACK);
		borderRectangle[CubeFace.R][1]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.R][1].Rotate(0,90,0);	borderRectangle[CubeFace.R][1].Traslate((sy-1), 0, 0);
		borderRectangle[CubeFace.R][2]=new Crectangle(borderWidth, sy, MyColor.BLACK); 													borderRectangle[CubeFace.R][2].Traslate(0,0 ,sz-borderWidth);
		borderRectangle[CubeFace.R][3]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.R][3].Rotate(0,90,0);	borderRectangle[CubeFace.R][3].Traslate((borderWidth), 0, 0);
	
		borderedFace[CubeFace.B]=new ObjectGroup();
		faceRectangle[CubeFace.B]=new Crectangle(sz-2*borderWidth, sx-2*borderWidth, CubeFace.GetDefaultColor(CubeFace.B));				faceRectangle[CubeFace.B].Traslate(borderWidth,  0,  borderWidth);
		borderRectangle[CubeFace.B][0]=new Crectangle(borderWidth, sx, MyColor.BLACK);
		borderRectangle[CubeFace.B][1]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.B][1].Rotate(0,90,0);	borderRectangle[CubeFace.B][1].Traslate((sx-1), 0, 0);
		borderRectangle[CubeFace.B][2]=new Crectangle(borderWidth, sx, MyColor.BLACK); 													borderRectangle[CubeFace.B][2].Traslate(0,0 ,sz-borderWidth);
		borderRectangle[CubeFace.B][3]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.B][3].Rotate(0,90,0);	borderRectangle[CubeFace.B][3].Traslate((borderWidth), 0, 0);
	
		borderedFace[CubeFace.L]=new ObjectGroup();
		faceRectangle[CubeFace.L]=new Crectangle(sz-2*borderWidth, sy-2*borderWidth, CubeFace.GetDefaultColor(CubeFace.L));				faceRectangle[CubeFace.L].Traslate(borderWidth,  0,  borderWidth);
		borderRectangle[CubeFace.L][0]=new Crectangle(borderWidth, sy, MyColor.BLACK);
		borderRectangle[CubeFace.L][1]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.L][1].Rotate(0,90,0);	borderRectangle[CubeFace.L][1].Traslate((sy-1), 0, 0);
		borderRectangle[CubeFace.L][2]=new Crectangle(borderWidth, sy, MyColor.BLACK); 													borderRectangle[CubeFace.L][2].Traslate(0,0 ,sz-borderWidth);
		borderRectangle[CubeFace.L][3]=new Crectangle(borderWidth, sz, MyColor.BLACK); borderRectangle[CubeFace.L][3].Rotate(0,90,0);	borderRectangle[CubeFace.L][3].Traslate((borderWidth), 0, 0);
	
		borderedFace[CubeFace.U]=new ObjectGroup();
		faceRectangle[CubeFace.U]=new Crectangle(sy-2*borderWidth, sx-2*borderWidth, CubeFace.GetDefaultColor(CubeFace.U));				faceRectangle[CubeFace.U].Traslate(borderWidth,  0,  borderWidth);
		borderRectangle[CubeFace.U][0]=new Crectangle(borderWidth, sx, MyColor.BLACK);
		borderRectangle[CubeFace.U][1]=new Crectangle(borderWidth, sy, MyColor.BLACK); borderRectangle[CubeFace.U][1].Rotate(0,90,0);	borderRectangle[CubeFace.U][1].Traslate((sx-1), 0, 0);
		borderRectangle[CubeFace.U][2]=new Crectangle(borderWidth, sx, MyColor.BLACK); 													borderRectangle[CubeFace.U][2].Traslate(0,0 ,sy-borderWidth);
		borderRectangle[CubeFace.U][3]=new Crectangle(borderWidth, sy, MyColor.BLACK); borderRectangle[CubeFace.U][3].Rotate(0,90,0);	borderRectangle[CubeFace.U][3].Traslate((borderWidth), 0, 0);

		borderedFace[CubeFace.D]=new ObjectGroup();
		faceRectangle[CubeFace.D]=new Crectangle(sy-2*borderWidth, sx-2*borderWidth, CubeFace.GetDefaultColor(CubeFace.D), boxTag);				faceRectangle[CubeFace.D].Traslate(borderWidth,  0,  borderWidth);
		borderRectangle[CubeFace.D][0]=new Crectangle(borderWidth, sx, MyColor.BLACK);
		borderRectangle[CubeFace.D][1]=new Crectangle(borderWidth, sy, MyColor.BLACK); borderRectangle[CubeFace.D][1].Rotate(0,90,0);	borderRectangle[CubeFace.D][1].Traslate((sx-1), 0, 0);
		borderRectangle[CubeFace.D][2]=new Crectangle(borderWidth, sx, MyColor.BLACK); 													borderRectangle[CubeFace.D][2].Traslate(0,0 ,sy-borderWidth);
		borderRectangle[CubeFace.D][3]=new Crectangle(borderWidth, sy, MyColor.BLACK); borderRectangle[CubeFace.D][3].Rotate(0,90,0);	borderRectangle[CubeFace.D][3].Traslate((borderWidth), 0, 0);

		for(int f=0; f<6; f++){
			borderedFace[f].add(faceRectangle[f]);
			for(int i=0; i<4; i++) borderedFace[f].add(borderRectangle[f][i]);
		}
		
		 // face-specific Rotate() and Translate()
		borderedFace[CubeFace.R].Rotate(0, 0, -90 ); 	borderedFace[CubeFace.R].Traslate(cSizeX, 0, 0); 
														borderedFace[CubeFace.B].Traslate(0, cSizeY, 0); 
		borderedFace[CubeFace.L].Rotate(0, 0, -90 );  
		borderedFace[CubeFace.U].Rotate(90, 0, 0);  
		borderedFace[CubeFace.D].Rotate(90, 0, 0); 		borderedFace[CubeFace.D].Traslate(0, 0, cSizeZ); 

		//
		for(int f=0; f<6; f++) ensemble.add(borderedFace[f]);
		
		currPosX=0; currPosY=0; currPosZ=0;
		
		this.pCount=ensemble.pCount;
		this.p=ensemble.p;
	}

	public void Rotate(int alpha, int beta, int gamma){
		ensemble.Rotate(alpha, beta, gamma);
	}
	public void Traslate(int tx, int ty, int tz){
		ensemble.Traslate(tx, ty, tz);
		currPosX+=tx;
		currPosY+=ty;
		currPosZ+=tz;
	}
	public void Project(Scene3D s){
		ensemble.Project(s);
	}

	public void resetPos(){
		ensemble.Traslate(-currPosX, -currPosY, -currPosZ);
		currPosX=0; currPosY=0; currPosZ=0; 
	}
	
	
	public void setColor(int c){
		clr=c;
		for(int f=0; f<6; f++) faceRectangle[f].setColor(c);
	}
}
