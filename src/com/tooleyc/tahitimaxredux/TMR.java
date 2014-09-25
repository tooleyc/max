package com.tooleyc.tahitimaxredux;


import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Iterator;

import java.util.ArrayList;

import rogue_opcode.AnimatedView;
import rogue_opcode.GameProc;
import rogue_opcode.GraphicResource;
import rogue_opcode.ScreenElement;
import rogue_opcode.geometrics.XYf;

import android.util.Log;

public class TMR extends GameProc {
	static final int LOGICAL_WIDTH = (int)(300 * 1.00);
	static final int LOGICAL_HEIGHT = (int)(500 * 1.00);

	public static final int SPLASH_STATE = 1;
	public static final int PLAY_STATE = 2;

	public static int sGameState = SPLASH_STATE;

	public static TMR sOnly;

	int mCurrentLevel = 7;
	int mWorldSize;

	int mGamePulse = 0;

	World mWorld;




	float mXMouse;
	float mYMouse;
	float mLastXMouse;
	float mLastYMouse;
	boolean mSingleTap;
	int mLastAction;

	@Override
	public void InitializeOnce() {
		Log.d("tahiti", "here");
		sOnly = this;
		AnimatedView.sOnly.NormailzeResolution(LOGICAL_WIDTH, LOGICAL_HEIGHT);

		AnimatedView.sOnly.Debug(true);

		//Stack overflow code to enumerate drawables so I don't have to call new GraphicResource() manually on all of them
		//Clean this up!
		//TODO - add to rogue_opcode as a method instead
		try {
			Class RClass = R.class;
			Class[] subclasses = RClass.getDeclaredClasses();
			Class RDrawable = null;

			for (Class subclass : subclasses) {
				String tName = subclass.getCanonicalName();
				if ("com.tooleyc.tahitimaxredux.R.drawable".equals(subclass.getCanonicalName())) {
					RDrawable = subclass;
					break;
				}
			}

			Field[] drawables = RDrawable.getFields();
			for (Field dr : drawables) {
				new GraphicResource(dr.getInt(null));
			}
		}
		catch (IllegalAccessException iae) {
		}

		mWorld = World.createWorld(20, 20);

		new Max();

		loadLevel();
	}

	@Override
	public void InitializeOnResume() {
		//previewLevels();
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent pMotionEvent)
	// {
	// 	//AnimatedView.sOnly.mDebugString2 = pMotionEvent.getAction() + "";
	// 	if (pMotionEvent.getAction() == 2) {
	// 		mTouchState.SetState(TouchState.SCROLL, pMotionEvent, pMotionEvent, 1, 1);
	// 	}
	// 	return super.onTouchEvent(pMotionEvent);
	// }

	@Override
	public boolean onTouchEvent(MotionEvent pMotionEvent)
	{
		mLastXMouse = mXMouse;
		mLastYMouse = mYMouse;
		mXMouse = pMotionEvent.getX();
		mYMouse = pMotionEvent.getY();

		if ((mLastAction == 0) && (pMotionEvent.getAction() != 2))
			mSingleTap = true;

		mLastAction = pMotionEvent.getAction();

		/*if (Math.abs(pMotionEvent.getRawX() - mTouchState.mRawX) >= 1) {
			mTouchState.mXScrollDist = mTouchState.mRawX - pMotionEvent.getRawX();
			mTouchState.mRawX = pMotionEvent.getRawX();
		} else {
			mTouchState.mXScrollDist = 0;
		}

		if (Math.abs(pMotionEvent.getRawY() - mTouchState.mRawY) >= 1) {
			mTouchState.mYScrollDist = mTouchState.mRawY - pMotionEvent.getRawY();
			mTouchState.mRawY = pMotionEvent.getRawY();
		} else {
			mTouchState.mYScrollDist = 0;
		}*/

		//AnimatedView.sOnly.mDebugString2 = pMotionEvent.toString();

		//This is rather hacky - there is a condition where the user taps and holds before moving the mouse -
		//this condition does not generate a SCROLL event for whatever reason so we are forcing one here.
		//Someday I'll understand this - maybe
		/*if (pMotionEvent.getAction() == 2) {
			//This does two things - it prevents large jumps while in this special condition
			// - it compensates for the fact that mTouchState.mLastX/Y are not updated between different kinds of action
			float tXMove = (Math.abs(mTouchState.mLastX - pMotionEvent.getX()) < 2) ? mTouchState.mLastX - pMotionEvent.getX() : 0;
			float tYMove = (Math.abs(mTouchState.mLastY - pMotionEvent.getY()) < 2) ? mTouchState.mLastY - pMotionEvent.getY() : 0;
			mTouchState.SetState(TouchState.SCROLL, pMotionEvent, mTouchState.mLastMotionEvent, tYMove, tXMove);
			mTouchState.mLastMotionEvent = pMotionEvent;
			mTouchState.mLastX = pMotionEvent.getX();
			mTouchState.mLastY = pMotionEvent.getY();
		}*/


		return true;//super.onTouchEvent(pMotionEvent);
		//true - we handled the event, false - we didn't handle the event.
	}



	void previewLevels() {
		try {
			InputStream tIS;
			try {
				tIS = TMR.sOnly.getResources().openRawResource(R.raw.level1 + (mCurrentLevel - 1));
			}
			catch (Exception e) {
				mCurrentLevel = 1;
				tIS = TMR.sOnly.getResources().openRawResource(R.raw.level1 + (mCurrentLevel - 1));
			}

			BufferedReader tFile = new BufferedReader(new InputStreamReader(tIS));
			String tLine = null;

			int tX = 0;
			int tY = 0;
			int tZ = 0;

			boolean tReadingWorld = true;

			ScreenElement tSE = new ScreenElement(R.drawable.green_cube_no_border);
			GraphicResource tGR = new GraphicResource(LOGICAL_WIDTH, LOGICAL_HEIGHT);

			while ((tLine = tFile.readLine()) != null) {
				Log.d("tahiti", "line" + tLine);

				boolean tReadingComment = false;

				int i = tLine.length() - 1;

				if (tLine.equals("Ornaments"))
					tReadingWorld = false;

				if ((tLine.length() > 0) && tLine.charAt(0) == '#')
					tReadingComment = true;

				if (tReadingWorld && !tReadingComment) {
					//Level stuff
					tY = 0;
					while (i > 0) {
						tZ = Integer.parseInt(tLine.charAt(i - 1) + "");
						for (int j = 0;j<tZ;j++) {
							XYf tPoint = mapXYZToScreenXY(tX, tY, j);
							tGR.drawSE(tSE, tPoint.x, tPoint.y, true);
						}

						tY++;
						i -= 2;
					}
					tX++;
				}
			}

			tGR.scale(LOGICAL_WIDTH / 3, LOGICAL_HEIGHT / 3);
			new ScreenElement(tGR.mResID, (LOGICAL_WIDTH / 2), (LOGICAL_HEIGHT / 2));
		} catch(Exception e) {
			//SOL
			Log.d("tahiti", "SOL" + e.getMessage());
		}
	}

	void loadNextLevel() {
		mCurrentLevel ++;
		loadLevel();
	}

	void loadLevel() {
		//AnimatedView.sOnly.Shake(new XYf((float)(Math.random() * 64  - 32), 64), 10, 20);

//		RecycleableSE.recycleAllOfClass("isoblock");
		XYf tCenter = mapXYZToScreenXY(0, 0, 1);

		mWorld.reset();

		try {
			InputStream tIS;
			try {
				tIS = TMR.sOnly.getResources().openRawResource(R.raw.level1 + (mCurrentLevel - 1));
			}
			catch (Exception e) {
				mCurrentLevel = 1;
				tIS = TMR.sOnly.getResources().openRawResource(R.raw.level1 + (mCurrentLevel - 1));
			}

			BufferedReader tFile = new BufferedReader(new InputStreamReader(tIS));
			String tLine = null;

			int tX = 0;
			int tY = 0;
			int tZ = 0;
			char tType = ' ';
			int tDrawable = 0;

			// get size of world and stuff
			tFile.mark(1000000);
			do {
				tLine = tFile.readLine();
			} while (tLine.charAt(0) == '#');


			mWorldSize = tLine.length() / 2;
			tFile.reset();
			int tBlockCount = 0;
			while (((tLine = tFile.readLine()) != null) && (!tLine.equals("Ornaments"))) {
				if ((tLine.length() > 0) && (tLine.charAt(0) != '#')) {
					int i = tLine.length() - 1;
					while (i > 0) {
						tBlockCount += Integer.parseInt(tLine.charAt(i - 1) + "") + 1;
						i -= 2;
					}
				}
			}
			tFile.reset();

			Ropes.deleteAllRopes();
			/*RecycleableSE.recycleAllOfClass("ladder");
			RecycleableSE.recycleAllOfClass("bridge");
			RecycleableSE.recycleAllOfClass("isoblock");*/
			RecycleableSE.recycleAll();
			RecycleableSE.createNOfClass(tBlockCount, "isoblock");
			Iterator<RecycleableSE> tItr = RecycleableSE.getIteratorForClass("isoblock");

			boolean tReadingWorld = true;

			while ((tLine = tFile.readLine()) != null) {
				Log.d("tahiti", tLine);

				int i = tLine.length() - 1;
				boolean tReadingComment = false;

				if (tLine.equals("Ornaments"))
					tReadingWorld = false;

				if ((tLine.length() > 0) && tLine.charAt(0) == '#')
					tReadingComment = true;

				if (tReadingWorld && !tReadingComment) {
					//Level stuff
					tY = 0;
					while (i > 0) {
						tZ = Integer.parseInt(tLine.charAt(i - 1) + "");
						tType = tLine.charAt(i);
						//level numbers imply 0 a starting point of 0 (on the floor)
						//the entry number represents how many blocks to stack on the floor
						//so entry 1 would give you 1 block with the first block 0 resting on the floor
						for (int j = 0;j<tZ;j++) {
							XYf tPoint = mapXYZToScreenXY(tX, tY, j);

							if (tType == 'l')
								tDrawable = R.drawable.green_cube_no_border;
							else
								tDrawable = R.drawable.blue_cube_no_border;

							RecycleableSE tRSE = tItr.next();
							tRSE.mSE.SetCurrentGR(tDrawable);
							tRSE.mSE.ZDepth(ZIndexFromXYZ(tX, tY, j));

							if (!tRSE.mUsedOnLastLevel) {
								XYf tPoint2 = mapXYZToScreenXY(mWorldSize / 2, mWorldSize / 2, 0);
								tRSE.mSE.mPos.x = tPoint2.x;
								tRSE.mSE.mPos.y = tPoint2.y;
							}

							tRSE.mUsedOnLastLevel = true;		//TODO - what was this for again?

							tRSE.ballisticTo(1.0f + 0.4f * (float)Math.random(), tPoint.x, tPoint.y);

							mWorld.mAllWorldPositions[tX][tY] = new WorldPosition(tZ, tType + "");

							tRSE.mSE.Wake();
						}

						tY++;
						i -= 2;
					}
					tX++;
				} else {
					//Ornament stuff
					if (!tLine.equals("Ornaments") && !tReadingComment) {
						if (tLine.endsWith("R")) {
							String[] tFields = tLine.split(",");
							int t1x = Integer.parseInt(tFields[0]);
							int t1y = Integer.parseInt(tFields[1]);
							int t1z = Integer.parseInt(tFields[2]);

							int t2x = Integer.parseInt(tFields[3]);
							int t2y = Integer.parseInt(tFields[4]);
							int t2z = Integer.parseInt(tFields[5]);

							Ropes.addRope(t1x, t1y, t1z, t2x, t2y, t2z);
						} else if (tLine.endsWith("LY")) {
							String[] tFields = tLine.split(",");

							//Z is ranged - first number is where to start (ground is 0)
							//second number is how many blocks to add
							//so 0-1 woould be one block resting on ground level
							//1-8 would be 8 blocks starting one block up from the floor

							int tLow = Integer.parseInt(tFields[2].split("-")[0]);
							int tCount = Integer.parseInt(tFields[2].split("-")[1]);
							for (tZ = tLow;tZ < (tLow + tCount);tZ ++) {		//1-9 gives you 8 blocks from 1 to 8
								RecycleableSE tOrnament = RecycleableSE.initRecycleableSE("unknown");

								tX = Integer.parseInt(tFields[0]);
								tY = Integer.parseInt(tFields[1]);
								XYf tPoint = mapXYZToScreenXY(tX, tY, tZ);
								tOrnament.mSE.mPos.x = tPoint.x;
								tOrnament.mSE.mPos.y = tPoint.y;

								tOrnament.mSE.ZDepth(ZIndexFromXYZ(tX, tY, tZ) - 1);		//-1 to ensure on top of any existing SE small Z is closer to player

								tOrnament.mSE.SetCurrentGR(R.drawable.ladder);
								tOrnament.setClass("ladder");
								mWorld.mAllWorldPositions[tX][tY].addPositionEntry(tZ, "ladder");
								tOrnament.mSE.Wake();
							}
						} else if (tLine.endsWith("b")) {
							//bridge
							RecycleableSE tOrnament = RecycleableSE.initRecycleableSE("unknown");
							String[] tFields = tLine.split(",");
							tX = Integer.parseInt(tFields[0]);
							tY = Integer.parseInt(tFields[1]);
							tZ = Integer.parseInt(tFields[2]);
							XYf tPoint = mapXYZToScreenXY(tX, tY, tZ);
							tOrnament.mSE.mPos.x = tPoint.x;
							tOrnament.mSE.mPos.y = tPoint.y;

							tOrnament.mSE.ZDepth(ZIndexFromXYZ(tX, tY, tZ));

							tOrnament.mSE.SetCurrentGR(R.drawable.bridge);
							tOrnament.setClass("bridge");
							mWorld.mAllWorldPositions[tX][tY].addPositionEntry(tZ, "bridge");
							tOrnament.mSE.Wake();
						} else if (tLine.endsWith("Max!")) {
							String[] tFields = tLine.split(",");
							Max.sOnly.mX = Integer.parseInt(tFields[0]);
							Max.sOnly.mY = Integer.parseInt(tFields[1]);
							Max.sOnly.mZ = Integer.parseInt(tFields[2]);

						} else if (tLine.endsWith("!")) {
							//TODO - exactly how is this different from the bridge?
							//prize of some kind - TODO - read name out of last entry and draw graphic accordingly
							RecycleableSE tOrnament = RecycleableSE.initRecycleableSE("unknown");
							String[] tFields = tLine.split(",");
							tX = Integer.parseInt(tFields[0]);
							tY = Integer.parseInt(tFields[1]);
							tZ = Integer.parseInt(tFields[2]);
							XYf tPoint = mapXYZToScreenXY(tX, tY, tZ);
							tOrnament.mSE.mPos.x = tPoint.x;
							tOrnament.mSE.mPos.y = tPoint.y;

							tOrnament.mSE.ZDepth(ZIndexFromXYZ(tX, tY, tZ));

							tOrnament.mSE.SetCurrentGR(R.drawable.sphere);
							tOrnament.setClass("prize");
							mWorld.mAllWorldPositions[tX][tY].addPositionEntry(tZ, "prize");
							tOrnament.mSE.Wake();
						}
					}
				}
			}
		} catch(Exception e) {
			//SOL
			Log.d("tahiti", "crap " + e.getMessage());
		}
	}

	static float ZIndexFromXYZ(float pX, float pY, float pZ) {
		if (pX == 0 && pY == 0)
			return 10000;

		float A = (float)Math.sqrt(Math.pow(pX, 2) + Math.pow(pY, 2));
		float psi = (float)Math.atan(pX / pY);
		float theta = (float)(45 * (Math.PI / 180) - psi);
		float B = A * (float)Math.cos(theta);

		return -1 * (B + (pZ * 10));
	}

	public static XYf mapXYZToScreenXY(float pX, float pY, float pZ) {
		return mapXYZToScreenXY(pX, pY, pZ, false);
	}

	public static XYf mapXYZToScreenXY(float pX, float pY, float pZ, boolean pCenter) {
		XYf tPoint = new XYf();

		tPoint.x = (pX * 14) - (pY * 14);
		tPoint.y = (pX * 7) + (pY * 7);
		tPoint.y -= pZ * 15;

		tPoint.x += (LOGICAL_WIDTH / 2);
		tPoint.y += (LOGICAL_HEIGHT / 3);

		//tPoint.y += 30;

		if (pCenter)
			tPoint.y += 7;

		return tPoint;
	}

	public void Update() {
		mGamePulse ++;

		Max.sOnly.wake();		//TODO - why?

		super.Update();
	}
}

/*
	World is a fixed size X-Y plane
	Each entry in the plane is called a WorldPosition
	Each WorldPosition has a base height (mZ, the height of the land), the class of terrain (ie "land")
		as well as a variable number of PositionEntry.
	Each PositionEntry has a height (mZ) and a class (ie "bridge" or "ladder")
*/
class World {
	static World sOnly = null;

	WorldPosition[][] mAllWorldPositions;

	public static World createWorld(int pXSize, int pYSize) {
		if (sOnly != null)
			return sOnly;

		return new World(pXSize, pYSize);
	}

	private World(int pXSize, int pYSize) {
		sOnly = this;

		reset();
	}

	public void reset() {
		mAllWorldPositions = new WorldPosition[20][20];
	}

	public int GetElevation(float pX, float pY) {
		int tX = (int)(pX + .5f);
		int tY = (int)(pY + .5f);
		try {
			return (int)mAllWorldPositions[tX][tY].mZ;
		}
		catch (Exception e) {
			return 0;
		}
	}

	public boolean XYZHasA(float pX, float pY, float pZ, String pOrnamentClass) {
		int tX = (int)(pX + .5f);
		int tY = (int)(pY + .5f);
		try {
			WorldPosition tWP = mAllWorldPositions[tX] [tY];

			if (tWP != null) {
				for (PositionEntry tPE : tWP.mAllPositionEntries) {
					if (tPE.mOrnamentClass.equals(pOrnamentClass) && ((int)tPE.mZ == (int)pZ)) {
						return true;
					}
				}
			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}
}

class WorldPosition {
	ArrayList<PositionEntry> mAllPositionEntries;	//other non-ground level things associated with this position
	float mZ;			//z of grpund level for this position
	String mClass;		//type of ground - water, land, etc.

	WorldPosition(float pZ, String pClass) {
		mZ = pZ;
		mAllPositionEntries = new ArrayList<PositionEntry>();
	}

	void addPositionEntry(float pZ, String pOrnamentClass) {
		mAllPositionEntries.add(new PositionEntry(pZ, pOrnamentClass));
	}
}

class PositionEntry {
	float mZ;
	String mOrnamentClass;			//type of ornament - ie bridge, ladder, etc.

	PositionEntry(float pZ, String pOrnamentClass) {
		mZ = pZ;
		mOrnamentClass = pOrnamentClass;
	}
}
