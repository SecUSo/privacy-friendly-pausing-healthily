package org.secuso.privacyfriendlybreakreminder.activities.helper;
/*
 * Copyright (C) 2014 skyfish.jy@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Matthieu Harlé
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.DataSetObserver;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;

/**
 * Provide a {@link android.support.v7.widget.RecyclerView.Adapter} implementation with cursor
 * support.
 *
 * Child classes only need to implement {@link #onCreateViewHolder(android.view.ViewGroup, int)} and
 * {@link #onBindViewHolder(RecyclerView.ViewHolder, Cursor)}.
 *
 * This class does not implement deprecated fields and methods from CursorAdapter! Incidentally,
 * only {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER} is available, so the
 * flag is implied, and only the Adapter behavior using this flag has been ported.
 *
 * @param <VH> {@inheritDoc}
 *
 * @see android.support.v7.widget.RecyclerView.Adapter
 * @see android.widget.CursorAdapter
 * @see Filterable
 * @see CursorFilter.CursorFilterClient
 */
public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements CursorFilter.CursorFilterClient, Filterable {

    protected Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
    private ChangeObserver mChangeObserver;
    private CursorFilter mCursorFilter;
    private FilterQueryProvider mFilterQueryProvider;
    private boolean mUseIndividualNotifies;

    private String mIdIdentifier = "_id";

    public CursorRecyclerViewAdapter(Context context, Cursor cursor, String idIdentifier) {
        this(context, null);
        this.setIdIdentifier(idIdentifier);
        this.changeCursor(cursor);
    }

    public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex(mIdIdentifier) : -1;

        mDataSetObserver = new NotifyingDataSetObserver();
        mChangeObserver = new ChangeObserver();

        if (mDataValid) {
            if (mChangeObserver != null) cursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) cursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public void setIdIdentifier(String identifier) {
        mIdIdentifier = TextUtils.isEmpty(identifier) ?
                "_id" :
                identifier;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(viewHolder, mCursor);
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        // unregister observers on old cursor
        final Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        // register observers on the new cursor
        mCursor = newCursor;
        if (mCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);

            mRowIdColumn = newCursor.getColumnIndexOrThrow(mIdIdentifier);
            mDataValid = true;
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
        }

        // notify that Dataset has Changed
        boolean notifyDataChanged = true;

        // check if we can get better notifies
        if(mUseIndividualNotifies && oldCursor != null && newCursor != null && !oldCursor.isClosed() && !newCursor.isClosed()) {
            notifyDataChanged = notifyItems(oldCursor, newCursor);
        }

        if(notifyDataChanged) {
            notifyDataSetChanged();
        }

        return oldCursor;
    }

    public void setUseIndividualNotifies(boolean use) {
        mUseIndividualNotifies = use;
    }

    /**
     * Compares the two cursors and calls notifyChanged on every item that changed.
     * @param oldCursor
     * @param newCursor
     * @return
     */
    private boolean notifyItems(Cursor oldCursor, Cursor newCursor) {
        String[] columns = new String[] { BaseColumns._ID };
        CursorJoiner joiner = new CursorJoiner(oldCursor, columns, newCursor, columns);
        for (CursorJoiner.Result res : joiner) {
            switch (res) {
                case LEFT:
                    notifyItemRemoved(newCursor.getPosition());
                    break;
                case RIGHT:
                    notifyItemInserted(newCursor.getPosition());
                    break;
                case BOTH:
//                    for(int i = 0; i < newCursor.getColumnCount(); i++) {
//                        if(!oldCursor.getString(i).equals(newCursor.getString(i))) {
//                            notifyItemChanged(newCursor.getPosition());
//                        }
//                    }
                    if (getRowHash(oldCursor) != getRowHash(newCursor)) {
                        notifyItemChanged(newCursor.getPosition());
                    }
                    break;
            }
        }

        return false;
    }

    private int getRowHash(Cursor cursor) {
        StringBuilder result = new StringBuilder("row");
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            result.append(cursor.getString(i));
        }
        return result.toString().hashCode();
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * Runs a query with the specified constraint. This query is requested
     * by the filter attached to this adapter.
     *
     * The query is provided by a
     * {@link FilterQueryProvider}.
     * If no provider is specified, the current cursor is not filtered and returned.
     *
     * After this method returns the resulting cursor is passed to {@link #changeCursor(Cursor)}
     * and the previous cursor is closed.
     *
     * This method is always executed on a background thread, not on the
     * application's main thread (or UI thread.)
     *
     * Contract: when constraint is null or empty, the original results,
     * prior to any filtering, must be returned.
     *
     * @param constraint the constraint with which the query must be filtered
     *
     * @return a Cursor representing the results of the new query
     *
     * @see #getFilter()
     * @see #getFilterQueryProvider()
     * @see #setFilterQueryProvider(FilterQueryProvider)
     */
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }

        return mCursor;
    }

    public Filter getFilter() {
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
    }

    /**
     * Returns the query filter provider used for filtering. When the
     * provider is null, no filtering occurs.
     *
     * @return the current filter query provider or null if it does not exist
     *
     * @see #setFilterQueryProvider(FilterQueryProvider)
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public FilterQueryProvider getFilterQueryProvider() {
        return mFilterQueryProvider;
    }

    /**
     * Sets the query filter provider used to filter the current Cursor.
     * The provider's
     * {@link FilterQueryProvider#runQuery(CharSequence)}
     * method is invoked when filtering is requested by a client of
     * this adapter.
     *
     * @param filterQueryProvider the filter query provider or null to remove it
     *
     * @see #getFilterQueryProvider()
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }

    /**
     * Called when the {@link ContentObserver} on the cursor receives a change notification.
     * Can be implemented by sub-class.
     *
     * @see ContentObserver#onChange(boolean)
     */
    protected void onContentChanged() {

    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        private final String TAG = NotifyingDataSetObserver.class.getSimpleName();
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}