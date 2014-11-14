package eu.se_bastiaan.popcorntimeremote.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import eu.se_bastiaan.popcorntimeremote.utils.PixelUtils;

public class JoystickView extends View {
    // Constants
    private final double RAD = 57.2957795;
    public enum Direction { CENTER, RIGHT, UP, LEFT, DOWN };
    // Variables
    private OnJoystickMoveListener mOnJoystickMoveListener;
    private int mPositionX = 0;
    private int mPositionY = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;
    private Paint mCirclePaint;
    private Paint mButtonPaint;
    private Paint mButtonImagePaint;
    private Bitmap mCenterImage, mLeftImage, mRightImage, mUpImage, mDownImage;
    private float mJoystickRadius;
    private float mButtonRadius;
    private float mOuterCircleRadius;
    private int mLastAngle = 0;
    private int mLastPower = 0;
    private boolean mUserIsTouching = false;
    private boolean mCalledOnce = false;
    private boolean mDrawn = false;
    private Handler mHandler = new Handler();
    private Context mContext;
    private DisplayMetrics mMetrics = new DisplayMetrics();

    public JoystickView(Context context) {
        super(context);
        initJoystickView(context);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView(context);
    }

    public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView(context);
    }

    protected void initJoystickView(Context context) {
        mContext = context;

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.parseColor("#444C53"));
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(PixelUtils.getPixelsFromDp(mContext, 2));
        mCirclePaint.setAntiAlias(true);

        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setColor(Color.parseColor("#1976D2"));
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setAntiAlias(true);

        mButtonImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    @Override
    protected void onFinishInflate() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int widthPixels = mMetrics.widthPixels;

        setMeasuredDimension(widthPixels, widthPixels);

        int baseRadius = widthPixels / 2;
        mButtonRadius = baseRadius * 0.1824074074f;
        mJoystickRadius = baseRadius * 0.3009259259f;
        mOuterCircleRadius = baseRadius * 0.7314814815f;
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

        Float centerBetweenRadius = mJoystickRadius + (mOuterCircleRadius - mJoystickRadius) / 1.5f;

        // painting the main circle
        canvas.drawCircle(mCenterX, mCenterY, mJoystickRadius, mCirclePaint);
        canvas.drawCircle(mCenterX, mCenterX, mOuterCircleRadius, mCirclePaint);

        // painting the move button
        if(mUserIsTouching) {
            mButtonPaint.setShadowLayer(2.0f, 0.0f, 0.0f, 0x99000000);
        } else {
            mButtonPaint.setShadowLayer(8.0f, 0.0f, 0.0f, 0x99000000);
        }

        try {
            if (mUpImage != null) {
                canvas.drawBitmap(mUpImage, mCenterX - (mUpImage.getWidth() / 2), mCenterY - centerBetweenRadius - (mCenterImage.getHeight() / 2), mButtonImagePaint);
            }

            if (mDownImage != null) {
                canvas.drawBitmap(mDownImage, mCenterX - (mDownImage.getWidth() / 2), mCenterY + centerBetweenRadius - (mCenterImage.getHeight() / 2), mButtonImagePaint);
            }

            if (mLeftImage != null) {
                canvas.drawBitmap(mLeftImage, mCenterX - centerBetweenRadius - (mLeftImage.getWidth() / 2), mCenterY - (mRightImage.getHeight() / 2), mButtonImagePaint);
            }

            if (mRightImage != null) {
                canvas.drawBitmap(mRightImage, mCenterX + centerBetweenRadius - (mRightImage.getWidth() / 2), mCenterY - (mRightImage.getHeight() / 2), mButtonImagePaint);
            }

            canvas.drawCircle(mPositionX, mPositionY, mButtonRadius, mButtonPaint);

            if (mCenterImage != null) {
                canvas.drawBitmap(mCenterImage, mPositionX - (mCenterImage.getWidth() / 2), mPositionY - (mCenterImage.getHeight() / 2), mButtonImagePaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            mButtonPaint.setAlpha(255);
            mHandler.removeCallbacksAndMessages(null);
            invalidate();
            if(mOnJoystickMoveListener != null && !mCalledOnce) {
                mOnJoystickMoveListener.onValueChanged(getAngle(), getPower(), getDirection());
            }
            mCalledOnce = false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mUserIsTouching = true;
            if(!move) mButtonPaint.setAlpha(190);
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

    public void setJoystickImage(Direction direction, Integer drawableRes) {
        Resources res = getResources();
        Bitmap resource = BitmapFactory.decodeResource(res, drawableRes);
        if(mDrawn) {
            resource = Bitmap.createScaledBitmap(resource, getWidth() * (2 / 3), getHeight() * (2 / 3), false);
        }

        switch (direction) {
            case CENTER:
                mCenterImage = resource;
                break;
            case UP:
                mUpImage = resource;
                break;
            case DOWN:
                mDownImage = resource;
                break;
            case LEFT:
                mLeftImage = resource;
                break;
            case RIGHT:
                mRightImage = resource;
                break;
        }
    }

    public void setJoystickImage(Direction direction, Bitmap bitmap) {
        if(bitmap != null) {
            if(mDrawn) {
                bitmap = Bitmap.createScaledBitmap(bitmap, getWidth() * (2 / 3), getHeight() * (2 / 3), false);
            }

            switch (direction) {
                case CENTER:
                    mCenterImage = bitmap;
                    break;
                case UP:
                    mUpImage = bitmap;
                    break;
                case DOWN:
                    mDownImage = bitmap;
                    break;
                case LEFT:
                    mLeftImage = bitmap;
                    break;
                case RIGHT:
                    mRightImage = bitmap;
                    break;
            }
        } else {
            mCenterImage = null;
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