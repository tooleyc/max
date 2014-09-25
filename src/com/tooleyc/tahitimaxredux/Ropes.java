package com.tooleyc.tahitimaxredux;

import java.util.ArrayList;
import java.util.Random;

import rogue_opcode.ActionElement;
import rogue_opcode.GraphicResource;
import rogue_opcode.ScreenElement;
import rogue_opcode.geometrics.XYZf;
import rogue_opcode.geometrics.XYf;

public class Ropes extends ActionElement {
	private static final long serialVersionUID = 1L;
	
	static Ropes sOnly = new Ropes();
	static Random sRandy = new Random();
	
	private static ArrayList<Rope> sAllRopes = new ArrayList<Rope>();
	private Ropes() {}
	
	public static void addRope(float p1x, float p1y, float p1z, float p2x, float p2y, float p2z) {
		Rope tRope = new Rope(p1x, p1y, p1z, p2x, p2y, p2z);
		sAllRopes.add(tRope);
	}
	
	public static void deleteAllRopes() {
		for (Rope tRope : sAllRopes) {
			tRope.deleteMe();
		}
		
		sAllRopes.clear();
	}
	
	public static Rope hasRope(float px, float py, float pz) {
		for (Rope tRope : sAllRopes) {
			if (Math.abs(px - tRope.m1x) < 1) 
				if (Math.abs(py - tRope.m1y) < 1)
					if (Math.abs(pz - tRope.m1z) < 1) {
						return tRope;
					}
		}
		
		return null;
	}
	
	public void Update() {
		if (TMR.sOnly.mGamePulse % 5 == 0) {
			for (Rope tRope : sAllRopes) {
				tRope.update();
			}
		}
	}	
}

class Rope {
	ScreenElement mRopeSE;
	int mBaseResId = 0;
	int mCurrentResId = 0;
	int mSwingDirection = 1;
	int mNumFrames = 9;
	Spline2D mSpline;
	
	float m1x;
	float m1y;
	float m1z;

	
	Rope(float p1x, float p1y, float p1z, float p2x, float p2y, float p2z) {
		m1x = p1x;
		m1y = p1y;
		m1z = p1z;
		
		float tXOffset = 0;
		float tYOffset = 0;
		
		XYZf[] mPoints = new XYZf[100];		//TODO - need this to be an array fo points that max can proceed along somwhow
		
		int tBorderExtra = 40;
		
		for (float i=0;i<mNumFrames;i++) {
			XYf t1 = TMR.mapXYZToScreenXY(p1x, p1y, p1z);
			XYf t2 =TMR. mapXYZToScreenXY(p2x, p2y, p2z);
			ScreenElement tSE = new ScreenElement(R.drawable.shadow, 0, 0);
											
			GraphicResource tGR = new GraphicResource((int)Math.abs(t2.x - t1.x) + (tBorderExtra * 2), (int)Math.abs(t2.y - t1.y) + (tBorderExtra * 2));

			//posts
			tXOffset = (t1.x > t2.x) ? t2.x : t1.x;
			tYOffset = (t1.y > t2.y) ? t2.y : t1.y;
			t1.x -= tXOffset;
			t2.x -= tXOffset;
			t1.y -= tYOffset;
			t2.y -= tYOffset;
			
			tGR.drawSE(tSE, t1.x + tBorderExtra, t1.y + tBorderExtra, true);
			tGR.drawSE(tSE, t2.x + tBorderExtra, t2.y + tBorderExtra, true);
	
			tSE = new ScreenElement(R.drawable.orange, 0, 0);
			tSE.Hibernate();

			t1 = TMR.mapXYZToScreenXY(p1x, p1y, p1z + 1, true);
			t2 = TMR.mapXYZToScreenXY(p2x, p2y, p2z + 1, true);
			
			float pSmallerx = (p2x < p1x) ? p2x : p1x;
			float pSmallery = (p2y < p1y) ? p2y : p1y;
			float pSmallerz = (p2z < p1z) ? p2z : p1z;
			
			float tHalfx = (Math.abs(p2x - p1x) / 2) + pSmallerx;
			float tHalfy = (Math.abs(p2y - p1y) / 2) + pSmallery;
			float tHalfz = pSmallerz + 0.5f;
			XYf t3 = TMR.mapXYZToScreenXY(tHalfx + (i / 15), tHalfy + (i / 15), tHalfz + (i / 10));

			t1.x -= tXOffset;
			t2.x -= tXOffset;
			t3.x -= tXOffset;
			t1.y -= tYOffset;
			t2.y -= tYOffset;
			t3.y -= tYOffset;

			double tXCoords[] = {t1.x, t3.x, t2.x};
			double tYCoords[] = {t1.y, t3.y, t2.y};
	
			mSpline = new Spline2D(tXCoords, tYCoords);
	
			for (int tTime = 0; tTime < 100; tTime+=1) {
				double[] tPoint = mSpline.getPoint(tTime / 100f);
				float tX = (float)tPoint[0];
				float tY =  (float)tPoint[1];
				tGR.drawSE(tSE, tX + tBorderExtra, tY + tBorderExtra, true);
				
				//mPoints[tTime].x = p2
			}
			
			mBaseResId = (int)(tGR.mResID - i);
		}
		mCurrentResId = mBaseResId + Ropes.sRandy.nextInt(mNumFrames - 1);
		
		mRopeSE = new ScreenElement(mBaseResId, (int)tXOffset - tBorderExtra, (int)tYOffset - tBorderExtra);
		mRopeSE.DrawCentered(false);
		mRopeSE.ZDepth(TMR.ZIndexFromXYZ(10, 10, 10));			//TODO - what should this be?
	}
	
	void deleteMe() {
		for (int i=0;i<mNumFrames;i++) {
			GraphicResource.Unload(mBaseResId + i);
		}
		
		mRopeSE.Unload();
		mRopeSE = null;
	}
	
	XYZf getPositionFromTime(float pTime) {
		double[] tPoint = mSpline.getPoint(pTime);
		
		return new XYZf();

	}
	
	void update() {
		mCurrentResId += mSwingDirection;
		if ((mCurrentResId == mBaseResId + (mNumFrames - 1)) || (mCurrentResId == mBaseResId))
			mSwingDirection *= -1;
		mRopeSE.SetCurrentGR(mCurrentResId);
	}
}
