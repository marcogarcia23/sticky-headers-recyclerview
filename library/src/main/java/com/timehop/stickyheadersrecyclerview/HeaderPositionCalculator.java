package com.timehop.stickyheadersrecyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.timehop.stickyheadersrecyclerview.caching.HeaderProvider;
import com.timehop.stickyheadersrecyclerview.calculation.DimensionCalculator;

/**
 * Calculates the position and location of header views
 */
public class HeaderPositionCalculator {

    private final StickyRecyclerHeadersAdapter mAdapter;
    private final HeaderProvider mHeaderProvider;
    private final DimensionCalculator mDimensionCalculator;

    /**
     * The following fields are used as buffers for internal calculations. Their sole purpose is to avoid
     * allocating new Rect every time we need one.
     */
    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();

    public HeaderPositionCalculator(StickyRecyclerHeadersAdapter adapter, HeaderProvider headerProvider, DimensionCalculator dimensionCalculator) {
        mAdapter = adapter;
        mHeaderProvider = headerProvider;
        mDimensionCalculator = dimensionCalculator;
    }

    /**
     * Determines if a view should have a sticky header.
     * The view has a sticky header if:
     * 1. It is the first element in the recycler view
     * 2. It has a valid ID associated to its position
     *
     * @param itemView    given by the RecyclerView
     * @param orientation of the Recyclerview
     * @param position    of the list item in question
     * @return True if the view should have a sticky header
     */
    public boolean hasStickyHeader(View itemView, int orientation, int position) {
        int offset, margin;
        mDimensionCalculator.initMargins(mTempRect1, itemView);
        if (orientation == LinearLayout.VERTICAL) {
            offset = itemView.getTop();
            margin = mTempRect1.top;
        } else {
            offset = itemView.getLeft();
            margin = mTempRect1.left;
        }

        return offset <= margin && mAdapter.getHeaderId(position) >= 0;
    }

    /**
     * Determines if an item in the list should have a header that is different than the item in the
     * list that immediately precedes it. Items with no headers will always return false.
     *
     * @param position of the list item in questions
     * @return true if this item has a different header than the previous item in the list
     * @see {@link StickyRecyclerHeadersAdapter#getHeaderId(int)}
     */
    public boolean hasNewHeader(int position) {
        if (indexOutOfBounds(position)) {
            return false;
        }

        long headerId = mAdapter.getHeaderId(position);

        if (headerId < 0) {
            return false;
        }

        long nextItemHeaderId = -1;
        int nextItemPosition = position - 1;
        if (!indexOutOfBounds(nextItemPosition)) {
            nextItemHeaderId = mAdapter.getHeaderId(nextItemPosition);
        }
        int firstItemPosition = 0;

        return position == firstItemPosition || headerId != nextItemHeaderId;
    }

    private boolean indexOutOfBounds(int position) {
        return position < 0 || position >= mAdapter.getItemCount();
    }

    public void initHeaderBounds(Rect bounds, RecyclerView recyclerView, View header, View firstView, boolean firstHeader) {
        initDefaultHeaderOffset(bounds, recyclerView, header, firstView);

        if (firstHeader && isStickyHeaderBeingPushedOffscreen(recyclerView, header)) {
            View viewAfterNextHeader = getFirstViewUnobscuredByHeader(recyclerView, header);
            int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterNextHeader);
            View secondHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
            translateHeaderWithNextHeader(recyclerView, bounds, header, viewAfterNextHeader, secondHeader);
        }
    }

    private void initDefaultHeaderOffset(Rect headerMargins, RecyclerView recyclerView, View header, View firstView) {
        int translationX, translationY;
        mDimensionCalculator.initMargins(mTempRect1, header);
        translationX = firstView.getLeft() + mTempRect1.left;
        translationY = Math.max(
                firstView.getTop() - header.getHeight() - mTempRect1.bottom,
                getListTop(recyclerView) + mTempRect1.top);

        headerMargins.set(translationX, translationY, translationX + header.getWidth(),
                translationY + header.getHeight());
    }

    private boolean isStickyHeaderBeingPushedOffscreen(RecyclerView recyclerView, View stickyHeader) {
        View viewAfterHeader = getFirstViewUnobscuredByHeader(recyclerView, stickyHeader);
        int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterHeader);
        if (firstViewUnderHeaderPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        if (firstViewUnderHeaderPosition > 0 && hasNewHeader(firstViewUnderHeaderPosition)) {
            View nextHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
            mDimensionCalculator.initMargins(mTempRect1, nextHeader);
            mDimensionCalculator.initMargins(mTempRect2, stickyHeader);

            int topOfNextHeader = viewAfterHeader.getTop() - mTempRect1.bottom - nextHeader.getHeight() - mTempRect1.top;
            int bottomOfThisHeader = recyclerView.getPaddingTop() + stickyHeader.getBottom() + mTempRect2.top + mTempRect2.bottom;
            if (topOfNextHeader < bottomOfThisHeader) {
                return true;
            }
        }

        return false;
    }

    private void translateHeaderWithNextHeader(RecyclerView recyclerView, Rect translation,
                                               View currentHeader, View viewAfterNextHeader, View nextHeader) {
        mDimensionCalculator.initMargins(mTempRect1, nextHeader);
        mDimensionCalculator.initMargins(mTempRect2, currentHeader);
        int topOfStickyHeader = getListTop(recyclerView) + mTempRect2.top + mTempRect2.bottom;
        int shiftFromNextHeader = viewAfterNextHeader.getTop() - nextHeader.getHeight() - mTempRect1.bottom - mTempRect1.top - currentHeader.getHeight() - topOfStickyHeader;
        if (shiftFromNextHeader < topOfStickyHeader) {
            translation.top += shiftFromNextHeader;
        }
    }

    /**
     * Returns the first item currently in the RecyclerView that is not obscured by a header.
     *
     * @param parent Recyclerview containing all the list items
     * @return first item that is fully beneath a header
     */
    private View getFirstViewUnobscuredByHeader(RecyclerView parent, View firstHeader) {
        int step = 1;
        int from = 0;
        for (int i = from; i >= 0 && i <= parent.getChildCount() - 1; i += step) {
            View child = parent.getChildAt(i);
            if (!itemIsObscuredByHeader(parent, child, firstHeader)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Determines if an item is obscured by a header
     *
     * @param parent
     * @param item   to determine if obscured by header
     * @param header that might be obscuring the item
     * @return true if the item view is obscured by the header view
     */
    private boolean itemIsObscuredByHeader(RecyclerView parent, View item, View header) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) item.getLayoutParams();
        mDimensionCalculator.initMargins(mTempRect1, header);

        int adapterPosition = parent.getChildAdapterPosition(item);
        if (adapterPosition == RecyclerView.NO_POSITION || mHeaderProvider.getHeader(parent, adapterPosition) != header) {
            // Resolves https://github.com/timehop/sticky-headers-recyclerview/issues/36
            // Handles an edge case where a trailing header is smaller than the current sticky header.
            return false;
        }

        int itemTop = item.getTop() - layoutParams.topMargin;
        int headerBottom = header.getBottom() + mTempRect1.bottom + mTempRect1.top;
        if (itemTop > headerBottom) {
            return false;
        }

        return true;
    }

    private int getListTop(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingTop();
        } else {
            return 0;
        }
    }

    private int getListLeft(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingLeft();
        } else {
            return 0;
        }
    }
}
