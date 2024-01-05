package dhaliwal.production.memeking.ui.home;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

class PreLoadingLinearLayoutManager extends LinearLayoutManager {
    private int mPages = 1;
    private OrientationHelper mOrientationHelper;


    public PreLoadingLinearLayoutManager(final Context context) {
        super(context);
    }

    public PreLoadingLinearLayoutManager(final Context context, final int pages) {
        super(context);
        this.mPages = pages;
    }

    public PreLoadingLinearLayoutManager(final Context context, final int orientation, final boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void setOrientation(final int orientation) {
        super.setOrientation(orientation);
        mOrientationHelper = null;
    }

    /**
     * Set the number of pages of layout that will be preloaded off-screen,
     * a page being a pixel measure equivalent to the on-screen size of the
     * recycler view.
     *
     * @param pages the number of pages; can be {@code 0} to disable preloading
     */
    public void setPages(final int pages) {
        this.mPages = pages;
    }

    @Override
    protected void calculateExtraLayoutSpace(@NotNull final RecyclerView.State state, @NotNull int[] extraLayoutSpace) {
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, getOrientation());
        }
        int extraScrollSpace = mOrientationHelper.getTotalSpace() * mPages;
        int extraLayoutSpaceStart = 0;
        int extraLayoutSpaceEnd = 0;
        if (getLayoutDirection()== -1){
            extraLayoutSpaceStart = extraScrollSpace;
        }
        else{
            extraLayoutSpaceEnd = extraScrollSpace;
        }
        extraLayoutSpace[0] = extraLayoutSpaceStart;
        extraLayoutSpace[1] = extraLayoutSpaceEnd;


    }
}
