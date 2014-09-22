package eu.se_bastiaan.popcorntimeremote.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import eu.se_bastiaan.popcorntimeremote.R;

public class JoystickView extends View {
    // Constants
    private final double RAD = 57.2957795;
    public enum Direction { CENTER, RIGHT, UP, LEFT, DOWN};
    // Variables
    private OnJoystickMoveListener mOnJoystickMoveListener;
    private int mPositionX = 0;
    private int mPositionY = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;
    private Paint mMainCircle;
    private Paint mButton;
    private Paint mButtonImagePaint;
    private Bitmap mButtonImage;
    private int mJoystickRadius;
    private int mButtonRadius;
    private int mLastAngle = 0;
    private int mLastPower = 0;
    private boolean mUserIsTouching = false;
    private boolean mCalledOnce = false;
    private boolean mDrawn = false;
    private Handler mHandler = new Handler();

    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView();
    }

    protected void initJoystickView() {
        mMainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainCircle.setColor(Color.parseColor("#77FFFFFF"));
        mMainCircle.setStyle(Paint.Style.STROKE);
        mMainCircle.setAntiAlias(true);

        mButton = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButton.setColor(Color.parseColor("#75B8FF"));
        mButton.setStyle(Paint.Style.FILL);
        mButton.setAntiAlias(true);

        mButtonImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onFinishInflate() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

        setMeasuredDimension(d, d);

        mButtonRadius = (int) ((d / 4) * 0.6);
        mJoystickRadius = (int) d / 4;
    }

    private int measure(int measureSpec) {
        int result;

        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        if(!mDrawn) {
            mPositionX = mCenterX = getWidth() / 2;
            mPositionY = mCenterY = getHeight() / 2;
            mDrawn = true;
        }

        if(!mUserIsTouching) {
            if(mCenterX != mPositionX) {
                if(mCenterX > mPositionX) {
                    mPositionX = mPositionX + 8;
                    if(mPositionX > mCenterX) mPositionX = mCenterX;
                } else {
                    mPositionX = mPositionX - 8;
                    if(mPositionX < mCenterX) mPositionX = mCenterX;
                }
            }
            if(mCenterY != mPositionY) {
                if(mCenterY > mPositionY) {
                    mPositionY = mPositionY + 8;
                    if(mPositionY > mCenterY) mPositionY = mCenterY;
                } else {
                    mPositionY = mPositionY - 8;
                    if(mPositionY < mCenterY) mPositionY = mCenterY;
                }
            }
        }

        // painting the main circle
        canvas.drawCircle(mCenterX, mCenterY, mJoystickRadius, mMainCircle);

        // painting the move button
        if(mUserIsTouching) {
            mButton.setShadowLayer(2.0f, 0.0f, 0.0f, 0x99000000);
        } else {
            mButton.setShadowLayer(8.0f, 0.0f, 0.0f, 0x99000000);
        }

        canvas.drawCircle(mPositionX, mPositionY, mButtonRadius, mButton);

        if(mButtonImage != null) {
            canvas.drawBitmap(mButtonImage, mPositionX - (mButtonImage.getWidth() / 2), mPositionY - (mButtonImage.getHeight() / 2), mButtonImagePaint);
        }

        if(!mUserIsTouching && (mCenterX != mPositionX || mCenterY != mPositionY)) invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double abs = Math.sqrt(Math.pow(event.getX() - mCenterX, 2) + Math.pow(event.getY() - mCenterY, 2));
        boolean move = 100 * abs / mJoystickRadius > 30;

        if(move) {
            mPositionX = (int) event.getX();
            mPositionY = (int) event.getY();
            if (abs > mJoystickRadius) {
                mPositionX = (int) ((mPositionX - mCenterX) * mJoystickRadius / abs + mCenterX);
                mPositionY = (int) ((mPositionY - mCenterY) * mJoystickRadius / abs + mCenterY);
            }
            invalidate();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            mUserIsTouching = false;
            mButton.setAlpha(255);
            mHandler.removeCallbacksAndMessages(null);
            invalidate();
            if(mOnJoystickMoveListener != null && !mCalledOnce) {
                mOnJoystickMoveListener.onValueChanged(getAngle(), getPower(), getDirection());
            }
            mCalledOnce = false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mUserIsTouching = true;
            if(!move) mButton.setAlpha(190);
            if(mOnJoystickMoveListener != null) {
                mHandler.postDelayed(mCallbackRunnable, 200);
            }
            invalidate();
        }

        return true;
    }

    private int getAngle() {
        if (mPositionX > mCenterX) {
            if (mPositionY < mCenterY) {
                return mLastAngle = (int) (Math.atan((mPositionY - mCenterY)
                        / (mPositionX - mCenterX))
                        * RAD + 90);
            } else if (mPositionY > mCenterY) {
                return mLastAngle = (int) (Math.atan((mPositionY - mCenterY)
                        / (mPositionX - mCenterX)) * RAD) + 90;
            } else {
                return mLastAngle = 90;
            }
        } else if (mPositionX < mCenterX) {
            if (mPositionY < mCenterY) {
                return mLastAngle = (int) (Math.atan((mPositionY - mCenterY)
                        / (mPositionX - mCenterX))
                        * RAD - 90);
            } else if (mPositionY > mCenterY) {
                return mLastAngle = (int) (Math.atan((mPositionY - mCenterY)
                        / (mPositionX - mCenterX)) * RAD) - 90;
            } else {
                return mLastAngle = -90;
            }
        } else {
            if (mPositionY <= mCenterY) {
                return mLastAngle = 0;
            } else {
                if (mLastAngle < 0) {
                    return mLastAngle = -180;
                } else {
                    return mLastAngle = 180;
                }
            }
        }
    }

    private int getPower() {
        return mLastPower = (int) (100 * Math.sqrt(Math.pow(mPositionX - mCenterX, 2) + Math.pow(mPositionY - mCenterY, 2)) / mJoystickRadius);
    }

    private Direction getDirection() {
        if (mLastPower == 0 && mLastAngle == 0) {
            return Direction.CENTER;
        }

        int a = 0;
        if (mLastAngle <= 0) {
            a = (mLastAngle * -1) + 90;
        } else if (mLastAngle > 0) {
            if (mLastAngle <= 90) {
                a = 90 - mLastAngle;
            } else {
                a = 360 - (mLastAngle - 90);
            }
        }

        int direction = (a + 45) / 90 + 1;

        if (direction > 4) {
            direction = 1;
        }
        return Direction.values()[direction];
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
        mOnJoystickMoveListener = listener;
    }

    public void setJoystickImage(Integer drawableRes) {
        Resources res = getResources();
        Bitmap resource = BitmapFactory.decodeResource(res, drawableRes);
        if(mDrawn) {
            mButtonImage = Bitmap.createScaledBitmap(resource, getWidth() * (2 / 3), getHeight() * (2 / 3), false);
        } else {
            mButtonImage = resource;
        }
    }

    public void setJoystickImage(Bitmap bitmap) {
        if(bitmap != null) {
            if(mDrawn) {
                mButtonImage = Bitmap.createScaledBitmap(bitmap, getWidth() * (2 / 3), getHeight() * (2 / 3), false);
            } else {
                mButtonImage = bitmap;
            }
        } else {
            mButtonImage = null;
        }
    }

    public void setDirection(Direction direction) {
        switch (direction) {
            case DOWN:
                mPositionX = mCenterX;
                mPositionY = getWidth();
                break;
            case UP:
                mPositionX = mCenterX;
                mPositionY = 0;
                break;
            case LEFT:
                mPositionY = mCenterY;
                mPositionX = 0;
                break;
            case RIGHT:
                mPositionY = mCenterY;
                mPositionX = getWidth();
                break;
        }

        double abs = Math.sqrt((mPositionX - mCenterX) * (mPositionX - mCenterX)
                + (mPositionY - mCenterY) * (mPositionY - mCenterY));
        if (abs > mJoystickRadius) {
            mPositionX = (int) ((mPositionX - mCenterX) * mJoystickRadius / abs + mCenterX);
            mPositionY = (int) ((mPositionY - mCenterY) * mJoystickRadius / abs + mCenterY);
        }

        invalidate();
        if(mOnJoystickMoveListener != null) mOnJoystickMoveListener.onValueChanged(getAngle(), getPower(), direction);
    }

    public static interface OnJoystickMoveListener {
        public void onValueChanged(int angle, int power, Direction direction);
    }

    private Runnable mCallbackRunnable = new Runnable() {
        @Override
        public void run() {
            mCalledOnce = true;
            mOnJoystickMoveListener.onValueChanged(getAngle(), getPower(), getDirection());
            mHandler.postDelayed(this, 500);
        }
    };
}