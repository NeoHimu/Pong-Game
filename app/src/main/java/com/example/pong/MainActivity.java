package com.example.pong;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {
    // pongView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    PongView pongView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();

        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        // Initialize pongView and set it as the view
        pongView = new PongView(this, size.x, size.y);
        setContentView(pongView);
    }
    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the pongView resume method to execute
        pongView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the pongView pause method to execute
        pongView.pause();
    }


    public class Ball {

        private RectF mRect;
        private float mXVelocity;
        private float mYVelocity;
        private float mBallWidth;
        private float mBallHeight;

        public Ball(int screenX, int screenY){

            // Make the mBall size relative to the screen resolution
            mBallWidth = screenX / 100;
            mBallHeight = mBallWidth;

            /*
                Start the ball travelling straight up
                at a quarter of the screen height per second
            */
            mYVelocity = screenY / 4;
            mXVelocity = mYVelocity;

            // Initialize the Rect that represents the mBall
            mRect = new RectF();

        }

        // Give access to the Rect
        public RectF getRect(){
            return mRect;
        }

        // Change the position each frame
        public void update(long fps){
            mRect.left = mRect.left + (mXVelocity / fps);
            mRect.top = mRect.top + (mYVelocity / fps);
            mRect.right = mRect.left + mBallWidth;
            mRect.bottom = mRect.top - mBallHeight;
        }

        // Reverse the vertical heading
        public void reverseYVelocity(){
            mYVelocity = -mYVelocity;
        }

        // Reverse the horizontal heading
        public void reverseXVelocity(){
            mXVelocity = -mXVelocity;
        }

        public void setRandomXVelocity(){

            // Generate a random number either 0 or 1
            Random generator = new Random();
            int answer = generator.nextInt(2);

            if(answer == 0){
                reverseXVelocity();
            }
        }

        // Speed up by 10%
        // A score of over 20 is quite difficult
        // Reduce or increase 10 to make this easier or harder
        public void increaseVelocity(){
            mXVelocity = mXVelocity + mXVelocity / 10;
            mYVelocity = mYVelocity + mYVelocity / 10;
        }

        public void clearObstacleY(float y){
            mRect.bottom = y;
            mRect.top = y - mBallHeight;
        }

        public void clearObstacleX(float x){
            mRect.left = x;
            mRect.right = x + mBallWidth;
        }

        public void reset(int x, int y){
            mRect.left = x / 2;
            mRect.top = y - 20;
            mRect.right = x / 2 + mBallWidth;
            mRect.bottom = y - 20 - mBallHeight;
        }
    }

    public class Bat {

        // RectF is an object that holds four coordinates - just what we need
        private RectF mRect;

        // How long and high our mBat will be
        private float mLength;
        private float mHeight;

        // X is the far left of the rectangle which forms our mBat
        private float mXCoord;

        // Y is the top coordinate
        private float mYCoord;

        // This will hold the pixels per second speed that
        // the mBat will move
        private float mBatSpeed;

        // Which ways can the mBat move
        public final int STOPPED = 0;
        public final int LEFT = 1;
        public final int RIGHT = 2;

        // Is the mBat moving and in which direction
        private int mBatMoving = STOPPED;

        // The screen length and width in pixels
        private int mScreenX;
        private int mScreenY;

        // This is the constructor method
// When we create an object from this class we will pass
// in the screen width and mHeight
        public Bat(int x, int y){

            mScreenX = x;
            mScreenY = y;

            // 1/8 screen width wide
            mLength = mScreenX / 8;

            // 1/25 screen mHeight high
            mHeight = mScreenY / 25;

            // Start mBat in roughly the sceen centre
            mXCoord = mScreenX / 2;
            mYCoord = mScreenY - 20;

            mRect = new RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + mHeight);

            // How fast is the mBat in pixels per second
            mBatSpeed = mScreenX;
            // Cover entire screen in 1 second
        }

        // This is a getter method to make the rectangle that
        // defines our bat available in PongView class
        public RectF getRect(){
            return mRect;
        }

        // The setMovementState method receives a int value as a parameter. We will call this method using one of the three public final int members, LEFT, RIGHT or STOPPED. This method will simply set that state to the mBatMoving member.
        // This method will be used to change/set if the mBat is going
        // left, right or nowhere

        public void setMovementState(int state){
            mBatMoving = state;
        }

        // This update method will be called from update in PongView
// It determines if the Bat needs to move and changes the coordinates
// contained in mRect if necessary
        public void update(long fps){

            if(mBatMoving == LEFT){
                mXCoord = mXCoord - mBatSpeed / fps;
            }

            if(mBatMoving == RIGHT){
                mXCoord = mXCoord + mBatSpeed / fps;
            }

            // Make sure it's not leaving screen
            if(mRect.left < 0){ mXCoord = 0; } if(mRect.right > mScreenX){
                mXCoord = mScreenX -
                        // The width of the Bat
                        (mRect.right - mRect.left);
            }

            // Update the Bat graphics
            mRect.left = mXCoord;
            mRect.right = mXCoord + mLength;
        }
    }


    class PongView extends SurfaceView implements Runnable {

        // This is our thread
        Thread mGameThread = null;

        // We need a SurfaceHolder object
        // We will see it in action in the draw method soon.
        SurfaceHolder mOurHolder;

        // A boolean which we will set and unset
        // when the game is running- or not
        // It is volatile because it is accessed from inside and outside the thread
        volatile boolean mPlaying;

        // Game is mPaused at the start
        boolean mPaused = true;

        // A Canvas and a Paint object
        Canvas mCanvas;
        Paint mPaint;

        // This variable tracks the game frame rate
        long mFPS;

        // The size of the screen in pixels
        int mScreenX;
        int mScreenY;

        // The players mBat
        Bat mBat;

        // A mBall
        Ball mBall;

        // For sound FX
        SoundPool sp;
        int beep1ID = -1;
        int beep2ID = -1;
        int beep3ID = -1;
        int loseLifeID = -1;
        int explodeID = -1;

        // The mScore
        int mScore = 0;

        // Lives
        int mLives = 3;

        public PongView(Context context, int x, int y) {

    /*
        The next line of code asks the
        SurfaceView class to set up our object.
    */
            super(context);

            // Set the screen width and height
            mScreenX = x;
            mScreenY = y;

            // Initialize mOurHolder and mPaint objects
            mOurHolder = getHolder();
            mPaint = new Paint();

            // A new mBat
            mBat = new Bat(mScreenX, mScreenY);

            // Create a mBall
            mBall = new Ball(mScreenX, mScreenY);

    /*
        Instantiate our sound pool
        dependent upon which version
        of Android is present
    */

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                sp = new SoundPool.Builder()
                        .setMaxStreams(5)
                        .setAudioAttributes(audioAttributes)
                        .build();

            } else {
                sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            }


            try{
                // Create objects of the 2 required classes
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                // Load our fx in memory ready for use
                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = sp.load(descriptor, 0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = sp.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = sp.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = sp.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = sp.load(descriptor, 0);

            }catch(IOException e){
                // Print an error message to the console
                Log.e("error", "failed to load sound files");
            }

            setupAndRestart();

        }

        public void setupAndRestart(){

            // Put the mBall back to the start
            mBall.reset(mScreenX, mScreenY);

            // if game over reset scores and mLives
            if(mLives == 0) {
                mScore = 0;
                mLives = 3;
            }

        }

        @Override
        public void run() {
            while (mPlaying) {

                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                // Update the frame
                if(!mPaused){
                    update();
                }

                // Draw the frame
                draw();

        /*
            Calculate the FPS this frame
            We can then use the result to
            time animations in the update methods.
        */
                long timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    mFPS = 1000 / timeThisFrame;
                }

            }

        }

        // Everything that needs to be updated goes in here
// Movement, collision detection etc.
        public void update() {

            // Move the mBat if required
            mBat.update(mFPS);

            mBall.update(mFPS);

            // Check for mBall colliding with mBat
            if(RectF.intersects(mBat.getRect(), mBall.getRect())) {
                mBall.setRandomXVelocity();
                mBall.reverseYVelocity();
                mBall.clearObstacleY(mBat.getRect().top - 2);

                mScore++;
                mBall.increaseVelocity();

                sp.play(beep1ID, 1, 1, 0, 0, 1);
            }

            // Bounce the mBall back when it hits the bottom of screen
            if(mBall.getRect().bottom > mScreenY){
                mBall.reverseYVelocity();
                mBall.clearObstacleY(mScreenY - 2);

                // Lose a life
                mLives--;
                sp.play(loseLifeID, 1, 1, 0, 0, 1);

                if(mLives == 0){
                    mPaused = true;
                    setupAndRestart();
                }
            }

            // Bounce the mBall back when it hits the top of screen
            if(mBall.getRect().top < 0){
                mBall.reverseYVelocity();
                mBall.clearObstacleY(12);

                sp.play(beep2ID, 1, 1, 0, 0, 1);
            }

            // If the mBall hits left wall bounce
            if(mBall.getRect().left < 0){
                mBall.reverseXVelocity();
                mBall.clearObstacleX(2);

                sp.play(beep3ID, 1, 1, 0, 0, 1);
            }

            // If the mBall hits right wall bounce
            if(mBall.getRect().right > mScreenX){
                mBall.reverseXVelocity();
                mBall.clearObstacleX(mScreenX - 22);

                sp.play(beep3ID, 1, 1, 0, 0, 1);
            }
        }

        public void draw(){
            // Make sure our drawing surface is valid or we crash
            if (mOurHolder.getSurface().isValid()) {

                // Draw everything here

                // Lock the mCanvas ready to draw
                mCanvas = mOurHolder.lockCanvas();

                // Clear the screen with my favorite color
                mCanvas.drawColor(Color.argb(255, 120, 197, 87));

                // Choose the brush color for drawing
                mPaint.setColor(Color.argb(255, 255, 255, 255));

                // Draw the mBat
                mCanvas.drawRect(mBat.getRect(), mPaint);

                // Draw the mBall
                mCanvas.drawRect(mBall.getRect(), mPaint);


                // Change the drawing color to white
                mPaint.setColor(Color.argb(255, 255, 255, 255));

                // Draw the mScore
                mPaint.setTextSize(40);
                mCanvas.drawText("Score: " + mScore + "   Lives: " + mLives, 10, 50, mPaint);

                // Draw everything to the screen
                mOurHolder.unlockCanvasAndPost(mCanvas);
            }

        }
        // If the Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            mPlaying = false;
            try {
                mGameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If the Activity starts/restarts
        // start our thread.
        public void resume() {
            mPlaying = true;
            mGameThread = new Thread(this);
            mGameThread.start();
        }

        // The SurfaceView class implements onTouchListener
// So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    mPaused = false;

                    // Is the touch on the right or left?
                    if(motionEvent.getX() > mScreenX / 2){
                        mBat.setMovementState(mBat.RIGHT);
                    }
                    else{
                        mBat.setMovementState(mBat.LEFT);
                    }

                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    mBat.setMovementState(mBat.STOPPED);
                    break;
            }
            return true;
        }
    }
}
