package com.rush.krenol;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.*;
import android.widget.Toast;

public class KrenolActivity extends Activity {
    //  constants
    static final int CELL_SIDE = 32;
    static final int MAX_GUTTER = 8;
    static final int MIDDLE_SIDE = 5; // center cell plus two surrounding rows

    //  properties
    DrawThread mDrawThread;
	MainView mView;
	
	Bitmap mBoardBitmap;
	Bitmap mBallsBitmap;

	Rect mSrcRect = new Rect();
	Rect mDstRect = new Rect();

    Rect mScreenBoardExt = new Rect();
    float mScreenCellSide = 0.0f;

    GameBoard mBoard = null;
    int mCurMove = 0;

    byte mCurPlayer = GameBoard.CELL_WHITE;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//  game window setup
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//  load resources
		Resources res = getResources();
		mBallsBitmap = BitmapFactory.decodeResource(res, R.drawable.balls);
		mBoardBitmap = BitmapFactory.decodeResource(res, R.drawable.board);
		
		mView = new MainView(this, null);
		setContentView(mView);
		mDrawThread = new DrawThread(mView.getHolder());

        startGame(7, 4);
	}

    public void startGame(int gutterW, int gutterH) {
        mCurMove = 0;
        mCurPlayer = GameBoard.CELL_WHITE;
        mBoard = new GameBoard(gutterW*2 + MIDDLE_SIDE, gutterH*2 + MIDDLE_SIDE);
    }
	
	void startDrawingThread() {
		mDrawThread.setRunning(true);
		mDrawThread.start();
	}
	
	void stopDrawingThread() {
		boolean retry = true;
		mDrawThread.setRunning(false);
		while (retry) {
			try {
				mDrawThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// keep trying to stop the draw thread
			}
		}
	}

	public void update(float dt) {
	}

	public class MainView extends SurfaceView implements SurfaceHolder.Callback {
		public MainView(Context context, AttributeSet attrs) {
			super(context, attrs);
			getHolder().addCallback(this);
		}
		
		@Override
		public void onDraw(Canvas canvas) {
            // reposition the board into the target rectangle (FIXME: move it elsewhere)
            int boardW = mBoard.mBoardW;
            int boardH = mBoard.mBoardH;
            int fullBoardS = MIDDLE_SIDE + MAX_GUTTER*2;
            int lx = (fullBoardS - boardW)/2;
            int ly = (fullBoardS - boardH)/2;
            mSrcRect.set(lx*CELL_SIDE, ly*CELL_SIDE, (lx + boardW)*CELL_SIDE, (ly + boardH)*CELL_SIDE);

            // center the board to preserve the aspect ratio and fit the target frame
            float wantW = boardW*CELL_SIDE;
            float wantH = boardH*CELL_SIDE;
            float haveW = getWidth(); // FIXME
            float haveH = getHeight();

            float wantRatio = wantW/wantH;
            float haveRatio = haveW/haveH;

            float dx = 0.0f, dy = 0.0f;
            if (wantRatio > haveRatio) {
                dy = (0.5f*(wantW*haveH - wantH*haveW)/wantW);
            } else {
                dx = (0.5f*(wantH*haveW - wantW*haveH)/wantH);
            }
            mDstRect.set((int)dx, (int)dy, getWidth() - (int)dx, getHeight() - (int)dy);
            mScreenBoardExt.set(mDstRect);

            float screenCellH = (haveH - dy*2.0f)/(float)boardH;
            mScreenCellSide = screenCellH;

            //  draw the board
            canvas.drawBitmap(mBoardBitmap, mSrcRect, mDstRect, null);

            //  draw the cells

			mDstRect.set(0, 0, (int)mScreenCellSide, (int)mScreenCellSide);
			mSrcRect.set(0, 0, CELL_SIDE, CELL_SIDE);

            int numCells = mBoard.getNumCells();
            for (int i = 0; i < numCells; i++) {
                byte s = mBoard.mCells[i];
                if (s == GameBoard.CELL_EMPTY) {
                    continue;
                }
                int cellX = i%mBoard.mBoardW;
                int cellY = i/mBoard.mBoardW;

                int dstX = mScreenBoardExt.left + (int)(mScreenCellSide*(float)cellX);
                int dstY = mScreenBoardExt.top  + (int)(mScreenCellSide*(float)cellY);
                mDstRect.offsetTo(dstX, dstY);

                if (s == GameBoard.CELL_BLACK) {
                    mSrcRect.offsetTo(CELL_SIDE, 0);
                } else {
                    mSrcRect.offsetTo(0, 0);
                }
                mSrcRect.right = mSrcRect.left + CELL_SIDE;
                canvas.drawBitmap(mBallsBitmap, mSrcRect, mDstRect, null);
            }
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int touchX = (int) event.getX();
			int touchY = (int) event.getY();

            int cellX = (int)((float)(touchX - mScreenBoardExt.left)/mScreenCellSide);
            int cellY = (int)((float)(touchY - mScreenBoardExt.top)/mScreenCellSide);

            if (cellX >= 0 && cellY >= 0 &&
                cellX < mBoard.mBoardW && cellY < mBoard.mBoardH &&
                mBoard.getCell(cellX, cellY) == GameBoard.CELL_EMPTY) {
                mBoard.setCell(cellX, cellY, mCurPlayer);
                mCurMove++;
                if (mCurPlayer == GameBoard.CELL_BLACK) {
                    mCurPlayer = GameBoard.CELL_WHITE;
                } else {
                    mCurPlayer = GameBoard.CELL_BLACK;
                }
                byte winner = mBoard.getWinner();
                if (winner != GameBoard.CELL_EMPTY) {

                    String caption = "The winner is ";
                    caption += winner == GameBoard.CELL_BLACK ? "black!" : "white!";

                    Toast toast = Toast.makeText(getApplicationContext(), caption, Toast.LENGTH_LONG);
                    toast.show();

                    //startGame(3, 3);
                }
            }
            return true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			startDrawingThread();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			stopDrawingThread();
		}
	}

	class DrawThread extends Thread {
		private SurfaceHolder mSurfaceHolder;
		private boolean mIsRunning = false;
		private long mLastTime;

		public DrawThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;
		}

		public void setRunning(boolean isRunning) {
			mIsRunning = isRunning;
		}

		@Override
		public void run() {
			Canvas c;
			mLastTime = SystemClock.uptimeMillis();
			while (mIsRunning) {
				final long time = SystemClock.uptimeMillis();
                final float timeDelta = ((float)(time - mLastTime)) * 0.001f;
                mLastTime = time;
				update(timeDelta);
                c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						mView.onDraw(c);
					}
				} finally {
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
	}

}