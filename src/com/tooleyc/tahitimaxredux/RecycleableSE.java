package com.tooleyc.tahitimaxredux;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.MethodNotSupportedException;

import rogue_opcode.ActionElement;
import rogue_opcode.GameProc;
import rogue_opcode.ScreenElement;

public class RecycleableSE extends ActionElement {

	private static final long serialVersionUID = 2449211438632720749L;
	static final float ACCELERATION =2000f;			//pixels per update slice^2


	static ArrayList<RecycleableSE> sAllRSEs = new ArrayList<RecycleableSE>();

//	public static Iterator<RecycleableSE> getIterator() {
//		return sAllRSEs.iterator();
//	}

	public static Iterator<RecycleableSE> getIteratorForClass(final String pClass) {
		return new Iterator<RecycleableSE>()
		{
			int mIndex = 0;

			@Override
			public RecycleableSE next()
			{
				while(!sAllRSEs.get(mIndex).mClass.equals(pClass))
				{
					++mIndex;
				}
				return sAllRSEs.get(mIndex++);
			}

			@Override
			public boolean hasNext()
			{
				int tIndex = mIndex;
				while(sAllRSEs.size() > tIndex && !sAllRSEs.get(tIndex).mClass.equals(pClass))
				{
					++tIndex;
				}
				return sAllRSEs.size() > tIndex ;
			}

			@Override
			public void remove()
			{
				// NO!
			}
		};
	}

	private boolean mAvailable;
	private String mClass;

	ScreenElement mSE;

	boolean mUsedOnLastLevel;

	private float mBallisticTimer = 0;
	private float mBallisticDestX, mBallisticDestY;
	private float mAngularVelocity = 0;

	private RecycleableSE(String pClass) {
		mSE = new ScreenElement("");
		mAvailable = false;
		mClass = pClass;
		mUsedOnLastLevel = false;

		sAllRSEs.add(this);
	}

	public void setClass(String pClass) {
		mClass = pClass;
	}

	void recycle() {
		mAvailable = true;
		mSE.Hibernate();
	}

	static void recycleAllOfClass(String pClass) {
		for (RecycleableSE tRSE : sAllRSEs) {
			if (tRSE.mClass.equals(pClass)) {
				if (tRSE.mAvailable)
					tRSE.mUsedOnLastLevel = false;

				tRSE.recycle();
			}
		}
	}

	static void recycleAll() {
		for (RecycleableSE tRSE : sAllRSEs) {
			if (tRSE.mAvailable)
				tRSE.mUsedOnLastLevel = false;

			tRSE.recycle();
		}
	}

	static void recycleNOfClass(int pN, String pClass) {
		int tN = pN;
		for (RecycleableSE tRSE : sAllRSEs) {
			if (tRSE.mClass.equals(pClass)) {
				tRSE.recycle();
				tN --;
				if (tN == 0)
					break;
			}
		}
	}

	static void createNOfClass(int pN, String pClass) {
		for (int i=0;i<pN;i++)
			initRecycleableSE(pClass);
	}

	//initRecycleableSE initializes the first available RSEs - if none available a new one is created.
	static RecycleableSE initRecycleableSE(String pClass) {
		//first look for any RSEs marked as available
		for (RecycleableSE tRSE : sAllRSEs) {
			if (tRSE.mAvailable) {
				tRSE.mAvailable = false;
				tRSE.mClass = pClass;
				//tRSE.mSE.Wake();
				return tRSE;
			}
		}

		//nothing available, make a new one, add it to sAllRSEs and return a pointer to it
		return new RecycleableSE(pClass);
	}

	void ballisticTo(float pTimeSecs, float pDestX, float pDestY) {
		mBallisticDestX = pDestX;
		mBallisticDestY = pDestY;

		float tXDist = pDestX - mSE.mPos.x;
		float tYDist = pDestY - mSE.mPos.y;

		mSE.mVel.x = (tXDist / pTimeSecs);
		mSE.mVel.y = -Math.abs((tYDist - 0.5f * (ACCELERATION * pTimeSecs * pTimeSecs)) / pTimeSecs);

		mBallisticTimer = pTimeSecs * 1000;

		mAngularVelocity = (float)(Math.random() * 16) - 8;


		mActive = true;
	}

	@Override
	public void Update()
	{

		if ((mBallisticDestX == 0) && (mBallisticDestY == 0)) {
			mSE.Angle(0);
			Active(false);

			return;
		}

		if(mBallisticTimer > 0)
		{
			mBallisticTimer -= GameProc.UPDATE_PERIOD;

			mSE.mVel.y += ACCELERATION / (GameProc.UPDATE_FREQ + 1);
			mSE.mPos.add(mSE.mVel.dividedBy(GameProc.UPDATE_FREQ + 1));

			mSE.Angle(mSE.Angle() + mAngularVelocity);
		}
		if(mBallisticTimer <= 0)
		{
			mSE.mPos.x = mBallisticDestX;
			mSE.mPos.y = mBallisticDestY;
			mSE.Angle(0);
			Active(false);
		}
	}
}
