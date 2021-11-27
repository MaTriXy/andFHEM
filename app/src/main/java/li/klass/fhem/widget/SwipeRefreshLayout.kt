package li.klass.fhem.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class SwipeRefreshLayout extends androidx.swiperefreshlayout.widget.SwipeRefreshLayout {
    private int mTouchSlop;

    private float mDownX;
    private boolean mHorizontalSwipe;
    private ChildScrollDelegate mChildScrollDelegate;

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setChildScrollDelegate(ChildScrollDelegate delegate) {
        mChildScrollDelegate = delegate;
    }

    @Override
    public boolean canChildScrollUp() {
        return super.canChildScrollUp()
                || (mChildScrollDelegate != null
                && mChildScrollDelegate.canChildScrollUp());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mHorizontalSwipe = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mDownX);

                if (mHorizontalSwipe || xDiff > mTouchSlop) {
                    mHorizontalSwipe = true;
                    return false;
                }
                break;
        }

        return super.onInterceptTouchEvent(event);
    }

    public interface ChildScrollDelegate {
        boolean canChildScrollUp();
    }
}
