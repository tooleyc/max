package com.tooleyc.tahitimaxredux;

import android.util.Log;

import rogue_opcode.ActionElement;
import rogue_opcode.AnimatedView;
import rogue_opcode.GameProc;
import rogue_opcode.ScreenElement;
import rogue_opcode.GameProc.TouchState;
import rogue_opcode.geometrics.XYZf;

@SuppressWarnings("serial")
public class Max extends ActionElement {
	static Max sOnly;

	static final float WALK_SENSETIVITY = .06f;			//how fast max moves when the finger is tap-hold-scroll

	ScreenElement mMaxSE;
	ScreenElement mMaxShadowSE;
	float mX = 9;
	float mY = 9;
	float mZ = 1;
	boolean mOnLadder = false;
	boolean mInJump = false;
	float mFallSpeed = 0;
	float mJumpSpeed = 0;

	Rope mCurrentRope = null;
	float mRopeProgress = 0;

	Max() {
		sOnly = this;
		mMaxSE = new ScreenElement(R.drawable.shadow, (int)TMR.mapXYZToScreenXY(mX, mY, mZ).x, (int)TMR.mapXYZToScreenXY(mX, mY, mZ).y);
		mMaxShadowSE = new ScreenElement(R.drawable.shadow_small, 0, 0);
		hibernate();
	}

	public void Update() {
		AnimatedView.sOnly.mDebugString2 = "";

		if (TMR.sOnly.mWorld.XYZHasA(mX, mY, mZ, "prize"))
		{
			TMR.sOnly.loadNextLevel();
		}

		if (mOnLadder) {
			AnimatedView.sOnly.mDebugString2 = "On a ladder!;";
		}

		if (mCurrentRope != null) {
			AnimatedView.sOnly.mDebugString2 = "On a rope!;";
			mRopeProgress += .01f;
			XYZf tNewPos = mCurrentRope.getPositionFromTime(mRopeProgress);
		}

		/*
		TouchState tTS = GameProc.sOnly.mTouchState;
		float tDeltaX = 0;
		float tDeltaY = 0;


		if (tTS.Is(TouchState.SCROLL)) {
			if (Math.abs(tTS.GetXScrollDist()) > Math.abs(tTS.GetYScrollDist())) {
				tDeltaX = (-1) * tTS.GetXScrollDist() * WALK_SENSETIVITY;
			} else {
				tDeltaY = (-1) * tTS.GetYScrollDist() * WALK_SENSETIVITY;
			}
		}

		if (tTS.Is(TouchState.SINGLE_TAP)) {
			if (!mInJump) {
				mInJump = true;
				mZ += .1;
				mFallSpeed = -0.9f;
			}
		}
		*/

		float tDeltaX = WALK_SENSETIVITY * (TMR.sOnly.mXMouse - TMR.sOnly.mLastXMouse);
		float tDeltaY = WALK_SENSETIVITY * (TMR.sOnly.mYMouse - TMR.sOnly.mLastYMouse);

		if (Math.abs(tDeltaX) > Math.abs(tDeltaY))
			tDeltaY = 0;
		else
			tDeltaX = 0;

		if (TMR.sOnly.mSingleTap) {
			if (!mInJump) {
				mInJump = true;
				mZ += .1;
				mFallSpeed = -0.9f;
				TMR.sOnly.mSingleTap = false;
				//TODO - perhaps make the jump exactly one block out?
				//Not like this...
				// if (tDeltaX > 0)
				// 	tDeltaX = 1;
				// if (tDeltaX < 0)
				// 	tDeltaX = -1;
				// if (tDeltaY > 0)
				// 	tDeltaY = 1;
				// if (tDeltaY > 0)
				// 	tDeltaY = -1;

			}
		}




		attemptMove(tDeltaX, tDeltaY);
		mMaxSE.mPos.x = TMR.mapXYZToScreenXY(mX, mY, mZ).x;
		mMaxSE.mPos.y = TMR.mapXYZToScreenXY(mX, mY, mZ).y;
		mMaxSE.ZDepth(TMR.ZIndexFromXYZ(mX, mY, mZ));

		mMaxShadowSE.mPos.x = TMR.mapXYZToScreenXY(mX, mY, TMR.sOnly.mWorld.GetElevation(mX, mY)).x;
		mMaxShadowSE.mPos.y = TMR.mapXYZToScreenXY(mX, mY, TMR.sOnly.mWorld.GetElevation(mX, mY)).y;
		mMaxShadowSE.ZDepth(TMR.ZIndexFromXYZ(mX, mY, TMR.sOnly.mWorld.GetElevation(mX, mY)));


		if (!mOnLadder) {
			boolean tFloating = true;

			float tSlop = 0.45f;

			//scan the four corners of our location to look for wall collisions
			if (TMR.sOnly.mWorld.GetElevation(mX + tSlop, mY + tSlop) == mZ)
				tFloating = false;

			if (TMR.sOnly.mWorld.GetElevation(mX - tSlop, mY - tSlop) == mZ)
				tFloating = false;

			if (TMR.sOnly.mWorld.GetElevation(mX - tSlop, mY + tSlop) == mZ)
				tFloating = false;

			if (TMR.sOnly.mWorld.GetElevation(mX + tSlop, mY - tSlop) == mZ)
				tFloating = false;



			//TODO - falling while on a bridge is still not perfect -
			//sometimes Max will fall through the bridge
			if (TMR.sOnly.mWorld.XYZHasA(mX + tSlop, mY + tSlop, mZ, "bridge"))
				tFloating = false;

			if (TMR.sOnly.mWorld.XYZHasA(mX - tSlop, mY - tSlop, mZ, "bridge"))
				tFloating = false;

			if (TMR.sOnly.mWorld.XYZHasA(mX - tSlop, mY + tSlop, mZ, "bridge"))
				tFloating = false;

			if (TMR.sOnly.mWorld.XYZHasA(mX + tSlop, mY - tSlop, mZ, "bridge"))
				tFloating = false;





			if (tFloating || (mFallSpeed < 0)) {
				float tZ = TMR.sOnly.mWorld.GetElevation(mX, mY);
				if (tZ < mZ) {
					mZ -= mFallSpeed;
					mFallSpeed += .2f;

					//we are falling - check to see if we cross a rope - if so, catch it!
					mCurrentRope = Ropes.hasRope(mX, mY, mZ);
				}

				if (tZ >= mZ) {
					mZ = tZ;
					mFallSpeed = .02f;
					//mInJump = false;
				}
			} else {
				mInJump = false;
			}

			// if we are somehow inside of a block then make sure we are moved outside of it (Ladders don't count)
			int tWallHit = wallHit();
			if (!mOnLadder && (tWallHit > 0)) {
				if (tWallHit == 1) {
					mX -= .1;mY -= .1;
				} else if (tWallHit == 2) {
					mX += .1;mY += .1;
				} else if (tWallHit == 3) {
					mX += .1;mY -= .1;
				} else if (tWallHit == 4) {
					mX -= .1;mY += .1;
				}
			}
		}
	}

	int wallHit() {
		float tSlop = 0.45f;
		float tLeeway = .2f;

		int tWallHit = 0;

		//scan the four corners of our location to look for wall collisions
		//we'll give Max a little leeway so that he can climb up steps and small ledges
		if (TMR.sOnly.mWorld.GetElevation(mX + tSlop, mY + tSlop) > mZ + tLeeway)
			tWallHit = 1;

		if (TMR.sOnly.mWorld.GetElevation(mX - tSlop, mY - tSlop) > mZ + tLeeway)
			tWallHit = 2;

		if (TMR.sOnly.mWorld.GetElevation(mX - tSlop, mY + tSlop) > mZ + tLeeway)
			tWallHit = 3;

		if (TMR.sOnly.mWorld.GetElevation(mX + tSlop, mY - tSlop) > mZ + tLeeway)
			tWallHit = 4;

		return tWallHit;
	}

	void attemptMove(float pDeltaX, float pDeltaY) {

		//TODO - if Max is trying to get into a passage exactly one block wide then help
		//align him if he is close

		if (pDeltaX == 0 && pDeltaY == 0)
			return;

		//temporary sanity checks until we get our movement controls finalized
		if (Math.abs(pDeltaX) > 1)
			return;

		if (Math.abs(pDeltaY) > 1)
			return;

		mX += pDeltaX;
		mY += pDeltaY;


		boolean tWallHit = (wallHit() != 0);

		//if we were on a ladder but not anymore then move max onto platform and get out of ladder mode
		if (mOnLadder) {
			mX -= pDeltaX;
			mY -= pDeltaY;

			mZ += 0.2f * (pDeltaY < 0 ? 1 : -1);				//Climbing speed - must be < DISMOUNT_ADJUST

			//Top of ladder reached?
			if (TMR.sOnly.mWorld.GetElevation(mX, mY - 0.5f) < mZ) {
			//if (!tWallHit) {
				mOnLadder = false;
				mY += -0.5f;
				mZ += 0.25f;											//DISMOUNT_ADJUST
				return;
			}

			//Bottom of ladder reached?
			if (TMR.sOnly.mWorld.GetElevation(mX, mY) > mZ) {
				mZ += 0.25f;											//DISMOUNT_ADJUST
				mOnLadder = false;
				return;
			}
		}

		//TODO - Max should be able to move up small steps, just not the big ones...
		else if (tWallHit) {
			//iWe have run into a wall (the elevation is higher than our current elevation)

			Log.d("tahiti", "My Rounded Location: " + (int)(mX + .5f) + ":" + (int)(mY + .5f) + ":" + mZ);
			Log.d("tahiti", "My Looking in y: " + (int)(mX + .5f) + ":" + (int)(mY - .5f) + ":" + mZ);
			//If there is a ladder we go up.
			if (TMR.sOnly.mWorld.XYZHasA(mX, mY - 0.5f, mZ + 1, "ladder")) {
				mOnLadder = true;
				mX = (int)(mX + 0.5f);
				return;
			}

			mX -= pDeltaX;
			mY -= pDeltaY;
		}
	}


	public void hibernate() {
		super.Active(false);
		mMaxSE.Hibernate();
	}

	public void wake() {
		super.Active(true);
		mMaxSE.Wake();
	}
}
