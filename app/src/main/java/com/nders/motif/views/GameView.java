package com.nders.motif.views;

import android.app.Activity;
import android.content.Context;
import android.database.SQLException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nders.motif.R;
import com.nders.motif.data.LevelDatabaseHelper;
import com.nders.motif.entities.Circle;
import com.nders.motif.entities.DotColor;
import com.nders.motif.entities.DotNode;
import com.nders.motif.entities.Line;
import com.nders.motif.entities.Rectangle;
import com.nders.motif.data.Loader;
import com.nders.motif.levels.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;



public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback, Loader.LoaderListener{

    // for logging debug information
    private static final String TAG = GameView.class.getSimpleName();


    /**
     **   LAYOUT
     */

    // Horizontal spacing between dots
    private static final int HORIZONTAL_SPACING = 100;

    // Vertical spacing between dots
    private static final int VERTICAL_SPACING = 100;

    // The number of dots in a row.
    private static final int MAX_ROW_COUNT = 6;

    // The number of dots in a column.
    private static final int MAX_COLUMN_COUNT = 6;

    // Height and width of the area allocated for playing the dots.
    // This area is centered within the view.
    private static final int DIMENSION = 1000;

    // Radius of main dots - dots centred in the view.
    protected static final int RADIUS = 50;

    // Radius of smaller dots drawn in the header
    protected static final int RADIUS_SMALL = 40;

    // Spacing between the smaller dots drawn in the header.
    protected static final float HEADER_DOTS_SPACING = 90;

    // The height setting of the header and the footer.
    protected static final int HEADER_FOOTER_HEIGHT = 300;

    // Padding used in "padding" the contents of the header and the footer.
    protected static final float VERTICAL_PADDING = 16;

    protected static final int SCORE_TEXT_DIM = 300;


    /**
     **   DRAWING
     */

    // Partitions
    protected Rect headerRect;
    protected Rect footerRect;
    protected Rect scoreRect;
    protected Rect movesRect;

    // Colors
    protected int BACKGROUND_COLOR  = Color.WHITE;
    protected int HEADER_FOOTER_COLOR = Color.parseColor("#FFF6D5");
    protected int SCORE_COLOR =  HEADER_FOOTER_COLOR;
    protected int TEXT_COLOR = Color.parseColor("#202020");
    protected int TEXT_MOVES_COLOR = Color.LTGRAY; //Color.parseColor("#FFC107");
    // Paints
    protected Paint mTextPaint;
    protected Paint mBlackPathPaint;
    protected Paint mWhitePaint;
    protected Paint mWhitePathPaint;
    protected Paint mBlackPaint;
    protected Paint mColorPaint;
    protected Paint mColorPathPaint;
    protected Paint mBackgroundPathPaint;
    protected Paint mBackgroundPaint;
    protected Paint mHeaderFooterPaint;

    // Tracks the position of the initial dot touched when a player is about connect ...
    // ... a series of dots.
    protected float mStartX;
    protected float mStartY;

    // Tracks the touch position.
    // mx is short for move event X value
    // my is short for move event Y value.
    protected float mx;
    protected float my;
    protected float last_mx;
    protected float last_my;

    // Text Params
    protected static final float TEXT_SIZE_MEDIUM = 45;
    protected static final float TEXT_SIZE_LARGE = 150;
    protected static final float TEXT_SIZE_SMALL = 40;
    protected static final float TEXT_PADDING = 100;

    protected static final float TOUCH_STROKE_WIDTH = 40;
    protected static final float TOUCH_TOLERANCE = 2;
    protected static final float RECT_TOLERANCE = 4;





    /**
     **  CONTAINERS
     */

    // Keeps track of the stationary dots drawn on screen.
    protected Stack<List<Rectangle>> mRectangles;

    // Temporary variables for initializing the game
    protected List<Circle> mCircles = new ArrayList<>();
    protected List<Circle> mBufferCircles = new ArrayList<>();

    // Keeps track of all the lines drawn
    protected Stack<Line> mLines;

    // Stores the first selected dot
    protected Rectangle mStartRect = null;
    private DotColor mSelectedDotColor = null;

    // Stores all the dots that have been connected
    // The contents of this container get swapped with other dots ...
    // ... that appear above them and have not been selected (connected).
    // This occurs if and only if the size of the container is greater than one.
    protected Stack<Rectangle> mSelectedDots;

    // Stores all the dots that are to been removed.
    protected SparseArray<Rectangle> mDotsToBeUpdated;


    /**
     **  DATA
     */

    // Stores the relationship between dots.
    // A pair of dots (represented as an ArrayList) is the key; while the "existence" of an ...
    // ... edge (boolean) is the value.
    protected HashMap<ArrayList, Boolean> mEdges = new HashMap<>();

    // The number of dots displayed on the screen.
    // This corresponds to the size of a game.
    protected static final int DOT_COUNT = MAX_COLUMN_COUNT* MAX_ROW_COUNT;

    // List of objects containing the graph nodes loaded from the database.
    // These are nodes displayed on the screen as colored dots.
    // There are "DOT_COUNT" number of graph nodes.
    protected List<DotNode> mGraphNodes;

    // List of remaining nodes loaded from the database.
    // The content of this container are gradually removed and used to replace ...
    // .. selected dots in "mGraphNodes".
    protected List<DotNode> mBufferNodes = new ArrayList<>();

    // List of deleted nodes.
    // Nodes are deleted when their corresponding dots have been selected and ...
    // ... removed.
    protected List<DotNode> mDeletedNodes = new ArrayList<>();

    // Object delegated with the responsibility of querying and loading data ...
    // ... from the database.
    protected Loader mDataLoader;

    /**
     **  ANIMATION
     */

    static final int ANIM_START_POS = HEADER_FOOTER_HEIGHT * 2;
    static int TIME_DELAY;



    /**
     **  GAME CONTROL
     */

    // This thread is used in tandem with the "main" or UI thread.
    // It handles the all drawing calls besides the first initialization of the game.
    protected Thread mGameThread = null;

    enum STATE {ACTION_DOWN, ACTION_UP, MOVING, RESET, DO_NOTHING}

    // OnTouch State for passing control from the UI thread to the GameThread.
    protected STATE mState = STATE.RESET;

    // Is true when data (nodes) have been loaded from the database.
    protected boolean mDoneLoadingData = false;

    // Is true when the associated activity starts or resumes.
    protected boolean  mSurfaceReady = false;

    // Is true when the game starts and all the initial set of dots have been drawn.
    protected boolean mInitialized = false;

    // Is true when the associated activity is not paused and is in focus.
    protected boolean mRunning = false;

    // Is true when the surface is ready, data has been loaded and the game has been ...
    // ... initialized.
    // It is set to false when game is paused or drawing is occurring.
    // When the game is not ready, user input (touch input) is ignored.
    protected boolean mReady = false;

    // Is true when the SurfaceView has been created and it's "OnSurfaceCreated" callback ...
    // ... method has been called.
    protected boolean mSurfaceCreated = false;

    // Is true when the SurfaceView has been destroyed. This boolean is set true in the ...
    // ... SurfaceView's "OnSurfaceDestroyed" callback.
    protected boolean mSurfaceWasDestroyed = true;

    // Object holding the details of the current game level.
    protected Level mGameLevel = null;

    // Keeps track of frequencies of the "dotColors" loaded.
    // This is used for ensuring the an optimum number of particular colors are present.
    // This varies with the game level.
    protected EnumMap<DotColor, Integer> mDotColorCounter = new EnumMap<>(DotColor.class);

    // The current game int_score.
    protected int mGameScore = 0;


    /**
     **   MISC
     */

    // Reference to the SurfaceView's surface holder.
    protected SurfaceHolder mSurfaceHolder;

    // Callback interface for handling "game complete" and "game over" events.
    protected GameOverListener mGameOverListener = null;

    // Static reference to the GameView to prevent the recreation of multiple instances.
    private static GameView sInstance;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Constructors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Constructor is private to prevent direct instantiation.
     * Make a call to the static method "getInstance" instead.
     * @param context
     */
    private GameView(Context context) {
        this(context, null);
    }

    /**
     * Constructor is private to prevent direct instantiation.
     * Make a call to the static method "getInstance" instead.
     * @param context
     * @param attrs
     */
    private GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Constants
        TIME_DELAY = (int) (1000/((Activity)context).getWindowManager()
                .getDefaultDisplay().getRefreshRate());



        // Paints

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint.setColor(BACKGROUND_COLOR);
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mBackgroundPathPaint = new Paint();
        mBackgroundPathPaint.setAntiAlias(true);
        mBackgroundPathPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH + 4);
        mBackgroundPathPaint.setDither(true);
        mBackgroundPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mBackgroundPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackgroundPathPaint.setColor(BACKGROUND_COLOR);
        mBackgroundPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));


        mBlackPathPaint = new Paint();
        mBlackPathPaint.setAntiAlias(true);
        mBlackPathPaint.setDither(true);
        mBlackPathPaint.setStyle(Paint.Style.STROKE);
        mBlackPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
        mBlackPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mBlackPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mBlackPathPaint.setColor(getContext().getColor(R.color.black));
        mBlackPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mBlackPaint = new Paint();
        mBlackPaint.setAntiAlias(true);
        mBlackPaint.setDither(true);
        mBlackPaint.setStyle(Paint.Style.FILL);
        mBlackPaint.setColor(getContext().getColor(R.color.black));
        mBlackPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mWhitePaint = new Paint();
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWhitePaint.setColor(getContext().getColor(R.color.white));
        mWhitePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mWhitePathPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mWhitePathPaint.setStyle(Paint.Style.STROKE);
        mWhitePathPaint.setStrokeWidth(8);
        mWhitePathPaint.setStrokeJoin(Paint.Join.ROUND);
        mWhitePathPaint.setStrokeCap(Paint.Cap.ROUND);
        mWhitePathPaint.setColor(Color.WHITE);
        mWhitePathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mColorPathPaint = new Paint();
        mColorPathPaint.setStyle(Paint.Style.FILL);
        mColorPathPaint.setDither(true);
        mColorPathPaint.setAntiAlias(true);
        mColorPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mColorPathPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
        mColorPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStrokeWidth(TOUCH_STROKE_WIDTH - 2);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mHeaderFooterPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mHeaderFooterPaint.setStyle(Paint.Style.FILL);
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);

        // Keeping track of dots and User drawings
        mRectangles = new Stack<>();
        mCircles = new ArrayList<>();
        mLines = new Stack<>();
        mSelectedDots = new Stack<>();
        mDotsToBeUpdated = new SparseArray<Rectangle>();

        // Data Loader
        mDataLoader = new Loader(context,36);
        mDataLoader.setLoadListener(this);

        // Surface Holder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        mSurfaceHolder.addCallback(this);
        setDrawingCacheEnabled(true);
    }


    /**
     * Static method used to create a singleton instance of the class
     * @param context Activity context
     * @return static instance of this class
     */
    public static GameView getInstance(Context context){
        if(sInstance == null){
            sInstance = new GameView(context);
        }else{
            // necessary to redraw the surface
            sInstance.mState = STATE.RESET;
        }
        return sInstance;
    }

    /**
     * Used to quit and destroy the Singleton static instance of the class.
     */
    public static void quitInstance(){
        if(sInstance != null){
            sInstance.close();
            sInstance = null;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Drawing Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus){
            pause();
        }else {
            resume();
        }
        mState = STATE.RESET;
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // hack to cancel multitouch events
        if(event.getPointerCount() > 1) return false;
        if(!mReady) return false;

        // Retrieve the point
        mx = event.getX();
        my = event.getY();

        // Handles the drawing of lines
        onTouchEventLine(event);
        return true;
    }

    private void onTouchEventLine(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // First touch. Store the initial point
                mStartX = mx;
                mStartY = my;
                for(List<Rectangle> list: mRectangles){
                    for(Rectangle rect: list){
                        if(rect.contains(mStartX, mStartY)){
                            mStartX = rect.getX();
                            mStartY = rect.getY();
                            mStartRect = rect;
                            mColorPaint.setColor(mStartRect.dotColor().colorInfo());
                            mColorPathPaint.setColor(mStartRect.dotColor().colorInfo());
                            mStartRect.select();
                            mSelectedDotColor = mStartRect.dotColor();
                            mSelectedDots.push(mStartRect);
                            break;
                        }
                    }
                }
                if(mStartRect != null){
                    mState = STATE.ACTION_DOWN;
                }else{
                    mState = STATE.DO_NOTHING;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mStartRect == null){
                    break;
                }
                for(List<Rectangle> list: mRectangles){
                    for(Rectangle rect: list){
                        //==============================================================
                        // IF "rect" CONTAINS THE COORDINATE, {mx, my},...
                        // A LINE COULD BE DRAWN
                        //==============================================================
                        if(rect.contains(mx, my)){
                            float mx_raw = mx; // save raw current x value
                            float my_raw = my; // save raw current y value
                            mx = rect.getX();
                            my = rect.getY();

                            if(mStartRect != null ){
                                if(rect.id() != mStartRect.id()){
                                    if(mDataLoader.checkEdge(mStartRect.id(), rect.id(), mEdges)) {
                                        //==============================================================
                                        // IF LINE TO BE DRAWN IS NON-DIAGONAL
                                        //==============================================================
                                        if (mx == mStartX || my == mStartY) {
                                            //Log.i(TAG, "TOUCHED: " + rect.mId());
                                            // new line to be added
                                            Line line = new Line(mStartX, mStartY, mx, my);

                                            //==============================================================
                                            // BACKTRACK (IF CONDITION IS MEANT)
                                            //==============================================================
//                                        if (mLines.size() > 0) {
//                                            double distFrom2ndLastDot =
//                                                    Math.sqrt(Math.pow(mLines.peek().startX - mx_raw, 2) +
//                                                    Math.pow(mLines.peek().startY - my_raw, 2));
//                                            // The sum, (VERTICAL_SPACING + 2 * RADIUS - RECT_TOLERANCE),
//                                            // is the value below which the most recent line is removed
//                                            // of the stack.
//
//                                            // This allows for undo movesLeft
//
//                                            if (distFrom2ndLastDot <= (VERTICAL_SPACING + 2 * RADIUS - RECT_TOLERANCE)) {
//                                                mStartX = mLines.peek().startX;
//                                                mStartY = mLines.peek().startY;
//                                                mLines.pop();
//
//                                                // pop rectangle off stack
//                                                rect.deselect();
//                                                if(!mSelectedDots.empty()) mSelectedDots.pop();
//
//                                                break;
//                                            }
//                                        }

                                            //==========================================================
                                            // ADD NEW LINE
                                            //==========================================================

                                            // Add new line iff the line (to be drawn) connects
                                            // only adjacent dots

                                            // The sum, (VERTICAL_SPACING + 2*RADIUS), is the maximum
                                            // distance between the centres of two adjacent dots

                                            if (line.length() <= (VERTICAL_SPACING + 2 * RADIUS)){

                                                //==========================================================
                                                // IF RECTANGLE HASN'T BEEN CONNECTED
                                                //==========================================================
                                                if (!rect.isSelected()) {
                                                    mLines.push(line);
                                                    mStartX = mx;
                                                    mStartY = my;

                                                    // push rectangle onto stack
                                                    rect.select();
                                                    mSelectedDots.push(rect);

//                                                double distFromLastDot = Math.sqrt(Math.pow(mLines.peek().endX - mx_raw, 2) +
//                                                        Math.pow(mLines.peek().endY - my_raw, 2));
//                                                if (mOnDrawingPathListener != null && distFromLastDot > (VERTICAL_SPACING / 2)) {
//                                                    mOnDrawingPathListener.onDotConnected(mSelectedDots.size());
//                                                }
                                                }
                                            }
                                        } //### END IF (LINE TO BE DRAWN IS NON-DIAGONAL)

                                    }
                                }
                                else{
                                    //==============================================================
                                    // CLOSE PATH
                                    //==============================================================
                                    // To close a rectangular path 4 lines are needed.
                                    // If mLines has at least 3 lines, close path
                                    if(mLines.size() >= 3){
                                        Line line = new Line(mStartX, mStartY, mx, my);
                                        mLines.push(line);
                                    }
                                }
                                break;
                            } // END IF (mStartRect != null &&  mOnDrawingPathListener != null )
                        }
                    }
                }
                mState = STATE.MOVING;
                break;
            case MotionEvent.ACTION_UP:
                if(mStartRect != null){
                    mState = STATE.ACTION_UP;
                }else{
                    mState = STATE.RESET;
                }
                break;
        }
    }


    public synchronized void onActionUp(){

        // If no two dots have been connected, return
        if(mSelectedDots.size() <= 1 || mLines.empty()){
            mState = STATE.RESET;
            return;
        }

        Canvas canvas = mSurfaceHolder.lockCanvas(null);

        //////////////////////////
        //
        // STEP 1: RESET SURFACE
        //
        //////////////////////////

        // Redraw Rectangles
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
        for(List<Rectangle> rectRow: mRectangles){
            for(Rectangle rect: rectRow){
                mColorPaint.setColor(rect.dotColor().colorInfo());
                canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
            }
        }
        // Redraw Header and Footer
        drawHeaderAndFooter(canvas);

        mSurfaceHolder.unlockCanvasAndPost(canvas);
        Log.i(TAG,"SELECTED DOTS: " + mSelectedDots.size());


//        try {
//            Thread.sleep(TIME_DELAY*200);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }
        System.out.print("\n------------PHASE 1----------------\n");
        printRects();

        /////////////////////////////////////////////////////////////////
        //
        // STEP 2:  ERASE SELECTED DOTS AND MOVE UNSELECTED DOTS THAT ...
        //          ... APPEAR ABOVE SELECTED DOTS DOWNWARDS
        //
        ////////////////////////////////////////////////////////////////


        // For every column, search upwards for selected dots.
        // For every selected dot found, swap with the nearest unselected dot found
        // Iterate until no unselected dot appears above a selected dot.

        synchronized (mRectangles){

            /*
                Algorithm.
                ----------
                starting from the second to the last row,...
                For every row, i{
                    For every column, j, in i{

                        If dot in position (i,j), is unselected
                        and find the lowest selected dot below it   {
                            if any found : => swap dots
                        }

                    }
                }

             */
//
//
//            for(int rowIdx = 1; rowIdx < MAX_ROW_COUNT; rowIdx++){
//
//                // Dots in row currently examined
//                List<Rectangle> currentRow = mRectangles.get(rowIdx);
//
//
//
//                for(int colIdx = 0; colIdx < MAX_COLUMN_COUNT; colIdx++){
//                    Rectangle currentDot = currentRow.get(colIdx);
//                    int rectID = currentDot.mId();
//
//                    // If current dot is unselected
//                    if(!currentDot.isSelected()){
//                        int targetRow = rowIdx;
//                        boolean rowFound = false;
//
//                        // find the next unselected dot (rectangle)
//                        while (--targetRow >= 0 ) {
//                            if (mRectangles.get(targetRow).get(colIdx).isSelected()){
//                                rowFound = true;
//                                break;
//                            }
//                        }
//
//                        if(rowFound){
//                            List<Rectangle> bottomRow = mRectangles.get(targetRow);
//
//                            // Selected dot to be swapped
//                            Rectangle bottomDot= bottomRow.get(colIdx);
//
//                            Rect currentRect = new Rect(
//                                    (int)(currentDot.left() - RECT_TOLERANCE - HORIZONTAL_SPACING/2),
//                                    (int)(currentDot.top() - RECT_TOLERANCE - VERTICAL_SPACING/2),
//                                    (int)(currentDot.right() + RECT_TOLERANCE+ HORIZONTAL_SPACING/2),
//                                    (int)(currentDot.bottom()  + RECT_TOLERANCE+ VERTICAL_SPACING/2)
//                            );
//
//                            Rect bottomRect = new Rect(
//                                    (int)(bottomDot.left() - RECT_TOLERANCE - HORIZONTAL_SPACING/2),
//                                    (int)(bottomDot.top() - RECT_TOLERANCE - VERTICAL_SPACING/2),
//                                    (int)(bottomDot.right() + RECT_TOLERANCE+ HORIZONTAL_SPACING/2),
//                                    (int)(bottomDot.bottom()  + RECT_TOLERANCE+ VERTICAL_SPACING/2)
//                            );
//
//
//
//                            // CLEAR PREVIOUS LOCATION (OF TOP-UNSELECTED DOT)
//                            canvas = mSurfaceHolder.lockCanvas(currentRect);
//                            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
//                            mSurfaceHolder.unlockCanvasAndPost(canvas);
//
//                            try {
//                                Thread.sleep(5);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//
//
//
//                            // DRAW DOT AT NEW POSITION
//                            mColorPaint.setColor(currentDot.dotColor().colorInfo());
//                            canvas = mSurfaceHolder.lockCanvas(bottomRect);
//                            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
//                            canvas.drawCircle(bottomDot.getX(), bottomDot.getY(), RADIUS, mColorPaint);
//                            mSurfaceHolder.unlockCanvasAndPost(canvas);
//                            ///////////////////////////////////////////////////////////////
//
//                            // Swap attributes
//                            currentDot.swap(bottomDot);
//
//
//                            // ADD TO LIST TO BE UPDATED
//                            mDotsToBeUpdated.put(rectID, bottomDot);
//
//                            currentRow.set(colIdx, bottomDot);
//                            mRectangles.set(targetRow, currentRow);
//                        }
//                    }
//
//
//                }
//            }
            List<Rectangle> markedDots = new ArrayList<>();

            for(int colIdx = 0; colIdx < MAX_COLUMN_COUNT; colIdx++){
                // NOTE
                // The row order is in reverse
                // The last row displayed on the GUI is the first row in the ...
                // ... mRectangles list.
                for(int rowIdx = 0; rowIdx < MAX_ROW_COUNT ; rowIdx++){
                    List<Rectangle> rectRow = mRectangles.get(rowIdx); // row list

                    Rectangle rect = rectRow.get(colIdx);

                    // skip rectangles not selected
                    if(!rect.isSelected()) continue;

                    // Params of Rectangle (Dot) to be updated
                    int rectID = rect.id();  // mId to be removed

                    Rect bottomRect = new Rect(
                            (int)(rect.left() - RECT_TOLERANCE - HORIZONTAL_SPACING/2),
                            (int)(rect.top() - RECT_TOLERANCE - VERTICAL_SPACING/2),
                            (int) (rect.right() + RECT_TOLERANCE+ HORIZONTAL_SPACING/2),
                            (int)(rect.bottom()  + RECT_TOLERANCE+ VERTICAL_SPACING/2)
                    );

                    int rowInCheck = rowIdx;

                    // find the next unselected dot (rectangle)
                    while (++rowInCheck < MAX_ROW_COUNT && mRectangles.get(rowInCheck).get(colIdx).isSelected()) ;


                    // if something was found
                    // if rect is not on the top row
                    // if (rowInCheck < MAX_ROW_COUNT && !(mRectangles.get(rowInCheck).get(colIdx).isMarked())) {
                    if (rowInCheck < MAX_ROW_COUNT ) {
                        // upper unselected rectangle
                        // still a black dot
                        List<Rectangle> upperRectRow = mRectangles.get(rowInCheck);
                        Rectangle topUnselected = upperRectRow.get(colIdx);

                        //////////////////////////////////////////////////////////////
                        Rect topRect = new Rect(
                                (int)(topUnselected.left() - RECT_TOLERANCE -HORIZONTAL_SPACING/2),
                                (int)(topUnselected.top() - RECT_TOLERANCE - VERTICAL_SPACING/2),
                                (int)(topUnselected.right() + RECT_TOLERANCE+ HORIZONTAL_SPACING/2),
                                (int)(topUnselected.bottom() + RECT_TOLERANCE+ VERTICAL_SPACING/2)
                        );

                        // CLEAR PREVIOUS LOCATION (OF TOP-UNSELECTED DOT)
                        canvas = mSurfaceHolder.lockCanvas(topRect);
                        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
                        //canvas.drawRect(topRect, mWhitePaint);
                        mSurfaceHolder.unlockCanvasAndPost(canvas);

                        try {
                            Thread.sleep(0);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        // DRAW DOT AT NEW POSITION
                        mColorPaint.setColor(topUnselected.dotColor().colorInfo());
                        canvas = mSurfaceHolder.lockCanvas(bottomRect);
                        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
                        canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                        ///////////////////////////////////////////////////////////////

                        // SWAP IDs
                        rect.swap(topUnselected);
                        markedDots.add(rect);

                        // ADD TO LIST TO BE UPDATED
                        mDotsToBeUpdated.put(rectID, topUnselected);

                        upperRectRow.set(colIdx, topUnselected);
                        mRectangles.set(rowInCheck, upperRectRow);
                    }else{

                        mDotsToBeUpdated.put(rectID, rect);

                        // ERASE DOT
                        canvas = mSurfaceHolder.lockCanvas(bottomRect);
                        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    rectRow.set(colIdx, rect);
                    mRectangles.set(rowIdx, rectRow);
                }
            }//////////////////////////// end for every column block

//            Collections.sort(markedDots, Rectangle.rowComparator());
//
//            int i = 0, len = markedDots.size();
//
//            while(i < len){
//                int j = i+1;
//                while(j < len && (markedDots.get(i).row() == markedDots.get(j).row())){
//                    j++;
//                }
//
//
//                System.out.println("diff: " + (j - i));
//                for(int k = i; k < j; k++){
//                    Rect rect = new Rect(
//                            (int)(markedDots.get(k).left() - RECT_TOLERANCE - HORIZONTAL_SPACING/2),
//                            (int)(markedDots.get(k).top() - RECT_TOLERANCE - VERTICAL_SPACING/2),
//                            (int)(markedDots.get(k).right() + RECT_TOLERANCE+ HORIZONTAL_SPACING/2),
//                            (int)(markedDots.get(k).bottom()  + RECT_TOLERANCE+ VERTICAL_SPACING/2)
//                    );
//                    System.out.println(markedDots.get(k).row() + ", " + markedDots.get(k).column());
//                    canvas = mSurfaceHolder.lockCanvas(rect);
//                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
//                    mColorPaint.setColor(markedDots.get(k).dotColor().colorInfo());
//                    canvas.drawCircle(markedDots.get(k).getX(), markedDots.get(k).getY(), RADIUS, mColorPaint);
//                    mSurfaceHolder.unlockCanvasAndPost(canvas);
//                }
//                i = j;
//
//                try {
//                    Thread.sleep(TIME_DELAY*8);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
        }




        //==================================================================================
        // ADD NEW NODES
        //==================================================================================

        System.out.print("\n------------PHASE 2 ----------------\n");
        printRects();
//        try {
//            Thread.sleep(TIME_DELAY*200);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }


        // Update Header and Footer Information
        // Information includes number of movesLeft left, int_score and number of dots left
        canvas = mSurfaceHolder.lockCanvas(null);
        drawHeaderAndFooter(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);

        synchronized (mDotsToBeUpdated){
            // SORT
            ArrayList<Rectangle> tempList = new ArrayList<>(mDotsToBeUpdated.size());
            for(int i = 0, count = mDotsToBeUpdated.size(); i < count; i++){

                mDeletedNodes.add(mDotsToBeUpdated.valueAt(i).toDotNode());
                tempList.add(mDotsToBeUpdated.valueAt(i));

                // update dotColorCounter
                mDotColorCounter.put( mDotsToBeUpdated.valueAt(i).dotColor(),
                       mDotColorCounter.get( mDotsToBeUpdated.valueAt(i).dotColor()) - 1);

            }
            Collections.sort(tempList, Rectangle.rowComparator());


            for(int idx = 0, len = tempList.size(); idx < len; idx++){
                Rectangle rect = tempList.get(idx);
                DotNode node = getNewNode();
                rect.deselect();
                rect.setId(node.id);
                rect.setDotColor(DotColor.valueOf(node.degree));

                ///////////////////////////////////////////////
                mColorPaint.setColor(rect.dotColor().colorInfo());
                Rect dirtyRect;
                float centreX = rect.getX();
                float startY = rect.getAnimY();
                while(rect.step()){
                    float centreY = rect.getAnimY();

                    dirtyRect = new Rect((int)(centreX - RADIUS - HORIZONTAL_SPACING/2),
                            (int)(startY - RADIUS - VERTICAL_SPACING/2),
                            (int)(centreX + RADIUS + HORIZONTAL_SPACING/2),
                            (int)(centreY + RADIUS + VERTICAL_SPACING/2) );
                    canvas = mSurfaceHolder.lockCanvas(dirtyRect);
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
                    canvas.drawCircle(rect.getX(), rect.getAnimY(), RADIUS, mColorPaint);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);

                    startY = rect.getAnimY();
                    try {
                        Thread.sleep(0);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                dirtyRect = new Rect((int)(centreX - RADIUS - HORIZONTAL_SPACING/2), (int)(startY - RADIUS - VERTICAL_SPACING/2),
                        (int)(centreX + RADIUS + HORIZONTAL_SPACING/2), (int)(rect.getY() + RADIUS + VERTICAL_SPACING/2) );
                canvas = mSurfaceHolder.lockCanvas(dirtyRect);
                canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);

                try {
                    Thread.sleep(2);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                //////////////////////////////////////////////////
            }
        }


        Log.i(TAG, mStartRect.dotColor().toString());
        mGameLevel.updateScore(mSelectedDotColor, mSelectedDots.size());

        // Reset state
        mState = STATE.RESET;
    }

    public synchronized void onActionMove(){
        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        //==========================================================
        // DRAW & DECORATE DOTS
        //==========================================================
        if(mStartRect != null && mGraphNodes != null){
            for(List<Rectangle> list: mRectangles){
                for(Rectangle rect: list){
                    if(rect.id() != mStartRect.id()){
                        if(mDataLoader.checkEdge(mStartRect.id(), rect.id(), mEdges)){
                            canvas.drawCircle( rect.getX(),  rect.getY(), RADIUS, mColorPaint);
                        }else{
                            canvas.drawCircle( rect.getX(),  rect.getY(), RADIUS, mBlackPaint);
                        }
                    } else {
                        canvas.drawCircle( rect.getX(),  rect.getY(), RADIUS, mColorPaint);
                        //canvas.drawCircle( rect.centreX(),  rect.centreY(), RADIUS - 20, mBlackPaint);
                    }
                }
            }

        }

        //==========================================================
        // DRAW LINE or PATH
        //==========================================================
        float dx = Math.abs(mx - mStartX);
        float dy = Math.abs(my - mStartY);

        // draw line connections
        synchronized (mLines){
            for(Line line: mLines){
                canvas.drawLine(line.startX, line.startY, line.endX, line.endY, mColorPathPaint);
            }
        }


        if (mStartRect != null && (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) ){
            canvas.drawLine(mStartX, mStartY, mx, my, mColorPathPaint);
            last_mx = mx;
            last_my = my;
        }

        drawHeaderAndFooter(canvas);

        // display
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    public synchronized void onActionReset(){

        Canvas canvas = mSurfaceHolder.lockCanvas(null);
        canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

        drawHeaderAndFooter(canvas);


        //=====================================================
        // REDRAW DOTS
        //=====================================================
        for(List<Rectangle> rectRow: mRectangles){
            for(Rectangle rect: rectRow){
                mColorPaint.setColor(rect.dotColor().colorInfo());
                canvas.drawCircle(rect.getX(), rect.getY(), RADIUS, mColorPaint);
            }
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);

        //=====================================================
        // RESET
        //=====================================================
        Log.i(TAG,"SELECTED DOTS: " + mSelectedDots.size());
        Log.i(TAG,"UPDATED DOTS: " + mDotsToBeUpdated.size());

        for(int i = MAX_ROW_COUNT - 1; i >= 0; i--){
            List<Rectangle> list = mRectangles.get(i);
            for(Rectangle rect: list){
                rect.deselect();
            }
            System.out.print("\n");
        }

        mDotsToBeUpdated.clear();
        mLines.clear();
        mSelectedDots.clear();
        mStartRect =  null;
        mState = STATE.DO_NOTHING;


        if(mGameOverListener != null){
            if(mGameLevel.succeeded() || mGameLevel.failed()){
                mGameOverListener.gameOver(mGameLevel);
            }
        }

        printRects();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Draw Graph
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void setupGame(){
        headerRect = new Rect(0, 0, getMeasuredWidth(), HEADER_FOOTER_HEIGHT);
        footerRect = new Rect(0, getMeasuredHeight() - HEADER_FOOTER_HEIGHT, getMeasuredWidth(), getMeasuredHeight());
        scoreRect = new Rect(0, 0, SCORE_TEXT_DIM, SCORE_TEXT_DIM);
        movesRect = new Rect(getMeasuredWidth() - SCORE_TEXT_DIM, 0, getMeasuredWidth(), SCORE_TEXT_DIM);

        Canvas canvas;

        boolean doneDrawing = false;

        int currentRow = 5; // start with the last row
        float endY = loadRow(currentRow);
        float startY = ANIM_START_POS;     // current height of the rows of dots
        float stepSize = (endY - startY)/2;  // change in Y

        while(!doneDrawing){
            //==========================================
            // TEMP DRAWING
            //========================================
            canvas = mSurfaceHolder.lockCanvas(null);
            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);

            drawHeaderAndFooter(canvas);

            for(Circle circle: mCircles){
                mColorPaint.setColor(circle.color());
                canvas.drawCircle(circle.centreX(), circle.centreY(), RADIUS, mColorPaint);
            }
            for(Circle circle: mBufferCircles){
                mColorPaint.setColor(circle.color());
                canvas.drawCircle(circle.centreX(), startY, RADIUS, mColorPaint);
            }
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            try {
                Thread.sleep(TIME_DELAY * 2);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            /////////////////////////////////////////


            startY += stepSize;
            if(startY >= endY){
                startY = endY;

                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC_OVER);
                drawHeaderAndFooter(canvas);
                mCircles.addAll(mBufferCircles);
                for(Circle circle: mCircles){
                    mColorPaint.setColor(circle.color());
                    canvas.drawCircle(circle.centreX(), circle.centreY(), RADIUS, mColorPaint);
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);


                try {
                    Thread.sleep(TIME_DELAY * 8);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                if(--currentRow > -1){
                    startY = ANIM_START_POS;
                    endY = loadRow(currentRow);
                    stepSize = (endY - startY)/2;
                }else{
                    doneDrawing = true;
                    mInitialized = true;
                }
            }
        }
    }

    private int loadRow(int row){
        List<Rectangle> newRow = new ArrayList<>();
        int x = (getMeasuredWidth() - DIMENSION)/2 ;
        int y = (getMeasuredHeight() - DIMENSION)/2 + row*(2* RADIUS + VERTICAL_SPACING);
        mBufferCircles = new ArrayList<>();
        for (int i = row*MAX_COLUMN_COUNT, count = i+MAX_COLUMN_COUNT, col =0; i < count ; i++, col++) {

            DotNode node = mGraphNodes.get(i);

            mBufferCircles.add(new Circle(x,y, RADIUS,node.id, DotColor.colorInfo(node.degree)));

            // Define new Rectangle
            newRow.add( new Rectangle(
                            x - RADIUS - RECT_TOLERANCE,
                            y - RADIUS - RECT_TOLERANCE ,
                            x + RADIUS + RECT_TOLERANCE,
                            y + RADIUS + RECT_TOLERANCE,
                               node,  row, col
                    ));
            newRow.get(newRow.size() - 1).setAnimStartY(ANIM_START_POS);

            // increase column
            x += 2* RADIUS + HORIZONTAL_SPACING;
        }
        mRectangles.push(newRow);
        return y;
    }

    private DotNode getNewNode(){
        DotNode node = null;


        while (!mBufferNodes.isEmpty()){
            node = mBufferNodes.get(mBufferNodes.size() - 1);
            mBufferNodes.remove(mBufferNodes.size() - 1);

            if(isDataValid(node.degree)){
                break;
            }
            mDeletedNodes.add(node);
        }

        if (mBufferNodes.isEmpty()){
            // If all nodes in the buffer container have been exhausted, ...
            // ... recycle the old deleted nodes.
            Log.i(TAG, "Recycled");
            Log.i(TAG, "Before: " + mBufferNodes.size() + ", " + mDeletedNodes.size());

            Collections.shuffle(mDeletedNodes);
            mBufferNodes = new ArrayList<>(mDeletedNodes);
            mDeletedNodes.clear();

            Log.i(TAG, "After " + mBufferNodes.size() + ", " + mDeletedNodes.size());
            while (!mBufferNodes.isEmpty()){
                node = mBufferNodes.get(mBufferNodes.size() - 1);
                mBufferNodes.remove(mBufferNodes.size() - 1);
                if(isDataValid(node.degree)){
                    break;
                }
                mDeletedNodes.add(node);
            }
        }

        if(node == null){
            Log.i(TAG, "I am null");
        }
        return node;
    }


    private void drawHeaderAndFooter(Canvas canvas){

        //////////////////////////////////////////////
        //                                          //
        // DRAW HEADER                              //
        //                                          //
        //////////////////////////////////////////////
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);
        canvas.drawRect(headerRect, mHeaderFooterPaint);

        int numOfDots = 1;
        EnumMap<DotColor, Integer> objective = null;
        EnumMap<DotColor, Integer> score = null;
        if(mGameLevel != null){
            objective = mGameLevel.getObjective();
            score = mGameLevel.getScore();
            numOfDots = objective.size();
        }

        float startX = getMeasuredWidth() -
                (numOfDots* RADIUS_SMALL *2 + HEADER_DOTS_SPACING *(numOfDots -1));
        float startY = (RADIUS_SMALL + VERTICAL_PADDING + HEADER_DOTS_SPACING);

        startX /= 2;
        startX += RADIUS_SMALL;

        mTextPaint.setTextSize(TEXT_SIZE_MEDIUM);
        mTextPaint.setColor(TEXT_COLOR);

        if(objective != null){
            for(DotColor dotColor: objective.keySet()){
                mColorPaint.setColor(dotColor.colorInfo());
                canvas.drawCircle(startX, startY, RADIUS_SMALL, mColorPaint);

                if(score.get(dotColor) < objective.get(dotColor)){
                    String string = score.get(dotColor) + " / " + objective.get(dotColor);
                    canvas.drawText(string, startX, startY + 100, mTextPaint);
                }else{
                    // draw tick mark
                    float posY = startY + RADIUS_SMALL*0.4f;
                    float x = RADIUS_SMALL*(float)Math.cos(Math.PI/3);
                    float y = RADIUS_SMALL*(float)Math.sin(Math.PI/3);
                    canvas.drawLine(startX, posY, startX + x, posY  - y, mWhitePathPaint);
                    x = .5f*RADIUS_SMALL*(float)Math.cos(Math.PI/6);
                    y = .5f*RADIUS_SMALL*(float)Math.sin(Math.PI/6);
                    canvas.drawLine(startX, posY, startX - x, posY  - y, mWhitePathPaint);
                }
                startX += RADIUS_SMALL *2 + HEADER_DOTS_SPACING;
            }
        }

        // Header Corners
        mHeaderFooterPaint.setColor(SCORE_COLOR);
        canvas.drawRect(scoreRect, mHeaderFooterPaint);
        canvas.drawRect(movesRect, mHeaderFooterPaint);


        // draw Score
        mTextPaint.setColor(TEXT_MOVES_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_LARGE);
        canvas.drawText(String.valueOf(mGameLevel.score()), scoreRect.centerX(), scoreRect.centerY() + TEXT_SIZE_LARGE/3, mTextPaint);
        mTextPaint.setColor(TEXT_MOVES_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_SMALL);
        canvas.drawText("SCORE", scoreRect.centerX(), scoreRect.centerY()+ TEXT_SIZE_LARGE/3 + 60, mTextPaint);


        // draw Moves
        mTextPaint.setColor(TEXT_MOVES_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_LARGE);
        canvas.drawText(String.valueOf(mGameLevel.movesLeft()), movesRect.centerX(), movesRect.centerY() + TEXT_SIZE_LARGE/3, mTextPaint);
        mTextPaint.setColor(TEXT_MOVES_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE_SMALL);
        canvas.drawText("MOVES", movesRect.centerX(), movesRect.centerY()+ TEXT_SIZE_LARGE/3 + 60, mTextPaint);



        //////////////////////////////////////////////
        //                                          //
        // DRAW FOOTER                              //
        //                                          //
        //////////////////////////////////////////////
        mHeaderFooterPaint.setColor(HEADER_FOOTER_COLOR);
        canvas.drawRect(footerRect, mHeaderFooterPaint);

        final int width = 900;
        final int radius = RADIUS;
        int spacing = (width - 3*2*radius)/2;
        int posX = (getMeasuredWidth() - width)/2 + radius;
        int posY = getMeasuredHeight() - HEADER_FOOTER_HEIGHT/2;

        // Draw Ability #1
        // Swap red dot.
        mColorPaint.setColor(Color.parseColor("#FF4081"));
        canvas.drawCircle(posX, posY, radius, mColorPaint);
        canvas.drawLine(posX - .6f*radius, posY, posX + .6f*radius, posY, mWhitePathPaint);
        canvas.drawLine(posX , posY- .6f*radius, posX , posY+ .6f*radius, mWhitePathPaint);

        // Draw Ability #2
        // Add additional number of moves
        posX += spacing + 2*radius;
        mColorPaint.setColor(Color.parseColor("#ffb6e9b5"));
        canvas.drawCircle(posX, posY, radius, mColorPaint);
        canvas.drawLine(posX, posY, posX, posY - radius*.7f, mWhitePathPaint);
        float offsetX = .75f*radius*(float)Math.cos(Math.PI/4);
        float offsetY = .75f*radius*(float)Math.sin(Math.PI/4);
        canvas.drawLine(posX, posY, posX + offsetX, posY + offsetY, mWhitePathPaint);

        // Draw Ability #3
        posX += spacing + 2*radius;
        mColorPaint.setColor(Color.LTGRAY);
        canvas.drawCircle(posX, posY, radius, mColorPaint);


        if(mStartRect != null){
            mColorPaint.setColor(mStartRect.dotColor().colorInfo());
        }
    }


    private boolean isDataValid(int degree){
        DotColor key = DotColor.valueOf(degree);
        boolean badDot = true;
        int currentCount = 0;

        if(mGameLevel.getObjective().containsKey(key)){
            badDot = false;
        }

        if(mDotColorCounter.containsKey(key)){
            currentCount = mDotColorCounter.get(key);
        }

        if( (!badDot && !mGameLevel.isDotColorComplete(key) ) || currentCount < Integer.valueOf(DOT_COUNT/7)){
            currentCount++;
            mDotColorCounter.put(key, currentCount);
            return true;
        }
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Debugging
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void printRects(){
        System.out.print("\n---------------------------------------\n");
        for(int i = MAX_ROW_COUNT - 1; i >= 0; i--){
            List<Rectangle> list = mRectangles.get(i);
            for(Rectangle rect: list){
                if(rect.isSelected()) {
                    System.out.print(colorToString(rect.dotColor().colorInfo()) + "* ");
                }else{
                    System.out.print(colorToString(rect.dotColor().colorInfo()) + "  ");
                }
            }
            System.out.print("\n");
        }
        System.out.print("\n--------------------------------------\n");
    }

    private String colorToString(int color){
        if (color == getContext().getColor(R.color.dot_violet))
            return "PUR";
        else if (color == getContext().getColor(R.color.dot_indigo))
            return "IND";
        else if (color == getContext().getColor(R.color.dot_blue))
            return "BLU";
        else if (color == getContext().getColor(R.color.dot_green))
            return "GRE";
        else if (color == getContext().getColor(R.color.dot_yellow))
            return "YEL";
        else if (color == getContext().getColor(R.color.dot_orange))
            return "ORA";
        else if (color == getContext().getColor(R.color.dot_red))
            return "RED";

        return "UNK";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Other Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void resume(){
        if(!mRunning){
            Log.i(TAG, "#0.  THREAD STARTED");
            mRunning = true;
            mGameThread = new Thread(this);
            mGameThread.start();

            // If Surface was created but not destroyed.
            // This happens when the device goes to sleep or the power button
            // is pressed.
            if(mSurfaceCreated && !mSurfaceWasDestroyed){
                mSurfaceReady = true;
            }

            mState = STATE.RESET;
        }
    }


    public void pause(){
        try {
            if(mRunning){
                mRunning = false;
                mSurfaceReady = false;
                mGameThread.join();
                Log.i(TAG, "PAUSED");
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }


    public void close(){
        pause();
        Log.i(TAG, "CLOSED");
        mDataLoader.close();
    }


    public void setGameLevel(int id){
        if(mGameLevel == null){

            LevelDatabaseHelper helper = null;

            try {
                helper = LevelDatabaseHelper.getInstance(getContext());
            } catch (Exception e) {
                throw new Error("Unable to create database");
            }
            finally {
                try {
                    if(helper != null){
                        mGameLevel = helper.getLevel(id);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public interface GameOverListener{
        void gameOver(Level level);
    }

    public void setGameOverListener(GameOverListener listener){
        mGameOverListener = listener;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Interfaces Implemented
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "#1.  SURFACE CREATED");
        mSurfaceReady = true;
        mSurfaceCreated = true;
        mSurfaceWasDestroyed = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "SURFACE DESTROYED");
        mSurfaceWasDestroyed = true;
        if(mRunning) pause();
    }

    @Override
    public void run() {
        //===============================
        // INITIALIZE GAME VIEW
        //===============================
        while (!mSurfaceReady){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("I'm just waiting.");
        }
        Log.i(TAG, "#2.  SURFACE READY");
        if(!mDoneLoadingData){
            mDataLoader.setGraphNumber(mGameLevel.id());
            mDataLoader.loadNodes();
        }
        while(!mDoneLoadingData){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }
        Log.i(TAG, "#3.  DONE LOADING DATA");

        if(!mInitialized){
            setupGame();
        }

        while(!mInitialized){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("I'm just waiting in mInitializer.");
        }
        mReady = true;

        Log.i(TAG, "GAME READY");
        //================================
        // GAME LOOP
        //================================
        while (mRunning){
            try{
                Thread.sleep(3);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            switch (mState){
                case ACTION_DOWN:
                    break;
                case ACTION_UP:
                    onActionUp();
                    break;
                case MOVING:
                    onActionMove();
                    break;
                case RESET:
                    onActionReset();
                    break;
                case DO_NOTHING:
                    break;
            }
        }
        Log.i(TAG, "STOPPED RUNNING");
    }


    @Override
    public boolean isNodeValid(int degree) {
        DotColor key = DotColor.valueOf(degree);
        boolean badDot = true;
        int currentCount = 0;

        if(mGameLevel.getObjective().containsKey(key)){
            badDot = false;
        }

        if(mDotColorCounter.containsKey(key)){
            currentCount = mDotColorCounter.get(key);
        }

        if(!badDot || currentCount < Integer.valueOf(DOT_COUNT/7)){
            currentCount++;
            mDotColorCounter.put(key, currentCount);
            return true;
        }
        return false;
    }

    @Override
    public void onLoad(ArrayList<DotNode> nodes) {
        mGraphNodes = nodes;
        mDoneLoadingData = true;
    }

    @Override
    public void onLoadBuffer(ArrayList<DotNode> nodes) {
        mBufferNodes = nodes;
        Log.i(TAG, "BUFFER SIZE " + mBufferNodes.size());
    }

}